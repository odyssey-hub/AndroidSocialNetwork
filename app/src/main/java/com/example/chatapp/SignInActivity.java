package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignInActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private boolean loginModeActive = true;

    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextPassword2;
    private EditText editTextName;
    private Button btnSign;
    private TextView textLogin;

    FirebaseDatabase database;
    DatabaseReference usersDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        usersDatabaseReference = database.getReference().child("users");

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextPassword2 = findViewById(R.id.editTextPassword2);
        editTextName = findViewById(R.id.editTextName);
        btnSign = findViewById(R.id.btnSignUp);
        textLogin = findViewById(R.id.textViewToogle);

        editTextName.setVisibility(View.GONE);
        editTextPassword2.setVisibility(View.GONE);

        if (auth.getCurrentUser() != null){
            startActivity(new Intent(SignInActivity.this, UsersListActivity.class));
        }


}
    public void btnSignUp_onClick(View view) {
        loginSignUpUser(editTextEmail.getText().toString().trim(),editTextPassword.getText().toString().trim());
    }

    private void loginSignUpUser(String email, String password) {
        if (loginModeActive) {
            if (editTextEmail.getText().toString().trim().equals("")) {
                Toast.makeText(this, "Пожалуйста, введите email", Toast.LENGTH_SHORT).show();
            } else if (editTextPassword.getText().toString().trim().length() < 7) {
                Toast.makeText(this, "Пожалуйста, введите пароль", Toast.LENGTH_SHORT).show();
            } else {
                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Log.d("auth", "signInWithEmail:success");
                                    FirebaseUser user = auth.getCurrentUser();
                                    Intent intent = new Intent(SignInActivity.this, UsersListActivity.class);
                                    startActivity(intent);
                                } else {
                                    Log.w("auth", "signInWithEmail:failure", task.getException());
                                    Toast.makeText(SignInActivity.this,task.getException().toString(),Toast.LENGTH_SHORT).show();
                                    //Toast.makeText(SignInActivity.this, "Неправильный логин и/или пароль", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        } else {
            if (editTextEmail.getText().toString().trim().equals("")) {
                Toast.makeText(SignInActivity.this, "Пожалуйста, введите email", Toast.LENGTH_SHORT).show();
            } else if (!validateEmail(editTextEmail.getText().toString().trim())) {
                Toast.makeText(SignInActivity.this, "Email некорректеный", Toast.LENGTH_SHORT).show();
            } else if (editTextPassword.getText().toString().trim().length() < 7) {
                Toast.makeText(SignInActivity.this, "Пароль должен содержать минимум 7 символов", Toast.LENGTH_SHORT).show();
            } else if (!editTextPassword.getText().toString().trim().equals(editTextPassword2.getText().toString().trim())) {
                Toast.makeText(SignInActivity.this, "Пароли не совпадают", Toast.LENGTH_SHORT).show();
            }  else if (editTextName.getText().toString().trim().equals("")) {
                Toast.makeText(SignInActivity.this, "Пожалуйста, введите ник", Toast.LENGTH_SHORT).show();
            } else {
                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Log.d("SignIn", "signInWithEmail:success");
                                    FirebaseUser user = auth.getCurrentUser();
                                    createUser(user);
                                    Intent intent = new Intent(SignInActivity.this, UsersListActivity.class);
                                    intent.putExtra("username",editTextName.toString().trim());
                                    startActivity(intent);
                                } else {
                                    Log.w("SignIn", "signInWithEmail:failure", task.getException());
                                    Toast.makeText(SignInActivity.this, task.getException().toString(),Toast.LENGTH_SHORT).show();
                                    //Toast.makeText(SignInActivity.this, "Не удалось зарегистрироваться",Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }

        }
    }

    private boolean validateEmail(String email){
        Pattern pattern = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.find();
    }

    private void createUser(FirebaseUser firebase_user){
        User user = new User();
        user.setId(firebase_user.getUid());
        user.setEmail(firebase_user.getEmail());
        user.setName(editTextName.getText().toString().trim());
        usersDatabaseReference.child(firebase_user.getUid()).setValue(user);
    }

    private void fillSpace(){
        editTextName.setText("");
        editTextPassword.setText("");
        editTextEmail.setText("");
        editTextPassword2.setText("");
    }

    public void toogleLoginMode(View view) {
        if (loginModeActive){
            loginModeActive = false;
            btnSign.setText("Зарегистрироваться");
            textLogin.setText("У меня уже есть аккаунт");
            fillSpace();
            editTextName.setVisibility(View.VISIBLE);
            editTextPassword2.setVisibility(View.VISIBLE);
        } else {
            loginModeActive = true;
            btnSign.setText("Авторизоваться");
            textLogin.setText("У меня нет аккаунта");
            fillSpace();
            editTextName.setVisibility(View.GONE);
            editTextPassword2.setVisibility(View.GONE);
        }
    }
}