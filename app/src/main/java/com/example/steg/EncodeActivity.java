package com.example.steg;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class EncodeActivity extends AppCompatActivity {

    // Request codes for image selection and write permission
    private static final int SELECT_IMAGE_REQUEST = 1;
    private static final int REQUEST_WRITE_PERMISSION = 2;

    // UI elements
    private ImageView imagePreview;
    private EditText secretMessageInput;
    private Button selectImageButton, encodeButton;
    private FloatingActionButton shareButton;

    // Bitmap for selected image
    private Bitmap selectedImage;
    private Uri savedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encode);

        // Initialize UI components
        imagePreview = findViewById(R.id.image_preview);
        secretMessageInput = findViewById(R.id.secret_message_input);
        selectImageButton = findViewById(R.id.select_image_button);
        encodeButton = findViewById(R.id.encode_button);
        shareButton = findViewById(R.id.share_button);

        // Set up button listeners for selecting, encoding, and sharing an image

        // Trigger image selection from gallery
        selectImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, SELECT_IMAGE_REQUEST);
        });

        // Encode the message into the image upon clicking the encode button
        encodeButton.setOnClickListener(v -> {
            String secretMessage = secretMessageInput.getText().toString();
            if (selectedImage != null && !secretMessage.isEmpty()) {
                Bitmap encodedImage = encodeMessageIntoImage(selectedImage, secretMessage);
                if (encodedImage != null) {
                    savedImageUri = saveEncodedImageToGallery(encodedImage);
                }
            } else {
                Toast.makeText(this, "Please select an image and enter a message.", Toast.LENGTH_SHORT).show();
            }
        });

        // Share the saved encoded image using the share button
        shareButton.setOnClickListener(v -> {
            if (savedImageUri != null) {
                shareEncodedImage(savedImageUri);
            } else {
                Toast.makeText(this, "Please encode and save an image first.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Handle the result from the image selection activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            try {
                // Load selected image and display in ImageView
                selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                imagePreview.setImageBitmap(selectedImage);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Encode a message into the bitmap image using simple steganography
    private Bitmap encodeMessageIntoImage(Bitmap image, String message) {
        // Create a mutable copy of the image
        Bitmap mutableImage = image.copy(Bitmap.Config.ARGB_8888, true);
        int width = mutableImage.getWidth();
        int height = mutableImage.getHeight();
        int[] pixels = new int[width * height];
        mutableImage.getPixels(pixels, 0, width, 0, 0, width, height);

        // Convert the message to a byte array
        byte[] textBytes = message.getBytes();
        int textLength = textBytes.length;

        // Check if the image can hold the entire message
        if (textLength * 8 + 32 > pixels.length) {
            Toast.makeText(this, "Text too long to be hidden in this image.", Toast.LENGTH_LONG).show();
            return null;
        }

        // Encode the length of the message in the first 32 bits of the image pixels
        for (int i = 0; i < 32; i++) {
            int bit = (textLength >> (31 - i)) & 1;
            pixels[i] = (pixels[i] & 0xFFFFFFFE) | bit;
        }

        // Encode each bit of the message into subsequent pixels
        for (int i = 0; i < textLength * 8; i++) {
            int bit = (textBytes[i / 8] >> (7 - i % 8)) & 1;
            pixels[i + 32] = (pixels[i + 32] & 0xFFFFFFFE) | bit;
        }

        // Update image with encoded pixel data and return
        mutableImage.setPixels(pixels, 0, width, 0, 0, width, height);
        return mutableImage;
    }

    // Save the encoded image to the device's gallery
    private Uri saveEncodedImageToGallery(Bitmap encodedImage) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "encoded_image_" + System.currentTimeMillis() + ".png");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/EncodedImages");

        // Insert the image into MediaStore and save as PNG
        Uri imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (imageUri != null) {
            try {
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    encodedImage.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    byte[] imageData = baos.toByteArray();
                    try (java.io.OutputStream outputStream = getContentResolver().openOutputStream(imageUri)) {
                        outputStream.write(imageData);
                    }
                }
                Toast.makeText(this, "Encoded image saved to gallery", Toast.LENGTH_SHORT).show();
                return imageUri;
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Failed to create MediaStore entry", Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    // Share the encoded image via an external app
    private void shareEncodedImage(Uri imageUri) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/png");
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Encoded Image");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Here's an encoded image.");

        // Open sharing options (WhatsApp, Email, etc.)
        startActivity(Intent.createChooser(shareIntent, "Share encoded image via"));
    }

    // Request write permissions from the user if not already granted
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Write permission is required to save images.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
