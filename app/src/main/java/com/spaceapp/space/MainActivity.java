package com.spaceapp.space;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.spaceapp.space.account.LogIn;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;


/**
 * Hi This is Yuze. Welcome to read Space App source code.
 *
 * This class below is our Main Activity.
 */
public class MainActivity extends AppCompatActivity {

    public static FirebaseAuth mAuth;
    public static FirebaseUser currentUser;
    public static GoogleSignInAccount googleAccount;

    public static GoogleSignInClient mGoogleSignInClient;

    /**
     * This method is the most important.
     * When you open our App, After the Splash page, this method will be executed
     * and finish the basic set up.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();

        //This variable is for implement the Google Signin function
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("767896223866-ssn52dc3bfkamrjpp5grjui0l5dq0vva.apps.googleusercontent.com")
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        //This "currentUser" is the storage of current user, other class may need this later.

        currentUser = mAuth.getCurrentUser();

        //If currentUser is null, which means now no user logged in, then start logging in process
        if (currentUser == null) {
            Log.i(">>>>", "here");
            //Check whether there is a google account logged in before
            googleAccount = GoogleSignIn.getLastSignedInAccount(this);
            if (googleAccount == null) {
                // No logged in Google Account also, start the LogIn activity and clean activity stack.
                Intent intent = new Intent(this, LogIn.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else {
                //There has been a google account, then we will login with this google account.
                firebaseAuthWithGoogle(googleAccount.getIdToken());
            }
        } else { //If there have been a signed in user, Space will load the Home page.
            setContentView(R.layout.activity_main);
            BottomNavigationView navView = findViewById(R.id.nav_view);
            // Passing each menu ID as a set of Ids because each
            // menu should be considered as top level destinations.
            AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.navigation_message, R.id.navigation_plaza, R.id.navigation_mine)
                    .build();
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
            NavigationUI.setupWithNavController(navView, navController);
        }
    }

    /**
     * This method is concentrated on solving google signin problem.
     * @param idToken the id of google account
     */
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(">>>>>", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            MainActivity.currentUser = user;
                            MainActivity.this.recreate();
                        } else {
                            Log.w(">>>>>>>", "signInWithCredential:failure", task.getException());
                            Intent intent = new Intent(MainActivity.this, LogIn.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    }
                });
    }

}