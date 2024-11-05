package com.example.steg;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private TextView textViewEmail, textViewUsername;
    private Button buttonLogout;
    private ImageView imageViewProfilePic;

    private Uri imageUri; // Holds the selected image URI

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase Auth, Firestore, and Storage
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        // Initialize UI elements
        textViewEmail = findViewById(R.id.textViewEmail);
        textViewUsername = findViewById(R.id.textViewUsername);
        buttonLogout = findViewById(R.id.buttonLogout);
        imageViewProfilePic = findViewById(R.id.imageViewProfilePic);

        // Get the current user
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            textViewEmail.setText(user.getEmail());
            loadUserProfile(user.getUid());
        }

        // Set click listener for logout button
        buttonLogout.setOnClickListener(v -> logoutUser());

        // Set click listener for profile picture to allow image upload
        imageViewProfilePic.setOnClickListener(v -> openFileChooser());
    }

    // Opens the file chooser for selecting an image
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            Log.d("ProfileActivity", "Selected image URI: " + imageUri.toString());
            uploadProfileImage();
        }
    }

    // Uploads the profile image to Firebase Storage
    private void uploadProfileImage() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User is not logged in.", Toast.LENGTH_SHORT).show();
            return; // Exit if user is not logged in
        }

        if (imageUri != null) {
            String uid = user.getUid();
            StorageReference fileRef = storageRef.child("profileImages/" + uid + ".jpg");

            fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                // Get the download URL and update Firestore
                fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    updateProfileImageUrl(uid, imageUrl);
                    Glide.with(this).load(imageUrl).circleCrop().into(imageViewProfilePic);
                    Toast.makeText(ProfileActivity.this, "Profile Image Updated", Toast.LENGTH_SHORT).show();
                });
            }).addOnFailureListener(e -> {
                Log.e("ProfileActivity", "Failed to upload image: " + e.getMessage());
                Toast.makeText(ProfileActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    // Updates the profile image URL in Firestore
    private void updateProfileImageUrl(String uid, String imageUrl) {
        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("profileImageUrl", imageUrl);

        db.collection("users").document(uid)
                .set(userUpdates, SetOptions.merge())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ProfileActivity.this, "Profile image URL updated", Toast.LENGTH_SHORT).show();
                    } else {
                        // Log the error for debugging
                        Log.e("ProfileActivity", "Failed to update profile: " + task.getException());
                        Toast.makeText(ProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    // Logout method
    private void logoutUser() {
        mAuth.signOut();
        Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
        finish();
    }

    // Method to load user profile data from Firestore
    private void loadUserProfile(String uid) {
        db.collection("users").document(uid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        if (task.getResult().exists()) {
                            String username = task.getResult().getString("username");
                            String profileImageUrl = task.getResult().getString("profileImageUrl");

                            // Log the retrieved values for debugging
                            Log.d("ProfileActivity", "Username: " + username);
                            Log.d("ProfileActivity", "Profile Image URL: " + profileImageUrl);

                            textViewUsername.setText(username != null ? username : "No username found");

                            // Load the profile image using Glide (if exists)
                            if (profileImageUrl != null) {
                                Glide.with(this).load(profileImageUrl).circleCrop().into(imageViewProfilePic);
                            } else {
                                imageViewProfilePic.setImageResource(R.drawable.default_profile_pic);
                            }
                        } else {
                            Toast.makeText(ProfileActivity.this, "No user profile found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ProfileActivity.this, "Error getting user profile", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
