
        package com.example.chatapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.chatapp.databinding.ActivityMessagingBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

import adapters.MessageAdapter;
import models.MessageModel;

public class MessagingActivity extends AppCompatActivity implements MessageAdapter.MessageLongClickListener {
    final ArrayList<MessageModel> msgData = new ArrayList<>();

    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    ActivityMessagingBinding activityMessagingBinding;
    public String receiverId;
    String receiverToken, senderName;
    String senderId;
    String decrypted = "";
    final MessageAdapter msgAdapter = new MessageAdapter(msgData, MessagingActivity.this);

    private MessageModel selectedMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        activityMessagingBinding = ActivityMessagingBinding.inflate(getLayoutInflater());
        setContentView(activityMessagingBinding.getRoot());

        switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
            case Configuration.UI_MODE_NIGHT_YES:
                activityMessagingBinding.parentViewgroup.setBackground(AppCompatResources.getDrawable(MessagingActivity.this, R.drawable.wpdark));
                break;
            case Configuration.UI_MODE_NIGHT_NO:
                activityMessagingBinding.parentViewgroup.setBackground(AppCompatResources.getDrawable(MessagingActivity.this, R.drawable.wplight));
                break;
        }

        senderId = firebaseAuth.getUid();

        Intent intent = getIntent();
        String uname = intent.getStringExtra("USERNAME");
        String profileImg = intent.getStringExtra("PROFILEIMAGE");
        receiverId = intent.getStringExtra("USERID");
        receiverToken = intent.getStringExtra("TOKEN");


        activityMessagingBinding.receiverName.setText(uname);
        Picasso.get().load(profileImg).fit().centerCrop()
                .error(R.drawable.user)
                .placeholder(R.drawable.user)
                .into(activityMessagingBinding.profilePicImageview);


        activityMessagingBinding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MessagingActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        msgAdapter.setMessageLongClickListener(this); // Set message long click listener
        activityMessagingBinding.msgRecyclerview.setAdapter(msgAdapter);
        activityMessagingBinding.msgRecyclerview.setLayoutManager(new LinearLayoutManager(this));


        firebaseDatabase.getReference("Users")
                .child(senderId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        senderName = dataSnapshot.child("userName").getValue().toString();
                        msgData.clear();

                        for (DataSnapshot e : dataSnapshot.child("Contacts").child(receiverId).child("Chats").getChildren()) {

                            String msg = e.child("msgText").getValue().toString();

                            try {
                                decrypted = AESUtils.decrypt(msg);
                            } catch (Exception er) {
                                er.printStackTrace();
                            }

                            msgData.add(new MessageModel(e.child("uId").getValue().toString()
                                    , decrypted
                                    , (Long) Long.valueOf(e.child("msgTime").getValue().toString())));

                        }

                        msgAdapter.notifyDataSetChanged();
                        activityMessagingBinding.msgRecyclerview.scrollToPosition(msgAdapter.getItemCount() - 1);

                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed to read value
                    }
                });


        //Messaging Mechanism
        activityMessagingBinding.sendMsgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String msg = activityMessagingBinding.typingSpace.getText().toString().trim();

                String encryptedMsg = msg;
                try {
                    encryptedMsg = AESUtils.encrypt(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                long date = new Date().getTime();

                activityMessagingBinding.typingSpace.setText("");
                final MessageModel messageModel = new MessageModel(senderId, encryptedMsg, date);

                if (!msg.isEmpty()) {
                    firebaseDatabase.getReference("Users").child(senderId).child("Contacts")
                            .child(receiverId).child("Chats").push()
                            .setValue(messageModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {

                                    activityMessagingBinding.msgRecyclerview.scrollToPosition(msgAdapter.getItemCount() - 1);

                                    FcmNotificationsSender fcmNotificationsSender = new FcmNotificationsSender(receiverToken, senderName
                                            , msg, getApplicationContext(), MessagingActivity.this);
                                    fcmNotificationsSender.SendNotifications();

                                    firebaseDatabase.getReference("Users").child(receiverId).child("Contacts").child(senderId)
                                            .child("interactionTime").setValue(date);

                                    firebaseDatabase.getReference("Users").child(senderId).child("Contacts").child(receiverId)
                                            .child("interactionTime").setValue(date);


                                    firebaseDatabase.getReference("Users").child(receiverId).child("Contacts")
                                            .child(senderId).child("Chats").push()
                                            .setValue(messageModel);

                                    firebaseDatabase.getReference("Users").child(senderId).child("Contacts").child(receiverId)
                                            .child("recentMessage").setValue(msg);

                                    firebaseDatabase.getReference("Users").child(receiverId).child("Contacts").child(senderId)
                                            .child("recentMessage").setValue(msg);
                                }
                            });
                }
            }
        });

        activityMessagingBinding.msgRecyclerview.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < oldBottom) {
                    activityMessagingBinding.msgRecyclerview.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if ((msgAdapter.getItemCount() - 1) > 1)
                                activityMessagingBinding.msgRecyclerview.smoothScrollToPosition(msgAdapter.getItemCount() - 1);
                        }
                    }, 10);
                }
            }
        });
    }

    @Override
    public void onMessageLongClick(MessageModel message) {
        // Handle long press on message
        selectedMessage = message;
        // Show options for editing and deleting the message
        showOptionsDialog();
    }

    private void showOptionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Message Options");
        builder.setItems(new CharSequence[]{"Edit", "Delete"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: // Edit
                        showEditMessageDialog();
                        break;
                    case 1: // Delete
                        deleteMessage();
                        break;
                }
            }
        });
        builder.show();
    }

    private void showEditMessageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Message");

        // Set up the input
        final EditText input = new EditText(this);
        input.setText(selectedMessage.getMsgText());
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String editedMessage = input.getText().toString().trim();
                if (!editedMessage.isEmpty()) {
                     // Update message text
                    updateMessage(selectedMessage ,editedMessage); // Update message in Firebase
                } else {
                    Toast.makeText(MessagingActivity.this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void updateMessage(MessageModel message, String editedMessage) {
        DatabaseReference senderReference = firebaseDatabase.getReference("Users")
                .child(senderId).child("Contacts").child(receiverId).child("Chats");
        senderReference.orderByChild("msgTime").equalTo(selectedMessage.getMsgTime()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String decrypted = "";
                    try {
                        decrypted = AESUtils.decrypt(snapshot.child("msgText").getValue().toString());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    String encryptedMsg = editedMessage;
                    try {
                        encryptedMsg = AESUtils.encrypt(editedMessage);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (decrypted.equals(selectedMessage.getMsgText())) {
                        selectedMessage.setMsgText(encryptedMsg);
                        snapshot.getRef().child("msgText").setValue(encryptedMsg); // Update message text
                        firebaseDatabase.getReference("Users").child(senderId).child("Contacts").child(receiverId)
                                .child("recentMessage").setValue(encryptedMsg);
                    }
                    int index = msgData.indexOf(selectedMessage);
                    if (index != -1) {
                        msgData.set(index, selectedMessage);
                        msgAdapter.notifyItemChanged(index); // Notify adapter of the change
                        break;
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });
         //Edit message from receiver node
        DatabaseReference receiverReference = firebaseDatabase.getReference("Users")
                .child(receiverId).child("Contacts").child(senderId).child("Chats");
        receiverReference.orderByChild("msgTime").equalTo(selectedMessage.getMsgTime()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String decrypted = "";
                    try {
                        decrypted = AESUtils.decrypt(snapshot.child("msgText").getValue().toString());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    String msg = message.getMsgText();

                    snapshot.getRef().child("msgText").setValue(msg); // Update message text
                    firebaseDatabase.getReference("Users").child(receiverId).child("Contacts").child(senderId)
                            .child("recentMessage").setValue(msg);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    private void deleteMessage() {
        // Delete message from sender node
        DatabaseReference senderReference = firebaseDatabase.getReference("Users")
                .child(senderId).child("Contacts").child(receiverId).child("Chats");
        senderReference.orderByChild("msgTime").equalTo(selectedMessage.getMsgTime()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String decrypted = "";
                    try {
                        decrypted = AESUtils.decrypt(snapshot.child("msgText").getValue().toString());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    if (decrypted.equals(selectedMessage.getMsgText())) {
                        snapshot.getRef().removeValue();
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });

        // Delete message from receiver node
        DatabaseReference receiverReference = firebaseDatabase.getReference("Users")
                .child(receiverId).child("Contacts").child(senderId).child("Chats");
        receiverReference.orderByChild("msgTime").equalTo(selectedMessage.getMsgTime()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String decrypted = "";
                    try {
                        decrypted = AESUtils.decrypt(snapshot.child("msgText").getValue().toString());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    if (decrypted.equals(selectedMessage.getMsgText())) {
                        snapshot.getRef().removeValue();
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });

        // Update local data and notify RecyclerView adapter
        int index = msgData.indexOf(selectedMessage);
        if (index != -1) {
            msgData.remove(index); // Remove deleted message from local data
            msgAdapter.notifyItemRemoved(index); // Notify adapter of the data change
        }
    }
}
               