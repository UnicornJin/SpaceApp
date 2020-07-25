package com.spaceapp.space.post;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.spaceapp.space.MainActivity;
import com.spaceapp.space.R;
import com.spaceapp.space.account.AddContact;
import com.spaceapp.space.account.Contact;
import com.spaceapp.space.ui.chatwindow.ChatWindow;

import java.io.Serializable;
import java.util.List;

import static androidx.core.content.ContextCompat.startActivity;

/**
 * This adapter helps to show a post in Plaza page=
 *
 * It has add contact function.
 */
public class PostPlazaAdapter extends RecyclerView.Adapter<PostPlazaAdapter.ViewHolder>{
    private List<Post> mPostList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View view;

        ImageView postImage;
        TextView postTitle;
        TextView postContent;
        TextView postTime;
        Button chat;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            this.postImage = (ImageView) view.findViewById(R.id.plaza_post_image);
            this.postTitle = (TextView) view.findViewById(R.id.plaza_post_title);
            this.postContent = (TextView) view.findViewById(R.id.plaza_post_content);
            this.postTime = (TextView) view.findViewById(R.id.plaza_post_time);
            this.chat = (Button) view.findViewById(R.id.plaza_post_chat);
        }
    }

    public PostPlazaAdapter(List<Post> postList) {
        this.mPostList = postList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_plaza_post, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return  holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Post post = mPostList.get(position);
        holder.postTitle.setText(post.getTitle());
        holder.postContent.setText(post.getContent());
        holder.postImage.setVisibility(View.VISIBLE);
        holder.postImage.setMaxHeight(250);
        holder.postTime.setText(post.getTimeString());

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(Uri.parse(post.getImage()).getPath(), bitmapOptions);

        int inSampleSize = 1;
        while (bitmapOptions.outHeight/inSampleSize > 720 || bitmapOptions.outWidth/inSampleSize > 1080) {
            inSampleSize *= 2;
        }

        bitmapOptions.inSampleSize = inSampleSize;
        bitmapOptions.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(Uri.parse(post.getImage()).getPath(), bitmapOptions);
        holder.postImage.setImageBitmap(bitmap);
        holder.chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startChat(post, holder.view);
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.mPostList.size();
    }

    private void startChat(final Post post, final View view) {
        Log.i(">>>>", "post author ID:" + post.getAuthorId());
        if (post.getAuthorId().equals(MainActivity.currentUser.getUid())) {
            Toast.makeText(view.getContext(), "Cannot chat with yourself", Toast.LENGTH_SHORT).show();
        } else {
            final FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("friends")
                    .whereEqualTo("personAId", MainActivity.currentUser.getUid())
                    .whereEqualTo("personBId", post.getAuthorId())
                    .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(view.getContext(), "You have added this user", Toast.LENGTH_SHORT).show();

                        String myname;
                        String hisname;
                        Contact clicking = new Contact();
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            myname = doc.getString("nameAToB");
                            hisname = doc.getString("nameBToA");
                            clicking.setUid(post.getAuthorId());
                            clicking.setMyname(myname);
                            clicking.setContactName(hisname);
                        }

                        Serializable clickingContact = clicking;
                        Intent intent = new Intent(view.getContext(), ChatWindow.class);
                        intent.putExtra("contact", clickingContact);
                        startActivity(view.getContext(), intent, null);

                    } else {
                        db.collection("friends")
                                .whereEqualTo("personBId", MainActivity.currentUser.getUid())
                                .whereEqualTo("personAId", post.getAuthorId())
                                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                                    Toast.makeText(view.getContext(), "You have added this user", Toast.LENGTH_SHORT).show();

                                    String myname;
                                    String hisname;
                                    Contact clicking = new Contact();
                                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                        myname = doc.getString("nameBToA");
                                        hisname = doc.getString("nameAToB");
                                        clicking.setUid(post.getAuthorId());
                                        clicking.setMyname(myname);
                                        clicking.setContactName(hisname);
                                    }

                                    Serializable clickingContact = clicking;
                                    Intent intent = new Intent(view.getContext(), ChatWindow.class);
                                    intent.putExtra("contact", clickingContact);
                                    startActivity(view.getContext(), intent, null);
                                } else {
                                    addcontact(post.getAuthorId(), view);
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    private void addcontact(String uid, View view) {
        Intent intent = new Intent(view.getContext(), AddContact.class);
        intent.putExtra("uid", uid);
        view.getContext().startActivity(intent);
    }
}
