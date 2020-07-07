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

/**
 * If the user do not have a Space Account and he/she wants to create one,
 * this class will help them.
 */
public class SignUp extends AppCompatActivity {

    private EditText signupEmail;
    private EditText signupPassword;
    private EditText signupConfPassword;

    private Button signupButton;

    /**
     * This method will load the layout for sign up activity.
     * And it will sign following behavior for all buttons.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        signupEmail = (EditText) findViewById(R.id.signup_email);
        signupPassword = (EditText) findViewById(R.id.signup_password);
        signupConfPassword = (EditText) findViewById(R.id.signup_conform_password);
        signupButton = (Button) findViewById(R.id.signup_signup);

        //After tapping on sign up button, program will check effectiveness of email and password
        //Then create account.
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = signupEmail.getText().toString();
                String password = signupPassword.getText().toString();
                String confpassword = signupConfPassword.getText().toString();
                if (!email.isEmpty() && password.equals(confpassword) && password.length() > 0) {
                    createAccount(email, password);
                } else {
                    Toast.makeText(SignUp.this,"Confirm password error.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    /**
     * This method is for creating account with email and password.
     * @param email email needed for creating account
     * @param password password setted for the account
     */
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
