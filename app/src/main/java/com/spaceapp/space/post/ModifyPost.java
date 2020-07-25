package com.spaceapp.space.post;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
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
            BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
            bitmapOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(imageUri.getPath(), bitmapOptions);

            int inSampleSize = 1;
            while (bitmapOptions.outHeight/inSampleSize > 720 || bitmapOptions.outWidth/inSampleSize > 1080) {
                inSampleSize *= 2;
            }

            bitmapOptions.inSampleSize = inSampleSize;
            bitmapOptions.inJustDecodeBounds = false;
            Bitmap bitmap = BitmapFactory.decodeFile(imageUri.getPath(), bitmapOptions);

            imageChosen.setImageBitmap(bitmap);
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
                if (imageUri != null){
                    sendingPost = new Post(MainActivity.currentUser.getUid(),
                            modContent.getText().toString(),
                            Timestamp.now(),
                            imageUri.toString(),
                            modTitle.getText().toString());
                    modifypost(sendingPost, intent.getLongExtra("post_time_second", 0), intent.getIntExtra("post_time_ns", 0));
                }
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
            imageUri = mSelected.uri;

            GlideEngine.getInstance().loadPhoto(ModifyPost.this, mSelected.uri, imageChosen);
        }
    }

    /**
     * This method will update the modified post to database.
     * @param post the post needed to send
     * @param second to get time of the original post, need this to change the previous post.
     * @param ns to get time of the original post, need this to change the previous post.
     */
    private void modifypost(final Post post, long second, int ns) {

        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final FirebaseStorage storage = FirebaseStorage.getInstance();

        Log.i(">>>>>>>", "time:" + new Timestamp(second, ns));

        db.collection("posts")
                .whereEqualTo("authorId", MainActivity.currentUser.getUid())
                .whereEqualTo("published", new Timestamp(second, ns))
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot q : queryDocumentSnapshots) {
                            q.getReference().delete();
                        }
                    }
                });

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