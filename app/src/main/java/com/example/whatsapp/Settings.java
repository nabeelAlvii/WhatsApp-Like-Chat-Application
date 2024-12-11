package com.example.whatsapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.example.whatsapp.Models.Users;
import com.example.whatsapp.databinding.ActivitySettingsBinding;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import es.dmoral.toasty.Toasty;

public class Settings extends AppCompatActivity {

    ActivitySettingsBinding binding;
    FirebaseStorage storage;
    FirebaseAuth auth;
    FirebaseDatabase database;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();

        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        // Define ProgressDialog
        ProgressDialog progressDialog = new ProgressDialog(Settings.this);
        progressDialog.setTitle("Uploading Profile Picture");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri sFile = result.getData().getData();
                        binding.profileImgSettings.setImageURI(sFile); // Set the selected image to the ImageView

                        String uid = auth.getUid(); // Get user UID
                        if (uid == null || uid.isEmpty()) {
                            Toasty.error(this, "User is not authenticated. Unable to upload profile picture.", Toast.LENGTH_LONG, true).show();
                            return;  // Exit if UID is null
                        }

                        final StorageReference reference = storage.getReference()
                                .child("Profile_picture")
                                .child(uid); // Use `uid` directly here

                        // Show loader when upload starts
                        progressDialog.show();

                        reference.putFile(sFile).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                database.getReference().child("Users")
                                                        .child(uid) // Use `uid` directly here
                                                        .child("profilePic").setValue(uri.toString());

                                                // Dismiss loader on success
                                                progressDialog.dismiss();
                                                Toasty.success(getApplicationContext(), "ProfilePic Updated", Toast.LENGTH_SHORT, true).show();
                                            }
                                        }).addOnFailureListener(e -> {
                                            // Dismiss loader and show error if download URL fails
                                            progressDialog.dismiss();
                                            Toasty.error(getApplicationContext(), "Failed to update profile pic", Toast.LENGTH_LONG, true).show();
                                        });
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    // Dismiss loader and show error if upload fails
                                    progressDialog.dismiss();
                                    Toasty.error(getApplicationContext(), "Upload failed", Toast.LENGTH_LONG, true).show();
                                });
                    } else if (result.getResultCode() == ImagePicker.RESULT_ERROR) {
                        String errorMsg = result.getData() != null ? ImagePicker.getError(result.getData()) : "Unknown error";
                        Toasty.error(this, errorMsg, Toast.LENGTH_LONG, true).show();
                    } else {
                        Toasty.warning(this, "Task Cancelled", Toast.LENGTH_SHORT, true).show();
                    }
                }
        );

        // Ensure user is signed in before attempting to fetch or modify user data
        if (auth.getCurrentUser() == null) {
            Toasty.error(this, "User is not authenticated. Please log in.", Toast.LENGTH_SHORT, true).show();
            Intent intent = new Intent(Settings.this, SignIn.class);
            startActivity(intent);
            finish();
            return; // Exit if the user is not authenticated
        }

        loadUserProfile();

        binding.addImg.setOnClickListener(v -> {
            ImagePicker.with(Settings.this)
                    .crop(9f, 16f)    // Crop image with 16:9 aspect ratio
                    .compress(1024)   // Final image size will be less than 1 MB
                    .maxResultSize(1080, 1080)    // Final image resolution will be less than 1080 x 1080
                    .createIntent(intent -> {
                        imagePickerLauncher.launch(intent); // Launch the ImagePicker intent
                        return null;
                    });
        });

        // Clear UI fields on sign out
        binding.tvUserName.setText("");
        binding.tvAbout.setText("");
        binding.profileImgSettings.setImageResource(R.drawable.profile);

        binding.tvUserName.setOnClickListener(v -> {
            showEditDialog("Edit Username", binding.tvUserName.getText().toString(), newValue -> {
                binding.tvUserName.setText(newValue);
            });
        });

        binding.tvAbout.setOnClickListener(v -> {
            showEditDialog("Edit About", binding.tvAbout.getText().toString(), newValue -> {
                binding.tvAbout.setText(newValue);
            });
        });

        binding.saveBtn.setOnClickListener(v -> {
            String newUserName = binding.tvUserName.getText().toString();
            String newAbout = binding.tvAbout.getText().toString();

            database.getReference()
                    .child("Users")
                    .child(auth.getUid()) // Use `auth.getUid()` here
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Users currentUser = snapshot.getValue(Users.class);
                            if (currentUser == null) {
                                return;
                            }

                            String currentUserName = currentUser.getUserName();
                            String currentAbout = currentUser.getAbout();

                            boolean isUserNameChanged = !newUserName.equals(currentUserName);
                            boolean isAboutChanged = !newAbout.equals(currentAbout);

                            HashMap<String, Object> obj = new HashMap<>();
                            StringBuilder message = new StringBuilder();

                            if (isUserNameChanged) {
                                obj.put("userName", newUserName);
                                message.append("Username, ");
                            }
                            if (isAboutChanged) {
                                obj.put("about", newAbout);
                                message.append("About, ");
                            }

                            if (!obj.isEmpty()) {
                                database.getReference()
                                        .child("Users")
                                        .child(auth.getUid()) // Use `auth.getUid()` here
                                        .updateChildren(obj).addOnSuccessListener(aVoid -> {
                                            message.setLength(message.length() - 2);
                                            Toasty.success(getApplicationContext(), message.toString() + " Updated", Toast.LENGTH_SHORT, true).show();
                                        }).addOnFailureListener(e -> {
                                            Toasty.error(getApplicationContext(), "Update Failed", Toast.LENGTH_SHORT, true).show();
                                        });
                            } else {
                                Toasty.info(getApplicationContext(), "No changes to save", Toast.LENGTH_SHORT, true).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toasty.error(getApplicationContext(), "Error retrieving data", Toast.LENGTH_SHORT, true).show();
                        }
                    });
        });

        binding.leftArrow.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.this, MainActivity.class);
            startActivity(intent);
        });
    }

    private void showEditDialog(String title, String currentValue, OnValueSetListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(currentValue);
        input.setSelection(input.getText().length());
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String newValue = input.getText().toString();
            listener.onValueSet(newValue);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    public interface OnValueSetListener {
        void onValueSet(String newValue);
    }

    private void loadUserProfile() {
        String uid = auth.getUid(); // Get the UID of the authenticated user

        if (uid == null || uid.isEmpty()) {
            Toasty.error(this, "User is not authenticated. Please log in.", Toast.LENGTH_SHORT, true).show();
            Intent intent = new Intent(Settings.this, SignIn.class);
            startActivity(intent);
            finish();
            return;
        }

        database.getReference().child("Users")
                .child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Users users = snapshot.getValue(Users.class);

                        if (users != null) {
                            Picasso.get()
                                    .load(users.getProfilePic())
                                    .placeholder(R.drawable.profile)
                                    .into(binding.profileImgSettings);

                            binding.tvUserName.setText(users.getUserName());
                            binding.tvAbout.setText(users.getAbout());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("FirebaseError", "Failed to fetch user profile", error.toException());
                    }
                });
    }
}
