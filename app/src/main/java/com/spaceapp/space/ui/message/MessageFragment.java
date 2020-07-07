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
        db.collection("USERDATA").document(MainActivity.currentUser.getUid())
                .collection("CONTACTS")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(">>>>", "Contact List Listen Failed: " + e.toString());
                        }

                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            String name = doc.getString("ContactName");
                            String uid = doc.getString("Uid");

                            final Contact newContact = new Contact();
                            newContact.setName(name);
                            newContact.setUid(uid);

                            db.collection("USERDATA").document(MainActivity.currentUser.getUid())
                                    .collection("CONTACTS")
                                    .document(newContact.getUid())
                                    .collection("MSGLIST")
                                    .orderBy("time", Query.Direction.ASCENDING)
                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                            if (e != null) {
                                                Log.w(">>>>>", "Last message listen failed: " + e);
                                            }

                                            for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {

                                                Log.i(">>>>>>", "Last Message Added: " + dc.getDocument().getString("content"));

                                                newContact.setLastMsg(dc.getDocument().getString("content"));
                                                newContact.setLastMsgTime(dc.getDocument().getTimestamp("time").getSeconds());
                                            }

                                            final boolean[] isBlocked = {false};
                                            db.collection("USERDATA").document(MainActivity.currentUser.getUid())
                                                    .collection("BLOCKLIST")
                                                    .document(newContact.getUid())
                                                    .get()
                                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                @Override
                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                    if (documentSnapshot.getBoolean("isBlocked") != null) {
                                                        isBlocked[0] = documentSnapshot.getBoolean("isBlocked");
                                                    }

                                                    if (isBlocked[0]) {

                                                    }else if (contactList.contains(newContact)) {
                                                        contactList.remove(newContact);
                                                        contactList.add(newContact);
                                                    } else {
                                                        contactList.add(newContact);
                                                    }

                                                    Collections.sort(contactList, new Comparator<Contact>() {
                                                        @Override
                                                        public int compare(Contact o1, Contact o2) {
                                                            return (-1) * o1.getLastMsgTime().compareTo(o2.getLastMsgTime());
                                                        }
                                                    });
                                                    adapter.notifyDataSetChanged();
                                                }
                                            });
                                        }
                                    });
                        }

                    }
                });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }
}
