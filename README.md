Overview
ChatApp is a messaging application that allows users to send and receive encrypted messages securely. It provides real-time communication between users and ensures the privacy of conversations through encryption. This documentation provides an overview of the features, architecture, setup instructions, and usage guidelines for ChatApp.
Features
•	Secure Messaging: All messages are encrypted using AES encryption, ensuring that only the sender and receiver can read the contents.
•	Real-time Communication: Messages are delivered in real-time, allowing users to have seamless conversations.
•	User Authentication: Users can create accounts and log in securely using Firebase Authentication.
•	User Profiles: Users can set up profiles with usernames and profile pictures.
•	Notifications: Users receive notifications for new messages even when the app is in the background.
•	Message Editing and Deleting: Users can edit and delete messages they have sent.
Architecture
Backend
•	Firebase Realtime Database: Stores message data and user information securely.
•	Firebase Authentication: Handles user authentication and authorization.
•	Firebase Cloud Messaging (FCM): Sends push notifications to users for new messages.
Frontend
•	Android (Java): The frontend is developed using Android SDK, utilizing Java for programming.
•	MVVM Architecture: The app follows the Model-View-ViewModel architectural pattern for better separation of concerns and maintainability.
•	RecyclerView: Messages are displayed using RecyclerView for efficient rendering of message lists.
•	AES Encryption: Messages are encrypted using Advanced Encryption Standard (AES) algorithm for secure communication.
Setup Instructions
1.	Clone the repository from GitHub: git clone https://github.com/yourusername/ChatApp.git
2.	Open the project in Android Studio.
3.	Connect the project to Firebase by following the Firebase setup instructions.
4.	Run the app on an Android device or emulator.
Usage
1.	Register or log in to your account.
2.	Start a conversation by selecting a user from your contacts.
3.	Send messages by typing in the message input field and tapping the send button.
4.	Long-press on a message to access options such as editing or deleting it.
5.	Receive real-time notifications for new messages.
Dependencies
•	Firebase SDK
•	Picasso for image loading
•	AESUtils for encryption and decryption
Contributing
Contributions to ChatApp are welcome! If you find any bugs or have suggestions for improvements, please open an issue or submit a pull request on GitHub.
License
This project is licensed under the MIT License.

