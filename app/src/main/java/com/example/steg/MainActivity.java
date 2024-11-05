package com.example.steg;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    // Firebase authentication instance
    private FirebaseAuth mAuth;

    // SharedPreferences keys for saving user settings
    private static final String PREFS_NAME = "MyPrefs";
    private static final String KEY_SHOW_WARNING = "show_warning";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Sets the layout file for the activity

        mAuth = FirebaseAuth.getInstance(); // Initialize Firebase authentication instance

        // Check if the user is logged in
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            // If user is not signed in, navigate to LoginActivity
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish(); // Close MainActivity to prevent returning to it without signing in
        }

        // Show a warning dialog if the user hasn't opted out of it
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean showWarning = preferences.getBoolean(KEY_SHOW_WARNING, true);
        if (showWarning) {
            showWarningDialog(); // Display warning dialog
        }

        // Set up onClickListener for the "Encode Text" button
        findViewById(R.id.encodeTextButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start EncodeActivity when the Encode Text button is clicked
                startActivity(new Intent(MainActivity.this, EncodeActivity.class));
            }
        });

        // Set up onClickListener for the "Decode Text" button
        findViewById(R.id.decodeTextButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start DecodeActivity when the Decode Text button is clicked
                startActivity(new Intent(MainActivity.this, DecodeActivity.class));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu); // Inflate the menu XML file into the app
        Log.d("MainActivity", "Menu created"); // Log statement for debugging
        return true; // Return true to display the menu
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle actions based on the selected menu item
        if (item.getItemId() == R.id.action_profile) {
            // If "Profile" is selected, navigate to ProfileActivity if logged in, or LoginActivity if not
            if (mAuth.getCurrentUser() != null) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            } else {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
            return true;
        } else if (item.getItemId() == R.id.action_login) {
            // Navigate to LoginActivity if "Login" is selected
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            return true;
        } else if (item.getItemId() == R.id.action_signup) {
            // Navigate to SignUpActivity if "Sign Up" is selected
            startActivity(new Intent(MainActivity.this, SignUpActivity.class));
            return true;
        } else if (item.getItemId() == R.id.action_sign_out) {
            // Call signOutUser method to sign out if "Sign Out" is selected
            signOutUser();
            return true;
        } else {
            return super.onOptionsItemSelected(item); // Call super for unhandled menu items
        }
    }

    private void signOutUser() {
        mAuth.signOut(); // Sign out the user from Firebase
        Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show(); // Show sign-out message
        startActivity(new Intent(MainActivity.this, LoginActivity.class)); // Redirect to LoginActivity
        finish(); // Close MainActivity to prevent returning to it after signing out
    }

    // Method to show the warning dialog about sharing encoded images
    private void showWarningDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this); // Create a dialog builder
        builder.setTitle("Warning") // Set dialog title
                .setMessage("Sharing encoded images directly through social media platforms like WhatsApp leads to data loss. Try to share the images as a document.")
                .setCancelable(false) // Prevent dismissing the dialog by tapping outside it
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss()) // Close dialog on "OK"
                .setNegativeButton("Don't Show Again", (dialog, which) -> {
                    // Save the user's choice in SharedPreferences
                    SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean(KEY_SHOW_WARNING, false); // Save that user doesn't want the warning
                    editor.apply();
                    dialog.dismiss();
                });

        AlertDialog alertDialog = builder.create(); // Create the dialog
        alertDialog.show(); // Show the dialog to the user
    }
}
