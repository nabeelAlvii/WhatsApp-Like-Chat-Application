package com.example.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.whatsapp.Models.Users;
import com.example.whatsapp.databinding.ActivitySignUpBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import es.dmoral.toasty.Toasty;

public class SignUp extends AppCompatActivity {

    ActivitySignUpBinding binding;
    private FirebaseAuth auth;
    FirebaseDatabase database;
    AlertDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        progressDialog = new AlertDialog.Builder(this)
                .setView(new ProgressBar(this))
                .setTitle("Creating Account")
                .setMessage("We're creating your account")
                .setCancelable(false) // So that it is not dismissed on back button press
                .create();

        // Toasty: Informing the user the activity is loaded
        Toasty.info(getApplicationContext(), "Welcome to Sign Up!", Toast.LENGTH_SHORT, true).show();

        binding.btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toasty: Validating input fields
                if (binding.etEmail.getText().toString().isEmpty() || binding.etPass.getText().toString().isEmpty() ||
                        binding.etName.getText().toString().isEmpty()) {
                    Toasty.warning(getApplicationContext(), "Please fill all fields!", Toast.LENGTH_SHORT, true).show();
                    return;
                }

                progressDialog.show();

                auth.createUserWithEmailAndPassword(binding.etEmail.getText().toString(), binding.etPass.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressDialog.dismiss();

                                if (task.isSuccessful()) {
                                    // User Created
                                    Users users = new Users(binding.etName.getText().toString(), binding.etEmail.getText().toString(),
                                            binding.etPass.getText().toString());
                                    String id = task.getResult().getUser().getUid();
                                    database.getReference().child("Users").child(id).setValue(users);

                                    // Toasty: Success message
                                    Toasty.success(getApplicationContext(), "User Created Successfully!", Toast.LENGTH_SHORT, true).show();

                                    // Redirect to SignIn Activity
                                    Intent intent = new Intent(SignUp.this, SignIn.class);
                                    startActivity(intent);
                                    finish();

                                } else {
                                    // Toasty: Error handling
                                    if (task.getException() != null) {
                                        String errorMessage = task.getException().getMessage();
                                        if (errorMessage != null && errorMessage.contains("email address is already in use")) {
                                            Toasty.error(getApplicationContext(), "Email is already in use by another account.", Toast.LENGTH_LONG, true).show();
                                        } else {
                                            Toasty.error(getApplicationContext(), "Sign-up failed: " + errorMessage, Toast.LENGTH_LONG, true).show();
                                        }
                                    } else {
                                        Toasty.error(getApplicationContext(), "Sign-up failed due to unknown error.", Toast.LENGTH_LONG, true).show();
                                    }
                                }
                            }
                        });
            }
        });

        binding.tvAlreadyAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toasty: User is navigating to SignIn screen
                Toasty.info(getApplicationContext(), "Redirecting to Sign In.", Toast.LENGTH_SHORT, true).show();

                Intent intent = new Intent(SignUp.this, SignIn.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
