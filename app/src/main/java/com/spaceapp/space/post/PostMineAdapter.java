package com.spaceapp.space.post;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.spaceapp.space.R;
import com.spaceapp.space.ui.mine.MineFragment;

import java.util.List;

import static androidx.core.content.ContextCompat.startActivity;

/**
 * This Adapter helps to show a post in Mine page.
 *
 * It has modify and delete function.
 */
public class PostMineAdapter extends RecyclerView.Adapter<PostMineAdapter.ViewHolder>{
    private List<Post> mPostList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View view;

        ImageView postImage;
        TextView postTitle;
        TextView postContent;
        TextView postTime;
        Button delete;
        Button modefy;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            this.postImage = (ImageView) view.findViewById(R.id.mine_post_image);
            this.postTitle = (TextView) view.findViewById(R.id.mine_post_title);
            this.postContent = (TextView) view.findViewById(R.id.mine_post_content);
            this.postTime = (TextView) view.findViewById(R.id.mine_post_time);
            this.delete = (Button) view.findViewById(R.id.delete_post);
            this.modefy = (Button) view.findViewById(R.id.modefy_post);
        }
    }

    public PostMineAdapter(List<Post> postList) {
        this.mPostList = postList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_mine_post, parent, false);
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
            holder.postImage.setImageURI(post.getImageUri());
        } else {
            holder.postTitle.setText(post.getTitle());
            holder.postContent.setText(post.getContent());
            holder.postImage.setVisibility(View.GONE);
            holder.postTime.setText(post.getTimeString());
        }

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                post.delete(holder.view);
            }
        });

        holder.modefy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                post.modefy(holder.view);
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.mPostList.size();
    }
}
