package com.spaceapp.space.post;

import android.net.Uri;
import com.google.firebase.Timestamp;

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