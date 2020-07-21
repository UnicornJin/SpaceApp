package com.spaceapp.space.ui.mine;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
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
import com.spaceapp.space.post.PostMineAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

/**
 * This is the personal information page of Space App
 *
 * Includes email of current user and post list.
 */
public class MineFragment extends Fragment {

    List<Post> myPostList = new ArrayList<>();

    public static View thisView;

    TextView userInfo;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        //If now the App is not logged in, it will jump to login page.
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
            //Load the email from the database
            final View root = inflater.inflate(R.layout.fragment_mine, container, false);
            thisView = root;

            userInfo = (TextView) root.findViewById(R.id.userinfo);
            userInfo.setText("Welcome to SpaceApp!\n" + MainActivity.currentUser.getEmail());

            //log out button
            ((Button) root.findViewById(R.id.mine_logout)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), LogOut.class);
                    startActivity(intent);
                }
            });

            //Tap on this button and write new post.
            ((Button) root.findViewById(R.id.mine_newPost)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), NewPost.class);
                    startActivity(intent);
                }
            });

            //swipe down the post list and refresh it.
            ((SwipeRefreshLayout) root.findViewById(R.id.mine_swipe)).setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    onResume();
                    ((SwipeRefreshLayout) root.findViewById(R.id.mine_swipe)).setRefreshing(false);
                }
            });

            return root;
        }

    }

    /**
     * load the post list from database, sort them and show them correctly.
     */
    @Override
    public void onResume() {
        super.onResume();
        if (MainActivity.currentUser == null) {

        } else {
            myPostList.clear();

            RecyclerView recyclerView = (RecyclerView) thisView.findViewById(R.id.mine_myposts);
            LinearLayoutManager manager = new LinearLayoutManager(thisView.getContext());
            recyclerView.setLayoutManager(manager);
            final PostMineAdapter adapter = new PostMineAdapter(myPostList);
            recyclerView.setAdapter(adapter);

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("posts")
                    .whereEqualTo("authorId", MainActivity.currentUser.getUid())
                    .orderBy("published", Query.Direction.DESCENDING)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.w(">>>>>", "Load my posts error:" + e);
                            }

                            myPostList.clear();

                            if (!(queryDocumentSnapshots == null) && !queryDocumentSnapshots.isEmpty()){
                                for (final QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                    try {
                                        Log.i(">>>>>>>", "doc:" + doc.toString());
                                        FirebaseStorage storage = FirebaseStorage.getInstance();
                                        StorageReference imageRef = storage.getReferenceFromUrl(doc.getString("image"));
                                        final File localTemp = File.createTempFile(doc.getTimestamp("published").toString(), ".jpg");
                                        imageRef.getFile(localTemp).addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    Post tempWithImage = new Post(
                                                            doc.getString("author"),
                                                            doc.getString("content"),
                                                            doc.getTimestamp("published"),
                                                            Uri.fromFile(localTemp).toString(),
                                                            doc.getString("title")
                                                    );
                                                    myPostList.add(tempWithImage);
                                                    Collections.sort(myPostList, new Comparator<Post>() {
                                                        @Override
                                                        public int compare(Post o1, Post o2) {
                                                            return (-1) * o1.getPublished().compareTo(o2.getPublished());
                                                        }
                                                    });
                                                    adapter.notifyDataSetChanged();
                                                }
                                            }
                                        });
                                    } catch (Exception exception) {
                                        exception.printStackTrace();
                                    }
                                }
                            } else {
                                Log.i(">>>>>:", "empty post list");
                            }
                        }
                    });
        }
    }

}