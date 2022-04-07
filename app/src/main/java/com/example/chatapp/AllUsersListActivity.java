package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class AllUsersListActivity extends AppCompatActivity {

    private String userName;

    private FirebaseAuth auth;
    private DatabaseReference usersDatabaseReference;
    private ChildEventListener usersChildEventListeners;

    private ArrayList<User> userArrayList;
    private ArrayList<String> friends;
    private RecyclerView recyclerViewUser;
    private UserAdapter userAdapter;
    private RecyclerView.LayoutManager userLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users_list);
        setTitle("Выберите собеседника");
        Intent intent = getIntent();
        if (intent != null){
            friends = intent.getStringArrayListExtra("friends");
            if (friends == null) friends = new ArrayList<>();
        }

        auth = FirebaseAuth.getInstance();
        userArrayList = new ArrayList<>();
        buildRecyclerView();
        attachUserDatabaseReferenceListener();
    }

    private void attachUserDatabaseReferenceListener() {
        usersDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        if (usersChildEventListeners == null){
            usersChildEventListeners = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    User user = snapshot.getValue(User.class);
                    if (!user.getId().equals(auth.getCurrentUser().getUid()) &&
                            !friends.contains(user.getId())){
                        user.setAvatarMockUpResource(R.drawable.ic_person_black_24dp);
                        userArrayList.add(user);
                        userAdapter.notifyDataSetChanged();
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
            usersDatabaseReference.addChildEventListener(usersChildEventListeners);
        }
    }

    private void buildRecyclerView() {
        recyclerViewUser = findViewById(R.id.recyclerViewAllUsersList);
        recyclerViewUser.setHasFixedSize(true);
        recyclerViewUser.addItemDecoration(new DividerItemDecoration(recyclerViewUser.getContext(),DividerItemDecoration.VERTICAL));
        userLayoutManager = new LinearLayoutManager(this);
        userAdapter = new UserAdapter(userArrayList);

        recyclerViewUser.setLayoutManager(userLayoutManager);
        recyclerViewUser.setAdapter(userAdapter);

        userAdapter.setOnUserClickListener(new UserAdapter.OnUserClickListener() {
            @Override
            public void onUserClick(int position) {
                returnNewFriend(position);
            }
        });
    }

    private void returnNewFriend(int position){
        String newFriendId = userArrayList.get(position).getId();
        DatabaseReference myDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(auth.getCurrentUser().getUid())
                .child("friends");
        myDatabaseReference.child(newFriendId).setValue(newFriendId);
        DatabaseReference yourDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(newFriendId)
                .child("friends");
        yourDatabaseReference.child(auth.getCurrentUser().getUid()).setValue(auth.getCurrentUser().getUid());
        startActivity(new Intent(AllUsersListActivity.this,UsersListActivity.class));
    }

}