# Obscura

**Obscura** is a steganography Android application that enables users to hide messages within images and audio files. Utilizing encryption and encoding techniques, the app ensures data security and privacy for message transmission.

**Features**

- **Image Steganography**: Encode and decode secret messages within images.
- **Audio Steganography**: Encode and decode text within audio files for secure messaging.
- **Firebase Authentication**: User login and session management.
- **User-Friendly Interface**: Simple navigation for encoding and decoding processes.
- **File Sharing**: Share images securely across authenticated users.
- **Secure Storage**: Save encoded files to internal storage for later retrieval and decoding.

**Installation**

1. Clone the repository to your local machine.

git clone https://github.com/username/Obscura.git

1. Open the project in Android Studio.
1. Connect your Firebase project and add the necessary google-services.json.
1. Build and run the project on your Android device.

**Usage**

1. **Login**: Authenticate using Firebase credentials.
1. **Main Screen**: Choose between Encode and Decode options.
1. **Encoding**:
   1. Select an image or audio file.
   1. Input your secret message.
   1. Save the encoded file to your device.
1. **Decoding**: Choose a previously encoded file to retrieve the hidden message.
1. **Sign Out**: Click the sign-out button to end the session.

**Requirements**

- **Android SDK** 21 or higher
- **Firebase Authentication** set up in the Firebase Console
- Internet connection for authentication

**Technologies Used**

- **Java/Kotlin** for Android development
- **Firebase** for user authentication and data storage
- **Steganography** for secure message encoding and decoding

**License**

This project is licensed under the MIT License.

