package com.spaceapp.space.post;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.spaceapp.space.R;

public class ModifyPost extends AppCompatActivity {

    private EditText modTitle;
    private EditText modContent;
    private Button modPhoto;
    private Button mod;
    private Button cancel;
    private ImageView imageChosen;

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

        Intent intent = getIntent();
        modTitle.setText(intent.getStringExtra("post_title"));
        modContent.setText(intent.getStringExtra("post_content"));
        imageChosen.setImageURI(Uri.parse(intent.getStringExtra("post_image")));

        this.modPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        this.mod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        this.cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }
}