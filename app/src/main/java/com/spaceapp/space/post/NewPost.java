package com.spaceapp.space.post;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.huantansheng.easyphotos.EasyPhotos;
import com.huantansheng.easyphotos.models.album.entity.Photo;
import com.spaceapp.space.MainActivity;
import com.spaceapp.space.R;

import java.util.ArrayList;

/**
 * This activity will help to create a new post.
 */
public class NewPost extends AppCompatActivity {
    private final int ADD_PHOTO = 1;

    private EditText postTitle;
    private EditText postContent;

    private ImageView tempImage;

    Photo mSelected;

    /**
     * Load the newPost layout
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        postTitle = (EditText) findViewById(R.id.newPost_title);
        postContent = (EditText) findViewById(R.id.newPost_text);

        tempImage = (ImageView) findViewById(R.id.newPost_choosenimage);
        tempImage.setVisibility(View.INVISIBLE);

        //After the user tapping send button, Space generate a post object and start sending process.
        ((Button) findViewById(R.id.send_post)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Post sendingPost;
                if (mSelected != null) {
                    sendingPost = new Post(MainActivity.currentUser.getUid(),
                            postContent.getText().toString(),
                            Timestamp.now(),
                            mSelected.uri,
                            postTitle.getText().toString());
                } else {
                    sendingPost = new Post(MainActivity.currentUser.getUid(),
                            postContent.getText().toString(),
                            Timestamp.now(),
                            postTitle.getText().toString());
                }
                sendPost(sendingPost);
            }
        });

        //This button helps the user to start adding photo to the post.
        ((Button) findViewById(R.id.newPost_addphoto)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EasyPhotos.createAlbum(NewPost.this, true, GlideEngine.getInstance())
                        .setFileProviderAuthority("com.spaceapp.space.fileprovider")
                        .start(ADD_PHOTO);
            }
        });
    }

    /**
     * Handle photo gotten from photo selection activity.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_PHOTO && resultCode == RESULT_OK) {

            ArrayList<Photo> resultPhotos = data.getParcelableArrayListExtra(EasyPhotos.RESULT_PHOTOS);

            mSelected = resultPhotos.get(0);

            tempImage.setVisibility(View.VISIBLE);
            GlideEngine.getInstance().loadPhoto(NewPost.this, mSelected.uri, tempImage);
        }
    }

    /**
     * This method will send the post created by user to database.
     * @param post the new post created
     */
    private void sendPost(final Post post) {

        final Post[] sentingPost = new Post[1];

        //have / do not have image, should be handled differently
        if (post.isWithImage()) { //If the post is with image, we need to upload the image and store address of image
            FirebaseStorage storage = FirebaseStorage.getInstance();
            final StorageReference imageRef = storage.getReference()
                    .child("userData")
                    .child("PostImageStorage")
                    .child(MainActivity.currentUser.getUid())
                    .child(post.getTime().toString() + ".jpg");

            imageRef.putFile(post.getImageUri()).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        sentingPost[0] = new Post(
                                post.getAuthor(),
                                post.getContent(),
                                post.getTime(),
                                null,
                                post.getTitle()
                        );

                        Log.i(">>>>>>>", "Senting post:" + sentingPost[0].toString());

                        FirebaseFirestore db = FirebaseFirestore.getInstance();

                        db.collection("POSTPLAZA")
                                .document(post.getTime().toString())
                                .set(sentingPost[0]);

                        db.collection("USERDATA")
                                .document(MainActivity.currentUser.getUid())
                                .collection("POSTS")
                                .document(post.getTime().toString())
                                .set(sentingPost[0])
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.i(">>>>>>>>", "Post Succeed");

                                        onBackPressed();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(NewPost.this, "Post Failed", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
        } else { //This part deal with posts without image
            sentingPost[0] = new Post(
                    post.getAuthor(),
                    post.getContent(),
                    post.getTime(),
                    post.getTitle()
            );

            Log.i(">>>>>>>", "Senting post:" + sentingPost[0].toString());

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("POSTPLAZA")
                    .document(post.getTime().toString())
                    .set(sentingPost[0]);

            db.collection("USERDATA")
                    .document(MainActivity.currentUser.getUid())
                    .collection("POSTS")
                    .document(post.getTime().toString())
                    .set(sentingPost[0])
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.i(">>>>>>>>", "Post Succeed");

                            onBackPressed();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(NewPost.this, "Post Failed", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


}