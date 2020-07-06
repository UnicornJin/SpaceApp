package com.spaceapp.space.account;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.spaceapp.space.MainActivity;

import java.io.Serializable;

public class Contact implements Serializable {

    private final long serialVersionUID = 123456;

    private String name;
    private String lastMsg;
    private String Uid;
    private Long lastMsgTime;

    public void setName(String name) {
        this.name = name;
    }

    public void setLastMsg(String lastMsg) {
        this.lastMsg = lastMsg;
    }

    public void setUid(String uid) {
        this.Uid = uid;
    }

    public void setLastMsgTime(Long time) { this.lastMsgTime = time; }

    public String getName() {
        return this.name;
    }

    public String getLastMsg() {
        return lastMsg;
    }

    public String getUid() {
        return this.Uid;
    }

    public Long getLastMsgTime() { return this.lastMsgTime; }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Contact) {
            return this.getUid().equals(((Contact) obj).getUid());
        } else {
            return false;
        }
    }


    public void delete(View view) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(view.getContext());
        dialog.setTitle("Delete Contact");
        dialog.setMessage("Are you sure about deleting this contact?");
        dialog.setCancelable(false);
        dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteHelper();
            }
        });
        dialog.show();
    }

    private void deleteHelper() {

    }

    public void block(View view) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(view.getContext());
        dialog.setTitle("Block Contact");
        dialog.setMessage("Are you sure about blocking this contact? You will not receive message from this contact anymore.");
        dialog.setCancelable(false);
        dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                blockHelper();
            }
        });
        dialog.show();
    }

    private void blockHelper() {

    }

    public void rename(View view) {
        final EditText ed = new EditText(view.getContext());
        ed.setHint("New Contact name");
        AlertDialog.Builder dialog = new AlertDialog.Builder(view.getContext());
        dialog.setTitle("Rename Contact");
        dialog.setView(ed);
        dialog.setCancelable(false);
        dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                renameHelper(ed.getText().toString());
            }
        });
        dialog.show();
    }

    private void renameHelper(String newName) {
        if (!newName.isEmpty()) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("USERDATA")
                    .document(MainActivity.currentUser.getUid())
                    .collection("CONTACTS")
                    .document(this.Uid)
                    .update("ContactName", newName);
        }
    }
}
