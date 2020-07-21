package com.spaceapp.space.post;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.Image;
import android.net.Uri;
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
import com.google.firebase.firestore.DocumentReference;
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
                            mSelected.uri.toString(),
                            postTitle.getText().toString());
                    sendPost(sendingPost);
                }
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
        final FirebaseStorage storage = FirebaseStorage.getInstance();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        storage.getReference()
                .child("posts/" + post.getAuthorId() + post.getTimeString() + ".jpg")
                .putFile(Uri.parse(post.getImage()))
                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            storage.getReference("posts/" + post.getAuthorId() + post.getTimeString() + ".jpg")
                                    .getDownloadUrl()
                                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            Log.i(">>>>>>>", "imageUrl:" + uri.toString());
                                            post.setImage(uri.toString());
                                            post.setAuthorId(MainActivity.currentUser.getUid());
                                            db.collection("posts")
                                                    .document()
                                                    .set(post)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            onBackPressed();
                                                        }
                                                    });
                                        }
                                    });
                        }
                    }
                });
    }
}