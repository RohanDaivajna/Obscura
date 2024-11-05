package com.example.steg;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {
    // Firebase Authentication and Firestore instances
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // UI elements for email, password, username, sign-up button, and login prompt
    private EditText editTextEmail, editTextPassword, editTextUsername;
    private Button buttonSignUp;
    private TextView loginPrompt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up); // Set the layout for this activity

        // Initialize Firebase Authentication and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Link UI components to XML layout IDs
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        editTextUsername = findViewById(R.id.username);
        buttonSignUp = findViewById(R.id.signupButton);
        loginPrompt = findViewById(R.id.loginPrompt);

        // Handle sign-up button click
        buttonSignUp.setOnClickListener(v -> signUpUser()); // Calls signUpUser method

        // Handle login prompt click
        loginPrompt.setOnClickListener(v -> navigateToLogin()); // Redirects to LoginActivity on click
    }

    // Method to register a new user
    private void signUpUser() {
        // Get input values from email, password, and username fields
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String username = editTextUsername.getText().toString().trim();

        // Validate the input fields
        if (!validateInputs(email, password, username)) {
            return; // Exit if validation fails
        }

        // Create a new user with email and password
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // On successful sign-up, get the current Firebase user
                        FirebaseUser user = mAuth.getCurrentUser();
                        // Save user data to Firestore
                        saveUserData(user, username);
                        Toast.makeText(SignUpActivity.this, "Sign-up successful!", Toast.LENGTH_SHORT).show();
                        // Redirect to MainActivity
                        navigateToMainActivity();
                    } else {
                        // Handle sign-up failure and display error message
                        Toast.makeText(SignUpActivity.this, "Sign-up failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    // Method to validate the user's input
    private boolean validateInputs(String email, String password, String username) {
        if (username.isEmpty()) {
            editTextUsername.setError("Username is required");
            editTextUsername.requestFocus();
            return false;
        }

        if (email.isEmpty()) {
            editTextEmail.setError("Email is required");
            editTextEmail.requestFocus();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Please enter a valid email");
            editTextEmail.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            editTextPassword.setError("Password is required");
            editTextPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            editTextPassword.setError("Password must be at least 6 characters long");
            editTextPassword.requestFocus();
            return false;
        }

        return true;
    }

    // Method to save user data in Firestore
    private void saveUserData(FirebaseUser user, String username) {
        // Prepare user data to be stored in Firestore
        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", user.getUid());
        userData.put("email", user.getEmail());
        userData.put("username", username);

        // Save the user data to the "users" collection in Firestore with document ID as user's UID
        db.collection("users").document(user.getUid())
                .set(userData)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "User data saved successfully"))
                .addOnFailureListener(e -> Log.e("Firestore", "Error saving user data", e));
    }

    // Method to navigate to MainActivity after successful sign-up
    private void navigateToMainActivity() {
        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Clear back stack
        startActivity(intent);
        finish(); // Close SignUpActivity
    }

    // Method to navigate to LoginActivity when login prompt is clicked
    private void navigateToLogin() {
        Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
        startActivity(intent);
        finish(); // Close SignUpActivity to prevent returning to it
    }
}
