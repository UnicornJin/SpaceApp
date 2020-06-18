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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.spaceapp.space.MainActivity;
import com.spaceapp.space.R;
import com.spaceapp.space.account.Contact;
import com.spaceapp.space.account.ContactAdapter;
import com.spaceapp.space.ui.chatwindow.ChatWindow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Serializable clickingContact = contactList.get(position);
                Intent intent = new Intent(getContext(), ChatWindow.class);
                intent.putExtra("contact", clickingContact);
                startActivity(intent);
            }
        });

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

    @Override
    public void onResume() {
        super.onResume();

        contactList.clear();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("USERDATA").document(MainActivity.currentUser.getUid())
                .collection("CONTACTS")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                String name = doc.getString("ContactName");
                                String uid = doc.getString("Uid");

                                final Contact newContact = new Contact();
                                newContact.setName(name);
                                newContact.setUid(uid);

                                db.collection("USERDATA").document(MainActivity.currentUser.getUid())
                                        .collection("CONTACTS")
                                        .document(newContact.getUid())
                                        .collection("MSGLIST")
                                        .orderBy("time")
                                        .limit(1)
                                        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (QueryDocumentSnapshot doc : task.getResult()) {

                                                Log.i(">>>>>", "Last MSg:" + doc.getString("content"));

                                                newContact.setLastMsg(doc.get("content").toString());
                                            }
                                            contactList.add(newContact);
                                            adapter.notifyDataSetChanged();
                                        }
                                    }
                                });
                            }
                        } else {
                            Log.e(">>>>>>", "Cannot load contact Lst");
                        }
                    }
                });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }
}
