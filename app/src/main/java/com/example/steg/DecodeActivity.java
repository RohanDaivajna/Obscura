package com.example.steg;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import java.io.IOException;

public class DecodeActivity extends AppCompatActivity {

    // Request code for selecting an encoded image from the gallery
    private static final int SELECT_ENCODED_IMAGE_REQUEST = 2;
    private ImageView encodedImagePreview; // ImageView to preview the selected encoded image
    private TextView decodedMessageOutput; // TextView to display the decoded message
    private Button selectEncodedImageButton, decodeButton; // Buttons to select and decode the image
    private Bitmap encodedImage; // Bitmap to store the selected encoded image

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decode);

        // Initialize UI elements
        encodedImagePreview = findViewById(R.id.encoded_image_preview);
        decodedMessageOutput = findViewById(R.id.message_output);
        selectEncodedImageButton = findViewById(R.id.select_encoded_image_button);
        decodeButton = findViewById(R.id.decode_button);

        // Set up the listener for the "Select Encoded Image" button
        selectEncodedImageButton.setOnClickListener(v -> {
            // Create an intent to pick an image from the gallery
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, SELECT_ENCODED_IMAGE_REQUEST);
        });

        // Set up the listener for the "Decode" button
        decodeButton.setOnClickListener(v -> {
            if (encodedImage != null) {
                // Decode the hidden message from the selected image
                String decodedMessage = decodeMessageFromImage(encodedImage);
                decodedMessageOutput.setText(decodedMessage); // Display the decoded message
            } else {
                Toast.makeText(this, "Please select an encoded image.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_ENCODED_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            // Retrieve the URI of the selected image
            Uri selectedImageUri = data.getData();
            try {
                // Convert the URI into a Bitmap and set it as the preview
                encodedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                encodedImagePreview.setImageBitmap(encodedImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Method to decode the hidden message from the encoded image
    private String decodeMessageFromImage(Bitmap image) {
        // Get the width and height of the image and retrieve all pixels
        int width = image.getWidth();
        int height = image.getHeight();
        int[] pixels = new int[width * height];
        image.getPixels(pixels, 0, width, 0, 0, width, height);

        // Extract the length of the hidden message (first 32 bits)
        int textLength = 0;
        for (int i = 0; i < 32; i++) {
            int bit = pixels[i] & 1; // Get the least significant bit of each pixel
            textLength = (textLength << 1) | bit; // Append bit to text length
        }

        // Check for validity of the text length
        if (textLength <= 0 || textLength * 8 + 32 > pixels.length) {
            Toast.makeText(this, "Invalid or corrupted hidden message", Toast.LENGTH_LONG).show();
            return "Error decoding message";
        }

        // Decode the hidden message using the retrieved length
        byte[] textBytes = new byte[textLength];
        for (int i = 0; i < textLength * 8; i++) {
            int bit = pixels[i + 32] & 1; // Get the least significant bit of each pixel
            textBytes[i / 8] = (byte) ((textBytes[i / 8] << 1) | bit); // Append the bit to the byte array
        }

        // Convert the byte array to a string and return the decoded message
        return new String(textBytes);
    }
}
