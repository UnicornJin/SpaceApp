package com.spaceapp.space.ui.mine;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.spaceapp.space.MainActivity;
import com.spaceapp.space.R;
import com.spaceapp.space.account.LogIn;
import com.spaceapp.space.account.LogOut;
import com.spaceapp.space.post.NewPost;
import com.spaceapp.space.post.Post;
import com.spaceapp.space.post.PostAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MineFragment extends Fragment {

    List<Post> myPostList = new ArrayList<>();

    private RecyclerView meRecyclerView;
    private PostAdapter adapter;

    private View thisView;

    TextView userInfo;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        if (MainActivity.currentUser == null) {
            View root = inflater.inflate(R.layout.fragment_mine_no_log_in, container, false);

            ((Button) root.findViewById(R.id.me_nologin_login)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), LogIn.class);
                    startActivity(intent);
                }
            });

            return root;
        } else {
            final View root = inflater.inflate(R.layout.fragment_mine, container, false);
            thisView = root;

            userInfo = (TextView) root.findViewById(R.id.userinfo);

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("USERDATA")
                    .document(MainActivity.currentUser.getUid())
                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot result = task.getResult();
                        if (result.exists()) {
                            String UserInfo = "Welcome to SpaceApp!\n" + result.get("Email").toString();
                            userInfo.setText(UserInfo);
                        } else {
                            Log.e(">>>>>>", "Email get error");
                        }
                    } else {
                        Log.e(">>>>>>", "Cannot get user Email.");
                    }
                }
            });



            ((Button) root.findViewById(R.id.mine_logout)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), LogOut.class);
                    startActivity(intent);
                }
            });

            ((Button) root.findViewById(R.id.mine_newPost)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), NewPost.class);
                    startActivity(intent);
                }
            });

            return root;
        }

    }

    @Override
    public void onResume() {
        if (MainActivity.currentUser == null) {

        } else {
            myPostList.clear();

            RecyclerView recyclerView = (RecyclerView) thisView.findViewById(R.id.mine_myposts);
            LinearLayoutManager manager = new LinearLayoutManager(thisView.getContext());
            recyclerView.setLayoutManager(manager);
            final PostAdapter adapter = new PostAdapter(myPostList);
            recyclerView.setAdapter(adapter);

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("USERDATA")
                    .document(MainActivity.currentUser.getUid())
                    .collection("POSTS")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (final DocumentSnapshot doc : task.getResult()) {
                                    if (doc.getBoolean("withImage")) {
                                        FirebaseStorage storage = FirebaseStorage.getInstance();
                                        StorageReference imageRef = storage.getReference()
                                                .child("userData")
                                                .child("PostImageStorage")
                                                .child(MainActivity.currentUser.getUid())
                                                .child(doc.getTimestamp("time").toString() + ".jpg");

                                        try {
                                            final File localTemp = File.createTempFile(doc.getTimestamp("time").toString(), ".jpg");
                                            imageRef.getFile(localTemp).addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        Post tempWithImage = new Post(
                                                                doc.getString("author"),
                                                                doc.getString("content"),
                                                                doc.getTimestamp("time"),
                                                                Uri.fromFile(localTemp),
                                                                doc.getString("title")
                                                        );
                                                        myPostList.add(tempWithImage);
                                                        Log.i(">>>>>>", "Added Post:"+tempWithImage.toString());
                                                        Collections.sort(myPostList, new Comparator<Post>() {
                                                            @Override
                                                            public int compare(Post o1, Post o2) {
                                                                return (-1) * o1.getTime().compareTo(o2.getTime());
                                                            }
                                                        });
                                                        adapter.notifyDataSetChanged();
                                                    }
                                                }
                                            });
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        Post tempWithoutImage = new Post(
                                                doc.getString("author"),
                                                doc.getString("content"),
                                                doc.getTimestamp("time"),
                                                doc.getString("title")
                                        );
                                        myPostList.add(tempWithoutImage);
                                        Log.i(">>>>>>", "Added Post:"+tempWithoutImage.toString());
                                        Collections.sort(myPostList, new Comparator<Post>() {
                                            @Override
                                            public int compare(Post o1, Post o2) {
                                                return (-1) * o1.getTime().compareTo(o2.getTime());
                                            }
                                        });
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                            } else {
                                Log.e(">>>>>>>", "Cannot get my Posts");
                            }
                        }
                    });
        }
        super.onResume();
    }

}