package com.spaceapp.space.account;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.spaceapp.space.MainActivity;
import com.spaceapp.space.R;

import java.util.HashMap;
import java.util.Map;

public class AddContact extends AppCompatActivity {

    EditText my_name;
    EditText his_name;
    Button Add;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        final String contactUid = getIntent().getStringExtra("uid");

        my_name = (EditText) findViewById(R.id.my_name);
        his_name = (EditText) findViewById(R.id.contact_name);
        Add = (Button) findViewById(R.id.addnew_contact);

        Add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (my_name.getText().length() == 0 || his_name.getText().length() == 0) {
                    Toast.makeText(AddContact.this, "Information needed", Toast.LENGTH_SHORT).show();
                } else {
                    String myName = my_name.getText().toString();
                    String ContactName = his_name.getText().toString();

                    Map<String, String> dataToMe = new HashMap<>();
                    dataToMe.put("ContactName", ContactName);
                    dataToMe.put("Uid", contactUid);

                    Map<String, String> dataToHim = new HashMap<>();
                    dataToHim.put("ContactName", myName);
                    dataToHim.put("Uid", MainActivity.currentUser.getUid());

                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("USERDATA")
                            .document(MainActivity.currentUser.getUid())
                            .collection("CONTACTS")
                            .document(contactUid)
                            .set(dataToMe);

                    db.collection("USERDATA")
                            .document(contactUid)
                            .collection("CONTACTS")
                            .document(MainActivity.currentUser.getUid())
                            .set(dataToHim);

                    onBackPressed();
                }
            }
        });
    }
}