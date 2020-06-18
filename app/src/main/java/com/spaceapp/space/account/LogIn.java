package com.spaceapp.space.account;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.spaceapp.space.MainActivity;
import com.spaceapp.space.R;

import static com.spaceapp.space.MainActivity.mAuth;

public class LogIn extends AppCompatActivity {

    private EditText signinEmail;
    private EditText signinPassword;
    private Button signin;
    private Button signup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        signinEmail = (EditText) findViewById(R.id.signin_email);
        signinPassword = (EditText) findViewById(R.id.signin_password);

        signin = (Button) findViewById(R.id.signin_button);
        signup = (Button) findViewById(R.id.signin_signup);

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LogIn.this, SignUp.class);
                startActivity(intent);
            }
        });

        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = signinEmail.getText().toString();
                String password = signinPassword.getText().toString();
                signIn(email, password);
            }
        });

    }

    private void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            MainActivity.currentUser = mAuth.getCurrentUser();
                            Intent intent = new Intent(LogIn.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } else {
                            Toast.makeText(LogIn.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}