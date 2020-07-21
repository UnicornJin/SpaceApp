package com.spaceapp.space.ui.chatwindow;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.spaceapp.space.MainActivity;
import com.spaceapp.space.R;
import com.spaceapp.space.account.Contact;
import com.spaceapp.space.message.Message;
import com.spaceapp.space.message.MessageAdapter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;

/**
 * This activity is the chat window of Space App
 * When the user tap on a contact, program will start this activity.
 */
public class ChatWindow extends AppCompatActivity {
    private Toolbar chatWindowToolbar;

    private List<Message> msgItemList = new ArrayList<>();

    private EditText inputText;
    private Button send;
    private RecyclerView msgRecyclerView;
    private MessageAdapter adapter;

    private Contact contact;

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
        contact = (Contact) intent.getSerializableExtra("contact");

        chatWindowToolbar = (Toolbar) findViewById(R.id.chat_window_toolbar);
        chatWindowToolbar.inflateMenu(R.menu.chat_window_menu);
        ((TextView) findViewById(R.id.chatting_name)).setText(contact.getContactName());

        //assign the top back button a back function
        chatWindowToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //set functions to each menu button
        chatWindowToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.chatwindow_delete:
                        contact.delete(getWindow().getDecorView());
                        break;
                    case R.id.chatwindow_rename:
                        contact.rename(getWindow().getDecorView());
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        //Load message list from database.
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("messages")
                .whereEqualTo("receiverId", MainActivity.currentUser.getUid())
                .whereEqualTo("senderId", contact.getUid())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(">>>>>", "Listen failed: " + e.toString());
                        }

                        if (!queryDocumentSnapshots.isEmpty()){
                            for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                                switch (dc.getType()) {
                                    case ADDED:
                                        Message temp = new Message(
                                                dc.getDocument().getTimestamp("time"),
                                                dc.getDocument().getString("content")
                                        );
                                        temp.setType(true);
                                        msgItemList.add(temp);
                                        msgItemList.sort(new Comparator<Message>() {
                                            @Override
                                            public int compare(Message o1, Message o2) {
                                                return o1.getTime().compareTo(o2.getTime());
                                            }
                                        });
                                        adapter.notifyDataSetChanged();
                                        msgRecyclerView.scrollToPosition(msgItemList.size() - 1);
                                        break;
                                }
                            }
                        }
                    }
                });

        db.collection("messages")
                .whereEqualTo("senderId", MainActivity.currentUser.getUid())
                .whereEqualTo("receiverId", contact.getUid())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(">>>>>", "Listen failed: " + e.toString());
                        }

                        if (!queryDocumentSnapshots.isEmpty()){
                            for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                                switch (dc.getType()) {
                                    case ADDED:
                                        Message temp = new Message(
                                                dc.getDocument().getTimestamp("time"),
                                                dc.getDocument().getString("content")
                                        );
                                        temp.setType(false);
                                        msgItemList.add(temp);
                                        msgItemList.sort(new Comparator<Message>() {
                                            @Override
                                            public int compare(Message o1, Message o2) {
                                                return o1.getTime().compareTo(o2.getTime());
                                            }
                                        });
                                        adapter.notifyDataSetChanged();
                                        msgRecyclerView.scrollToPosition(msgItemList.size() - 1);
                                        break;
                                }
                            }
                        }
                    }
                });



        //send message button, will start send message process.
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

    /**
     * This method will send message to database.
     * @param msg the message object generated.
     * @param contact the contact user chatting with
     */
    private void sendMsg(Message msg, Contact contact) {

        Message sendingMsg = new Message(
                contact.getMyname(),
                MainActivity.currentUser.getUid(),
                contact.getContactName(),
                contact.getUid(),
                msg.getContent(),
                msg.getTime()
                );
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("messages")
                .add(sendingMsg);
    }


}
