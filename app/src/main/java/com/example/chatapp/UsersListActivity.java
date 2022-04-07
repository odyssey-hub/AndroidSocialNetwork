package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

public class UsersListActivity extends AppCompatActivity {

    private String userName;

    private FirebaseAuth auth;
    private DatabaseReference usersDatabaseReference;
    DatabaseReference friendsDatabaseReference;
    private ChildEventListener usersChildEventListeners;
    private ChildEventListener friendsChildEventListeners;

    private ArrayList<User> userArrayList;
    private RecyclerView recyclerViewUser;
    private UserAdapter userAdapter;
    private RecyclerView.LayoutManager userLayoutManager;

    private ArrayList<String> friends;

    private StorageReference usersAvatarsStorageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);

        setTitle("Список собеседников");

        usersAvatarsStorageReference = FirebaseStorage.getInstance().getReference().child("avatar_images");

        Intent intent = getIntent();
        if (intent != null){
            userName = intent.getStringExtra("username");
        }

        friends = new ArrayList<>();

        auth = FirebaseAuth.getInstance();
        userArrayList = new ArrayList<>();
        buildRecyclerView();
        attachUserDatabaseReferenceListener();
    }

    private void getUserFriends()
    {
        friendsDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(auth.getCurrentUser().getUid()).child("friends");
        if (friendsChildEventListeners == null){
            friendsChildEventListeners = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    String data = "";
                    try{
                        data = snapshot.getValue().toString();
                    } catch (Exception e){
                        return;
                    }
                    String[] dataArray = data.split(",");
                    for(String pair : dataArray){
                        friends.add(pair.substring(pair.indexOf("=")+1));
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
            friendsDatabaseReference.addChildEventListener(friendsChildEventListeners);
        }
//        friendsDatabaseReference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//                //GenericTypeIndicator<List<String>> t = new GenericTypeIndicator<List<String>>() {};
//               //friends = snapshot.child("friends").getValue(t);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
    }

    private void attachUserDatabaseReferenceListener() {
        getUserFriends();
        usersDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        if (usersChildEventListeners == null){
            usersChildEventListeners = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    User user = snapshot.getValue(User.class);
                    if (friends.contains(user.getId())){
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
        recyclerViewUser = findViewById(R.id.recyclerViewUserList);
        recyclerViewUser.setHasFixedSize(true);
        recyclerViewUser.addItemDecoration(new DividerItemDecoration(recyclerViewUser.getContext(),DividerItemDecoration.VERTICAL));
        userLayoutManager = new LinearLayoutManager(this);
        userAdapter = new UserAdapter(userArrayList);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                removeFriend(viewHolder.getAdapterPosition());
            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerViewUser);

        recyclerViewUser.setLayoutManager(userLayoutManager);
        recyclerViewUser.setAdapter(userAdapter);

        userAdapter.setOnUserClickListener(new UserAdapter.OnUserClickListener() {
            @Override
            public void onUserClick(int position) {
                goToChat(position);
            }
        });
    }

    private void removeFriend(int position){
        String removeId = friends.get(position);
        usersDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users")
        .child(auth.getCurrentUser().getUid()).child("friends").child(removeId);
        usersDatabaseReference.removeValue();
        friends.remove(removeId);
        userArrayList.remove(position);
        userAdapter.notifyDataSetChanged();

    }

    private void goToChat(int position) {
        Intent intent = new Intent(UsersListActivity.this,ChatActivity.class);
        intent.putExtra("recipientUserId",userArrayList.get(position).getId());
        intent.putExtra("recipientUserName",userArrayList.get(position).getName());
        intent.putExtra("username",userName);
        startActivity(intent);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.add_friend:
                Intent intent = new Intent(UsersListActivity.this,AllUsersListActivity.class);
                intent.putStringArrayListExtra("friends",friends);
                startActivity(intent);
                return true;

            case R.id.sign_out:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(UsersListActivity.this,SignInActivity.class));
                return true;
            case R.id.change_avatar:
                Intent intent2 = new Intent(Intent.ACTION_GET_CONTENT);
                intent2.setType("image/*");
                intent2.putExtra(Intent.EXTRA_LOCAL_ONLY,true);
                startActivityForResult(Intent.createChooser(intent2,"Выберите изображение"),125);
            default:
                return  super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 125 && resultCode == RESULT_OK){
            Uri selectedImgUri = data.getData();
            final StorageReference imageReference = usersAvatarsStorageReference.child(selectedImgUri.getLastPathSegment());
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
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("users")
                                .child(auth.getCurrentUser().getUid()).child("avatarURL");
                        reference.setValue(downloadUri.toString());
                        Toast.makeText(getApplicationContext(),"Аватарка изменена",Toast.LENGTH_SHORT);
                    } else {}
                }
            });


        }

    }

}