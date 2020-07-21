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

/**
 * When the user want to have a chat with a post owner,
 * this class will help them.
 */
public class AddContact extends AppCompatActivity {

    EditText my_name;
    EditText his_name;
    Button Add;

    /**
     * The user need to name the contact and name himself.
     * So load the naming page first and then send chat request.
     * @param savedInstanceState
     */
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
                    //Deal with invalid name
                    Toast.makeText(AddContact.this, "Information needed", Toast.LENGTH_SHORT).show();
                } else {
                    //After tapping the Add button, Space will generate data needed and send to database.
                    String myName = my_name.getText().toString();
                    String ContactName = his_name.getText().toString();

                    Map<String, String> data = new HashMap<>();
                    data.put("nameBToA", ContactName);
                    data.put("personBId", contactUid);
                    data.put("personAId", MainActivity.currentUser.getUid());
                    data.put("nameAToB", myName);
                    data.put("personB", ContactName);
                    data.put("personA", myName);

                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("friends")
                            .add(data);
                    onBackPressed();
                }
            }
        });
    }
}