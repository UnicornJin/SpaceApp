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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.spaceapp.space.MainActivity;
import com.spaceapp.space.R;

import java.util.HashMap;
import java.util.Map;

import static com.spaceapp.space.MainActivity.mAuth;

public class SignUp extends AppCompatActivity {

    private EditText signupEmail;
    private EditText signupPassword;
    private EditText signupConfPassword;

    private Button signupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        signupEmail = (EditText) findViewById(R.id.signup_email);
        signupPassword = (EditText) findViewById(R.id.signup_password);
        signupConfPassword = (EditText) findViewById(R.id.signup_conform_password);
        signupButton = (Button) findViewById(R.id.signup_signup);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = signupEmail.getText().toString();
                String password = signupPassword.getText().toString();
                String confpassword = signupConfPassword.getText().toString();
                if (password.equals(confpassword)) {
                    createAccount(email, password);
                } else {
                    Toast.makeText(SignUp.this,"Confirm password error.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void createAccount(final String email, String password) {

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            final FirebaseUser user = mAuth.getCurrentUser();

                            Map<String, String> data = new HashMap<>();
                            data.put("Email", email);

                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection("USERDATA").document(user.getUid())
                                    .set(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        MainActivity.currentUser = user;
                                        Intent intent = new Intent(SignUp.this, MainActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                    } else {
                                        Toast.makeText(SignUp.this, "Sign up failed.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        } else {
                            Toast.makeText(SignUp.this, "Sign up failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
