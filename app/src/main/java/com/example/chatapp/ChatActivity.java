package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private ListView listViewMessages;
    private MessageAdapter adapter;
    private ProgressBar progressBar;
    private ImageButton btnSendImage;
    private Button btnSendMessage;
    private EditText editTextMessage;

    private String userName;
    private String recepientUserId;
    private String recepientUserName;

    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference dbReferencesMessages;
    private ChildEventListener messagesChildEventListener;
    private DatabaseReference dbReferencesUsers;
    private ChildEventListener usersChildEventListener;
    private FirebaseStorage storage;
    private StorageReference chatImagesStorageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Intent intent = getIntent();
        if (intent != null){
            userName = intent.getStringExtra("username");
            recepientUserId = intent.getStringExtra("recipientUserId");
            recepientUserName = intent.getStringExtra("recipientUserName");
        }

        setTitle("Чат с "+ recepientUserName);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        dbReferencesMessages = database.getReference().child("messages");
        dbReferencesUsers = database.getReference().child("users");
        storage = FirebaseStorage.getInstance();
        chatImagesStorageReference = storage.getReference().child("chat_images");

        listViewMessages = findViewById(R.id.listViewMessages);
        List<Message> messages = new ArrayList<>();
        progressBar = findViewById(R.id.progressBar);
        btnSendMessage = findViewById(R.id.btnSendMessage);
        btnSendImage = findViewById(R.id.btnSendPhoto);
        editTextMessage = findViewById(R.id.editTextMessage);


        adapter = new MessageAdapter(this,R.layout.message_item,messages);
        listViewMessages.setAdapter(adapter);

        progressBar.setVisibility(ProgressBar.INVISIBLE);

        editTextMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0){
                    btnSendMessage.setEnabled(true);
                } else {
                    btnSendMessage.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        editTextMessage.setFilters(new InputFilter[]{new InputFilter.LengthFilter(500)});

        messagesChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Message message= snapshot.getValue(Message.class);
                if (message.getSender().equals(auth.getCurrentUser().getUid()) && message.getRecipient().equals(recepientUserId))
                {
                    message.setMine(true);
                    adapter.add(message);
                } else if (message.getRecipient().equals(auth.getCurrentUser().getUid()) && message.getSender().equals(recepientUserId)){
                    message.setMine(false);
                    adapter.add(message);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        usersChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                User user = snapshot.getValue(User.class);
                if (user.getId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                    userName = user.getName();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        dbReferencesMessages.addChildEventListener(messagesChildEventListener);
        dbReferencesUsers.addChildEventListener(usersChildEventListener);
    }

    public void btnSendMessage_onClick(View view) {
        Message message = new Message();
        message.setText(editTextMessage.getText().toString());
        message.setName(userName);
        message.setSender(auth.getCurrentUser().getUid());
        message.setRecipient(recepientUserId);
        message.setImageUrl(null);
        dbReferencesMessages.push().setValue(message);
        editTextMessage.setText(" ");
    }

    public void btnSendPhoto_onClick(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY,true);
        startActivityForResult(Intent.createChooser(intent,"Выберите изображение"),123);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123 && resultCode == RESULT_OK){
            Uri selectedImgUri = data.getData();
            final StorageReference imageReference = chatImagesStorageReference.child(selectedImgUri.getLastPathSegment());
            UploadTask uploadTask = imageReference.putFile(selectedImgUri);

            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return imageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        Message message = new Message();
                        message.setImageUrl(downloadUri.toString());
                        message.setName(userName);
                        message.setSender(auth.getCurrentUser().getUid());
                        message.setRecipient(recepientUserId);
                        dbReferencesMessages.push().setValue(message);
                    } else {}
                }
            });
        }
    }
}