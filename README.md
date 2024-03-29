# ChatApp

ChatApp is a secure messaging application developed for Android devices. It enables users to exchange encrypted messages in real-time, ensuring the privacy and confidentiality of their conversations.

## Features

- **End-to-End Encryption**: Messages are encrypted using AES encryption, providing a high level of security.
- **Real-time Communication**: Enjoy seamless conversations with real-time message delivery.
- **User Authentication**: Securely log in and authenticate users using Firebase Authentication.
- **User Profiles**: Set up user profiles with usernames and profile pictures.
- **Push Notifications**: Receive push notifications for new messages, even when the app is in the background.
- **Message Editing and Deleting**: Users can edit and delete their sent messages for better control over their conversations.

## Architecture

### Backend

- **Firebase Realtime Database**: Stores message data and user information securely in the cloud.
- **Firebase Authentication**: Handles user authentication and authorization for secure access.
- **Firebase Cloud Messaging (FCM)**: Sends push notifications to users for new messages.

### Frontend

- **Android (Java)**: The frontend is built using Android SDK, with Java as the primary programming language.
- **MVVM Architecture**: Follows the Model-View-ViewModel architectural pattern for better code organization and maintainability.
- **RecyclerView**: Utilizes RecyclerView to display messages efficiently in a list format.
- **AES Encryption**: Implements Advanced Encryption Standard (AES) algorithm for message encryption, ensuring data security.

## Getting Started

To get started with ChatApp, follow these steps:

1. Clone the repository to your local machine:

   ```bash
   git clone https://github.com/yourusername/ChatApp.git
