package com.example.steg;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    // Firebase authentication instance
    private FirebaseAuth mAuth;

    // UI elements for email, password, login button, and sign-up text
    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin;
    private TextView textViewSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // Set the layout for the activity

        mAuth = FirebaseAuth.getInstance(); // Initialize Firebase authentication instance

        // Connect UI elements to their XML IDs
        editTextEmail = findViewById(R.id.username);
        editTextPassword = findViewById(R.id.password);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewSignUp = findViewById(R.id.textViewSignUp);

        // Set onClick listener for the login button
        buttonLogin.setOnClickListener(v -> loginUser()); // Calls loginUser method on click

        // Handle click on "Sign Up" text to navigate to SignUpActivity
        textViewSignUp.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, SignUpActivity.class)));
    }

    // Method to handle user login
    private void loginUser() {
        // Get and trim input from email and password fields
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Check if both fields are filled
        if (!email.isEmpty() && !password.isEmpty()) {
            // Attempt to sign in with Firebase using the provided email and password
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // If sign-in succeeds, show success message
                            Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                            // Redirect to MainActivity after successful login
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish(); // Close LoginActivity to prevent returning to it
                        } else {
                            // If sign-in fails, show error message
                            Toast.makeText(LoginActivity.this, "Login failed.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // If fields are empty, prompt the user to fill all fields
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
        }
    }
}
