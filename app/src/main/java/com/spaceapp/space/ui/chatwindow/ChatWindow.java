package com.spaceapp.space.ui.chatwindow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.spaceapp.space.MainActivity;
import com.spaceapp.space.R;
import com.spaceapp.space.account.Contact;
import com.spaceapp.space.message.Message;
import com.spaceapp.space.message.MessageAdapter;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class ChatWindow extends AppCompatActivity {
    private Toolbar chatWindowToolbar;

    private List<Message> msgItemList = new ArrayList<>();

    private EditText inputText;
    private Button send;
    private RecyclerView msgRecyclerView;
    private MessageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_window);

        inputText = (EditText) findViewById(R.id.chat_window_input);
        send = (Button) findViewById(R.id.send_msg_button);
        msgRecyclerView = (RecyclerView) findViewById(R.id.msg_recycler_view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        msgRecyclerView.setLayoutManager(layoutManager);
        adapter = new MessageAdapter(msgItemList);
        msgRecyclerView.setAdapter(adapter);

        Intent intent = getIntent();
        final Contact contact = (Contact) intent.getSerializableExtra("contact");
        Log.i(">>>>>>>>", "chatting contact: " + contact.getUid());

        chatWindowToolbar = (Toolbar) findViewById(R.id.chat_window_toolbar);
        chatWindowToolbar.inflateMenu(R.menu.chat_window_menu);
        ((TextView) findViewById(R.id.chatting_name)).setText(contact.getName());
        chatWindowToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("USERDATA")
                .document(MainActivity.currentUser.getUid())
                .collection("CONTACTS")
                .document(contact.getUid())
                .collection("MSGLIST")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(">>>>>", "Listen failed: " + e.toString());
                        }

                        for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                            switch (dc.getType()) {
                                case ADDED:
                                    Message temp = new Message(
                                            dc.getDocument().getTimestamp("time"),
                                            dc.getDocument().getString("content"),
                                            dc.getDocument().getBoolean("sent")
                                    );
                                    msgItemList.add(temp);
                                    adapter.notifyDataSetChanged();
                                    msgRecyclerView.scrollToPosition(msgItemList.size() - 1);
                                    break;
                            }
                        }
                    }
                });



        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sendingContent = inputText.getText().toString();
                if (!"".equals(sendingContent)) {
                    Message message = new Message(Timestamp.now(), sendingContent, true);
                    sendMsg(message, contact);
                    inputText.setText("");
                }
            }
        });

    }

    private void sendMsg(Message msg, Contact contact) {

        Message msgToMe = new Message(msg.getTime(), msg.getContent(), true);
        Message msgToHim = new Message(msg.getTime(), msg.getContent(), false);

        Log.i(">>>>>", "Writing to mine :" + contact.getUid());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("USERDATA")
                .document(MainActivity.currentUser.getUid())
                .collection("CONTACTS")
                .document(contact.getUid())
                .collection("MSGLIST")
                .document(msg.getTime().toString())
                .set(msgToMe);

        Log.i(">>>>>", "Writing to :" + contact.getUid());

        db.collection("USERDATA")
                .document(contact.getUid())
                .collection("CONTACTS")
                .document(MainActivity.currentUser.getUid())
                .collection("MSGLIST")
                .document(msg.getTime().toString())
                .set(msgToHim);
    }

}
