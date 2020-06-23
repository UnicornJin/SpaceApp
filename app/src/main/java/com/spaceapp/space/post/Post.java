package com.spaceapp.space.post;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.spaceapp.space.MainActivity;

import static androidx.core.content.ContextCompat.startActivity;

public class Post {
    private String author;
    private String content;
    private Timestamp time;
    private Uri imageUri;
    private String title;
    private boolean withImage;

    public Post(String author, String content, Timestamp time, Uri imageUri, String title){
        this.author = author;
        this.content = content;
        this.time = time;
        this.imageUri = imageUri;
        this.title = title;
        this.withImage = true;
    }

    public Post(String author, String content, Timestamp time, String title){
        this.author = author;
        this.content = content;
        this.time = time;
        this.imageUri = null;
        this.title = title;
        this.withImage = false;
    }

    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public Timestamp getTime() {
        return time;
    }

    public String getTimeString() {
        return  time.toDate().toString();
    }

    public String getTitle() {
        return title;
    }

    public boolean isWithImage() {return withImage; }

    public void setImageUri(Uri uri) {
        this.withImage = true;
        this.imageUri = uri;
    }

    public void delete(final View view) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("USERDATA")
                .document(MainActivity.currentUser.getUid())
                .collection("POSTS")
                .document(this.time.toString())
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(view.getContext(), "Post Deleted Successfully!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(view.getContext(), "ERROR: Cannot delete this post!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void modefy(View view) {
        Intent intent = new Intent(view.getContext(), ModifyPost.class);
        intent.putExtra("post_title", this.title);
        intent.putExtra("post_content", this.content);
        intent.putExtra("post_image", this.imageUri.toString());
        startActivity(view.getContext(), intent, null);
    }

    @Override
    public String toString() {
        return  "Time: " + time.toString() +
                "\nAuthor:" + author +
                "\nTitle:" + title +
                "\nContent:" + content +
                "\nis With Image? " + withImage +
                "\nImageUri:" + ((imageUri == null) ? "" : imageUri.toString());
    }
}