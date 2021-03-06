package com.spaceapp.space.ui.plaza;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
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
import com.spaceapp.space.post.PostPlazaAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * This is the Plaza page of Space App,
 * User can read others posts here.
 *
 * All posts will be sort according to post time.
 */
public class PlazaFragment extends Fragment {

    List<Post> plazaPostList = new ArrayList<>();

    RecyclerView plazaPosts;

    SwipeRefreshLayout plazaRefresh;

    View thisView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_plaza, container, false);

        plazaPosts = (RecyclerView) root.findViewById(R.id.plaza_posts);

        //Swipe down and refresh post list.
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

    /**
     * Load post list from database then sort and show them correctly
     */
    @Override
    public void onResume() {
        super.onResume();

        plazaPostList.clear();

        LinearLayoutManager manager = new LinearLayoutManager(thisView.getContext());
        plazaPosts.setLayoutManager(manager);
        final PostPlazaAdapter adapter = new PostPlazaAdapter(plazaPostList);
        plazaPosts.setAdapter(adapter);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("posts")
                .orderBy("published", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (final DocumentSnapshot doc : task.getResult()) {
                                FirebaseStorage storage = FirebaseStorage.getInstance();
                                StorageReference imageRef = storage.getReferenceFromUrl(doc.getString("image"));

                                try {
                                    final File localTemp = File.createTempFile(doc.getTimestamp("published").toString(), ".jpg");
                                    imageRef.getFile(localTemp).addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                Post tempWithImage = new Post(
                                                        doc.getString("author"),
                                                        doc.getString("authorId"),
                                                        doc.getString("content"),
                                                        doc.getTimestamp("published"),
                                                        Uri.fromFile(localTemp).toString(),
                                                        doc.getString("title")
                                                );
                                                plazaPostList.add(tempWithImage);
                                                Collections.sort(plazaPostList, new Comparator<Post>() {
                                                    @Override
                                                    public int compare(Post o1, Post o2) {
                                                        return (-1) * o1.getPublished().compareTo(o2.getPublished());
                                                    }
                                                });
                                                adapter.notifyDataSetChanged();
                                            }
                                        }
                                    });
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                });
    }
}