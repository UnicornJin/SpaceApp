package com.spaceapp.space.ui.plaza;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.spaceapp.space.R;
import com.spaceapp.space.post.Post;
import com.spaceapp.space.post.PostAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PlazaFragment extends Fragment {

    List<Post> plazaPostList = new ArrayList<>();

    RecyclerView plazaPosts;

    SwipeRefreshLayout plazaRefresh;

    View thisView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_plaza, container, false);

        plazaPosts = (RecyclerView) root.findViewById(R.id.plaza_posts);

        plazaRefresh = (SwipeRefreshLayout) root.findViewById(R.id.plaza_swipeRefreshLayout);
        plazaRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                onResume();
                plazaRefresh.setRefreshing(false);
            }
        });

        thisView = root;
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        plazaPostList.clear();

        LinearLayoutManager manager = new LinearLayoutManager(thisView.getContext());
        plazaPosts.setLayoutManager(manager);
        final PostAdapter adapter = new PostAdapter(plazaPostList);
        plazaPosts.setAdapter(adapter);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("POSTPLAZA")
                .orderBy("time", Query.Direction.DESCENDING)
                .limit(10)
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
                                            .child(doc.getString("author"))
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
                                                    plazaPostList.add(tempWithImage);
                                                    Log.i(">>>>>>", "Added Post:"+tempWithImage.toString());
                                                    Collections.sort(plazaPostList, new Comparator<Post>() {
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
                                    Post temp = new Post(
                                            doc.getString("author"),
                                            doc.getString("content"),
                                            doc.getTimestamp("time"),
                                            doc.getString("title")
                                    );
                                    plazaPostList.add(temp);
                                    Collections.sort(plazaPostList, new Comparator<Post>() {
                                        @Override
                                        public int compare(Post o1, Post o2) {
                                            return (-1) * o1.getTime().compareTo(o2.getTime());
                                        }
                                    });
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        }
                    }
                });
    }
}