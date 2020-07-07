package com.spaceapp.space.post;

import android.content.Intent;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.spaceapp.space.MainActivity;
import com.spaceapp.space.R;
import com.spaceapp.space.account.AddContact;

import java.util.List;

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

        if (post.isWithImage()) {
            holder.postTitle.setText(post.getTitle());
            holder.postContent.setText(post.getContent());
            holder.postImage.setVisibility(View.VISIBLE);
            holder.postImage.setMaxHeight(250);
            holder.postTime.setText(post.getTimeString());

            Log.i(">>>>>>", "post" + post.toString());

            holder.postImage.setImageURI(post.getImageUri());
        } else {
            holder.postTitle.setText(post.getTitle());
            holder.postContent.setText(post.getContent());
            holder.postImage.setVisibility(View.GONE);
            holder.postTime.setText(post.getTimeString());
        }

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
        if (post.getAuthor() == MainActivity.currentUser.getUid()) {
            Toast.makeText(view.getContext(), "Cannot chat with yourself", Toast.LENGTH_SHORT).show();
        } else {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("USERDATA")
                    .document(MainActivity.currentUser.getUid())
                    .collection("CONTACTS")
                    .document(post.getAuthor())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.getResult().exists()) {
                                Toast.makeText(view.getContext(), "You have added this user", Toast.LENGTH_SHORT).show();
                            } else {
                                addcontact(post.getAuthor(), view);
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
