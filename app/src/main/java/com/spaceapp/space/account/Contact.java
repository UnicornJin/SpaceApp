package com.spaceapp.space.account;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.spaceapp.space.MainActivity;

import java.io.Serializable;
import java.util.HashMap;

/**
 * This class is contact of user.
 * And it holds some operations on contact.
 */
public class Contact implements Serializable {

    private final long serialVersionUID = 123456;

    private String Contactname;
    private String Myname;
    private String Uid;

    public void setContactName(String Contactname) {
        this.Contactname = Contactname;
    }

    public void setMyname(String myname) {
        Myname = myname;
    }

    public void setUid(String uid) {
        this.Uid = uid;
    }

    public String getContactName() {
        return this.Contactname;
    }

    public String getMyname() {
        return Myname;
    }

    public String getUid() {
        return this.Uid;
    }

    /**
     * This method determines how to decide whether an object is equal to the contact.
     * only the contacts with the same uid will be equal to the contact.
     * @param obj the object waiting to be check.
     * @return true for obj is the same with the contact, false for not.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Contact) {
            return this.getUid().equals(((Contact) obj).getUid());
        } else {
            return false;
        }
    }


    /**
     * This method will ask user for confirmation on deleting the contact,
     * then start delete process.
     * @param view current view, for convenience
     */
    public void delete(final View view) {
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
                deleteHelper(view);
            }
        });
        dialog.show();
    }

    /**
     * This method will send delete contact order to database.
     * @param view current view, for convenience
     */
    private void deleteHelper(final View view) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("friends")
                .whereEqualTo("personAId", MainActivity.currentUser.getUid())
                .whereEqualTo("personBId", this.getUid())
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                    for (QueryDocumentSnapshot q : queryDocumentSnapshots) {
                        q.getReference().delete();
                    }
                }
            }
            });
        db.collection("friends")
                .whereEqualTo("personBId", MainActivity.currentUser.getUid())
                .whereEqualTo("personAId", this.getUid())
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                            for (QueryDocumentSnapshot q : queryDocumentSnapshots) {
                                q.getReference().delete();
                            }
                        }
                    }
                });
    }

    /**
     * This method will ask user for the new name of the contact.
     * @param view current view, for convenience.
     */
    public void rename(final View view) {
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
                renameHelper(view, ed.getText().toString());
            }
        });
        dialog.show();
    }

    /**
     * This method will send new name of contact to database.
     * @param view current view for convenience
     * @param newName new contact name from the user.
     */
    private void renameHelper(final View view, final String newName) {
        if (!newName.isEmpty()) {

            Log.i(">>>>", "My UID:" + MainActivity.currentUser.getUid());
            Log.i(">>>>", "Contact UID:" + getUid());

            final FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("friends")
                    .whereEqualTo("personAId", MainActivity.currentUser.getUid())
                    .whereEqualTo("personBId", getUid())
                    .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {

                        Log.i(">>>>>>", "q1:" + queryDocumentSnapshots);
                        for (QueryDocumentSnapshot q : queryDocumentSnapshots) {
                            Log.i(">>>>>>", "q2:" + q.toString());
                            q.getReference().update("nameBToA", newName);
                        }
                    }
                }
            });
            db.collection("friends")
                    .whereEqualTo("personBId", MainActivity.currentUser.getUid())
                    .whereEqualTo("personAId", getUid())
                    .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {

                        Log.i(">>>>>>", "q3:" + queryDocumentSnapshots);
                        for (QueryDocumentSnapshot q : queryDocumentSnapshots) {
                            Log.i(">>>>>>", "q4:" + q.toString());
                            q.getReference().update("nameAToB", newName);
                        }
                    }
                }
            });
        }
    }
}
