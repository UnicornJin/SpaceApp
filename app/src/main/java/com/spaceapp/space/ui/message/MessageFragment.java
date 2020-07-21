package com.spaceapp.space.ui.message;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.spaceapp.space.MainActivity;
import com.spaceapp.space.R;
import com.spaceapp.space.account.Contact;
import com.spaceapp.space.account.ContactAdapter;
import com.spaceapp.space.ui.chatwindow.ChatWindow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;

/**
 * This is the message page of Space App
 * All contacts will be shown here
 */
public class MessageFragment extends Fragment {

    private List<Contact> contactList = new ArrayList<>();

    private ContactAdapter adapter;

    private SwipeRefreshLayout messageRefresh;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        final View root = inflater.inflate(R.layout.fragment_message, container, false);

        adapter = new ContactAdapter(MessageFragment.this.getContext(), R.layout.layout_contact, contactList);
        ListView listView = (ListView) root.findViewById(R.id.msg_list);
        listView.setAdapter(adapter);

        //When the user tap on a contact
        //a chat window will appear.
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Serializable clickingContact = contactList.get(position);
                Intent intent = new Intent(getContext(), ChatWindow.class);
                intent.putExtra("contact", clickingContact);
                startActivity(intent);
            }
        });

        //swipe down will refresh the contact list.
        messageRefresh = (SwipeRefreshLayout) root.findViewById(R.id.message_swipe);
        messageRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                onResume();
                messageRefresh.setRefreshing(false);
            }
        });

        return root;
    }

    /**
     * get contact list from database and sort them according to time of the last message.
     * check whether they are blocked
     * IF they are blocked, the contact will not be shown in the contact list.
     */
    @Override
    public void onResume() {
        super.onResume();

        contactList.clear();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("friends")
                .whereEqualTo("personAId", MainActivity.currentUser.getUid())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(">>>>", "Contact List Listen Failed: " + e.toString());
                        } else {
                            if (!queryDocumentSnapshots.isEmpty()){
                                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                    final Contact newContact = new Contact();
                                    newContact.setContactName(doc.getString("nameBToA"));
                                    newContact.setMyname(doc.getString("nameAToB"));
                                    newContact.setUid(doc.getString("personBId"));
                                    contactList.add(newContact);
                                }
                            }
                        }
                    }
                });
    }
}
