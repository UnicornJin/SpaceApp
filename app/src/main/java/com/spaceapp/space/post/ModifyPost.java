package com.spaceapp.space.post;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
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
 * This class is used for post modification.
 */
public class ModifyPost extends AppCompatActivity {

    private final int CHANGE_PHOTO = 2;

    private EditText modTitle;
    private EditText modContent;
    private Button modPhoto;
    private Button mod;
    private Button cancel;
    private ImageView imageChosen;

    private Uri imageUri;

    Photo mSelected;

    /**
     * This method load modify page first and then assign functions to each button
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_post);

        this.modTitle = (EditText) findViewById(R.id.mod_post_title);
        this.modContent = (EditText) findViewById(R.id.mod_post_content);
        this.modPhoto = (Button) findViewById(R.id.mod_post_photo);
        this.mod = (Button) findViewById(R.id.mod_post);
        this.cancel = (Button) findViewById(R.id.mod_cancel);
        this.imageChosen = (ImageView) findViewById(R.id.mod_post_chosen_image);

        final Intent intent = getIntent();
        modTitle.setText(intent.getStringExtra("post_title"));
        modContent.setText(intent.getStringExtra("post_content"));
        if (intent.getStringExtra("post_image") != null) {
            imageUri = Uri.parse(intent.getStringExtra("post_image"));
            imageChosen.setImageURI(imageUri);
        }

        //modPhoto button will start a photo selection activity and replace original photo with chosen one.
        this.modPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EasyPhotos.createAlbum(ModifyPost.this, true, GlideEngine.getInstance())
                        .setFileProviderAuthority("com.spaceapp.space.fileprovider")
                        .start(CHANGE_PHOTO);
            }
        });

        //mod button will create a Post object and start the sending process
        this.mod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Post sendingPost;
                if (mSelected != null) {
                    sendingPost = new Post(MainActivity.currentUser.getUid(),
                            modContent.getText().toString(),
                            Timestamp.now(),
                            mSelected.uri,
                            modTitle.getText().toString());
                } else if (imageUri != null){
                    sendingPost = new Post(MainActivity.currentUser.getUid(),
                            modContent.getText().toString(),
                            Timestamp.now(),
                            imageUri,
                            modTitle.getText().toString());
                } else {
                    sendingPost = new Post(MainActivity.currentUser.getUid(),
                            modContent.getText().toString(),
                            Timestamp.now(),
                            modTitle.getText().toString());
                }

                modifypost(sendingPost, intent.getStringExtra("post_time"));
            }
        });


        this.cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    /**
     * After choosing photo, this method handle the photo result.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHANGE_PHOTO && resultCode == RESULT_OK) {

            ArrayList<Photo> resultPhotos = data.getParcelableArrayListExtra(EasyPhotos.RESULT_PHOTOS);

            mSelected = resultPhotos.get(0);

            GlideEngine.getInstance().loadPhoto(ModifyPost.this, mSelected.uri, imageChosen);
        }
    }

    /**
     * This method will update the modified post to database.
     * @param post the post needed to send
     * @param time time of the original post, need this to change the previous post.
     */
    private void modifypost(final Post post, String time) {
        final Post[] sentingPost = new Post[1];

        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final FirebaseStorage storage = FirebaseStorage.getInstance();

        db.collection("POSTPLAZA").document(time).delete();
        db.collection("USERDATA")
                .document(MainActivity.currentUser.getUid())
                .collection("POSTS")
                .document(time)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if (post.isWithImage()) {
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
                                                        Log.i(">>>>>>>>", "Modify Succeed");

                                                        onBackPressed();
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(ModifyPost.this, "Modify Failed", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }
                            });
                        } else {
                            sentingPost[0] = new Post(
                                    post.getAuthor(),
                                    post.getContent(),
                                    post.getTime(),
                                    post.getTitle()
                            );

                            Log.i(">>>>>>>", "Senting post:" + sentingPost[0].toString());

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
                                    Toast.makeText(ModifyPost.this, "Post Failed", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });

        storage.getReference()
                .child("userData")
                .child("PostImageStorage")
                .child(MainActivity.currentUser.getUid())
                .child(time + ".jpg")
                .delete();
    }
}