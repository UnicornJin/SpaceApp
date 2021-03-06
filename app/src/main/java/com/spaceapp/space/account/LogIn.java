package com.spaceapp.space.account;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.spaceapp.space.MainActivity;
import com.spaceapp.space.R;

import static com.spaceapp.space.MainActivity.mAuth;
import static com.spaceapp.space.MainActivity.mGoogleSignInClient;

/**
 * This class focus on LogIn tasks.
 */
public class LogIn extends AppCompatActivity {

    private static final int RC_SIGN_IN = 11;
    private EditText signinEmail;
    private EditText signinPassword;
    private Button signin;
    private Button signup;
    private SignInButton googleSignIn;

    /**
     * This method will load the layout and then sign following behaviors for each button.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Load the layout file
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        signinEmail = (EditText) findViewById(R.id.signin_email);
        signinPassword = (EditText) findViewById(R.id.signin_password);

        signin = (Button) findViewById(R.id.signin_button);
        signup = (Button) findViewById(R.id.signin_signup);
        googleSignIn = (SignInButton) findViewById(R.id.google_sign_in);

        //If user tap on Sign up button, then we execute sign up activity
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LogIn.this, SignUp.class);
                startActivity(intent);
            }
        });

        //If the user decided to log in, we execute signIn method with his email and password.
        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = signinEmail.getText().toString();
                String password = signinPassword.getText().toString();
                signIn(email, password);
            }
        });

        //If the user decided to log in with his google account,
        // we will start the google sign in process.
        googleSignIn.setSize(SignInButton.SIZE_STANDARD);
        googleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithGoogle();
            }
        });

    }

    //This method will make Space log in with the user's email and password
    private void signIn(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(LogIn.this,
                    "Something wrong with your email or password.", Toast.LENGTH_SHORT).show();
        } else {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                MainActivity.currentUser = mAuth.getCurrentUser();
                                Intent intent = new Intent(LogIn.this, MainActivity.class);
                                intent.setFlags(
                                        Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            } else {
                                Toast.makeText(LogIn.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    /**
     * This method is the start of google sign in
     * It will call the system to start a google sign in activity
     * and receive result from the activity.
     */
    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    /**
     * After getting the result, we know the google account of the user.
     * Then we log this account in using firebase auth
     *
     * These parameters are all data needed for correctly distinguish result and using them
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(">>>>>:", "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.w(">>>>>>", "Google sign in failed", e);
            }
        }
    }

    /**
     * This method is for Auth of google account.
     * @param idToken
     */
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(">>>>:", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Intent intent = new Intent(LogIn.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } else {
                            Log.w(">>>>:", "signInWithCredential:failure", task.getException());
                            Toast.makeText(LogIn.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}