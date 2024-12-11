package com.example.whatsapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.whatsapp.Models.Users;
import com.example.whatsapp.databinding.ActivitySignInBinding;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import es.dmoral.toasty.Toasty;

public class SignIn extends AppCompatActivity {

    ActivitySignInBinding binding;
    private FirebaseAuth auth;
    FirebaseDatabase database;
    AlertDialog progressDialog;
    GoogleSignInClient mGoogleSignInClient;
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;
    private static final int REQ_ONE_TAP = 2;  // Can be any integer unique to the Activity.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        progressDialog = new AlertDialog.Builder(this)
                .setView(new ProgressBar(this))
                .setTitle("Login")
                .setMessage("Login to your account")
                .setCancelable(false) // So that it is not dismissed on back button press
                .create();

        // Initialize the one-tap sign-in client
//        oneTapClient = Identity.getSignInClient(this);

        // Build the sign-in request
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        binding.btnGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkAvailable()) {
                    Toasty.info(getApplicationContext(), "Signing in with Google...", Toast.LENGTH_SHORT, true).show();
                    signIn();
                } else {
                    showNoInternetDialog();
                }
            }
        });

        binding.btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkAvailable()) {
                    String email = binding.etEmailIn.getText().toString().trim();
                    String password = binding.etPassIn.getText().toString().trim();

                    if (email.isEmpty() || password.isEmpty()) {
                        Toasty.warning(getApplicationContext(), "Please fill in all fields.", Toast.LENGTH_SHORT, true).show();
                    } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        Toasty.warning(getApplicationContext(), "Invalid email format.", Toast.LENGTH_SHORT, true).show();
                    } else if (password.length() < 6) { // Assuming a minimum password length of 6
                        Toasty.warning(getApplicationContext(), "Password must be at least 6 characters.", Toast.LENGTH_SHORT, true).show();
                    } else {
                        progressDialog.show();
                        auth.signInWithEmailAndPassword(binding.etEmailIn.getText().toString(), binding.etPassIn.getText().toString())
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            progressDialog.dismiss();
                                            Toasty.success(getApplicationContext(), "Sign in successful!", Toast.LENGTH_SHORT, true).show();
                                            Intent intent = new Intent(SignIn.this, MainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            progressDialog.dismiss();
//                                            Toast.makeText(SignIn.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            Toasty.error(getApplicationContext(), "Invalid Credentials !!!", Toast.LENGTH_SHORT, true).show();
                                        }
                                    }
                                });
                    }
                } else {
                    showNoInternetDialog();
                }
            }
        });

        binding.tvClickSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toasty.info(getApplicationContext(), "Redirecting to Sign Up...", Toast.LENGTH_SHORT, true).show();
                Intent intent = new Intent(SignIn.this, SignUp.class);
                startActivity(intent);
                finish();
            }
        });

        if (auth.getCurrentUser() != null) {
//            Toasty.info(getApplicationContext(), "User already signed in. Redirecting to Main Activity...", Toast.LENGTH_SHORT, true).show();
            Intent intent = new Intent(SignIn.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void signIn() {
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            // After sign out, launch the sign-in intent
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            signInLauncher.launch(signInIntent);
        });
    }

    // Method to check if the network is available
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo() != null &&
                connectivityManager.getActiveNetworkInfo().isConnected();
    }

    // Method to show a dialog if there is no internet connection
    private void showNoInternetDialog() {
        Toasty.error(getApplicationContext(), "No internet connection. Please check your connection and try again.", Toast.LENGTH_LONG, true).show();
        new AlertDialog.Builder(this)
                .setTitle("No Internet Connection")
                .setMessage("Please check your internet connection and try again.")
                .setPositiveButton("OK", null)
                .show();
    }

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        Log.d("TAG", "firebaseAuthWithGoogle: " + account.getId());
                        firebaseAuthWithGoogle(account.getIdToken());
                        Toasty.success(getApplicationContext(), "Sign in with Google!", Toast.LENGTH_SHORT, true).show();
                    } catch (ApiException e) {
                        Log.w("TAG", "Google SignIn Failed", e);
                        Toasty.error(getApplicationContext(), "Google Sign-In Failed.", Toast.LENGTH_SHORT, true).show();
                    }
                }
            }
    );

    private void firebaseAuthWithGoogle(String idToken) {
        Log.d("MainActivity", "firebaseAuthWithGoogle:" + idToken);

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("TAG", "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w("TAG", "signInWithCredential", task.getException());
                            Toasty.error(SignIn.this, "Authentication failed.", Toast.LENGTH_SHORT, true).show();
                        } else {
                            FirebaseUser user = auth.getCurrentUser();
                            if (user != null) {
                                Users users = new Users(binding.etEmailIn.getText().toString(), binding.etPassIn.getText().toString(), null);
                                users.setUserId(user.getUid());

                                String userName = user.getDisplayName() != null ? user.getDisplayName() : "Anonymous";
                                String email = user.getEmail() != null ? user.getEmail() : "Defaul@gmail.com";
                                String profilePic = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "default_profile_pic_url";

                                users.setUserName(userName);
                                users.setMail(email);
                                users.setProfilePic(profilePic);
                                database.getReference().child("Users").child(user.getUid()).setValue(users);
                            }
                            Toasty.success(getApplicationContext(), "Sign in with Google successful!", Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(SignIn.this, MainActivity.class));
                            finish();
                        }
                    }
                });
    }
}
