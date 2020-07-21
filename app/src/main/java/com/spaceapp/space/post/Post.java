package com.spaceapp.space.post;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.spaceapp.space.MainActivity;

import static androidx.core.content.ContextCompat.startActivity;

/**
 * This is the Post class, includes necessary methods for post handling.
 */
public class Post {
    private String author;
    private String authorId;
    private String title;
    private String content;
    private Timestamp published;
    private String image;

    public Post(String author, String content, Timestamp time, String imageUri, String title){
        this.author = author;
        this.content = content;
        this.published = time;
        this.image = imageUri;
        this.title = title;
    }

    public Post(String author, String authorId, String content, Timestamp time, String imageUri, String title){
        this.author = author;
        this.authorId = authorId;
        this.content = content;
        this.published = time;
        this.image = imageUri;
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    public String getImage() {
        return image;
    }

    public Timestamp getPublished() {
        return published;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getAuthorId() {
        return this.authorId;
    }

    public String getTimeString() {
        return  published.toDate().toString();
    }

    public String getTitle() {
        return title;
    }

    public void setImage(String image) {
        this.image = image;
    }

    /**
     * Before real delete a post, ask the user for confirmation.
     * Then start deleting process.
     * @param view
     */
    public void delete(final View view) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(view.getContext());
        dialog.setTitle("Delete Post");
        dialog.setMessage("Are you sure about deleting this post?");
        dialog.setCancelable(false);
        dialog.setPositiveButton("Yes, Delete.", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteHelper();
            }
        });
        dialog.setNegativeButton("No, don't!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        dialog.show();

    }

    /**
     * send delete order to database.
     */
    private void deleteHelper() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("posts")
                .whereEqualTo("authorId", MainActivity.currentUser.getUid())
                .whereEqualTo("published", this.published)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot q : queryDocumentSnapshots) {
                            q.getReference().delete();
                        }
                    }
                });
    }

    /**
     * start modify activity.
     * @param view
     */
    public void modefy(View view) {
        Intent intent = new Intent(view.getContext(), ModifyPost.class);
        intent.putExtra("post_title", this.title);
        intent.putExtra("post_content", this.content);
        intent.putExtra("post_image", this.image);
        intent.putExtra("post_time", this.published.toDate().toString());
        startActivity(view.getContext(), intent, null);
    }

    @Override
    public String toString() {
        return  "Time: " + published.toString() +
                "\nAuthor:" + author +
                "\nTitle:" + title +
                "\nContent:" + content +
                "\nImageUri:" + ((image == null) ? "" : image.toString());
    }
}