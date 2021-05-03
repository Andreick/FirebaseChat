package com.aps4sem.firebasechat;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

public class RegisterActivity extends AppCompatActivity {

    private EditText editUsername, editEmail, editPassword;
    private Button buttonPhoto, buttonRegister;
    private ImageView imagePhoto;

    private Uri pickedPhotoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editUsername = findViewById(R.id.edit_username);
        editEmail = findViewById(R.id.edit_email);
        editPassword = findViewById(R.id.edit_password);
        buttonPhoto = findViewById(R.id.button_photo);
        buttonRegister = findViewById(R.id.button_login);
        imagePhoto = findViewById(R.id.image_photo);

        buttonPhoto.setOnClickListener(v -> pickProfilePhoto());
        buttonRegister.setOnClickListener(v -> createNewAccount());
    }

    private void pickProfilePhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null) {
            if (requestCode == 0) {
                pickedPhotoUri = data.getData();

                Bitmap bitmap;
                try {
                    if (Build.VERSION.SDK_INT < 28) {
                        bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), pickedPhotoUri);
                    }
                    else {
                        ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), pickedPhotoUri);
                        bitmap = ImageDecoder.decodeBitmap(source);
                    }
                    imagePhoto.setImageDrawable(new BitmapDrawable(getApplicationContext().getResources(), bitmap));
                    buttonPhoto.setAlpha(0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void createNewAccount() {
        String username = editUsername.getText().toString();
        String email = editEmail.getText().toString();
        String password = editPassword.getText().toString();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Todos os campos devem ser preenchidos", Toast.LENGTH_SHORT).show();
        }
        else {
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            saveUserInFirebase(username);
                        }
                    })
                    .addOnFailureListener(e -> Log.e("Log", e.getMessage()));
        }
    }

    private void saveUserInFirebase(String username) {
        String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getUid());

        User user = new User(uid, username);

        FirebaseFirestore.getInstance().collection("Users")
                .document(uid)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Conta criada com sucesso!", Toast.LENGTH_SHORT).show();
                    goToHomeActivity();
                })
                .addOnFailureListener(e -> Log.e("Log", e.getMessage()));
    }

    /*private void saveUserInFirebase(String username) {
        String filename = UUID.randomUUID().toString();
        StorageReference ref = FirebaseStorage.getInstance().getReference("profiles/" + filename);
        ref.putFile(pickedPhotoUri)
                .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            Log.d("Log", uri.toString());

                            String uid = FirebaseAuth.getInstance().getUid();
                            String profileUrl = uri.toString();

                            User user = new User(uid, username, profileUrl);

                            FirebaseFirestore.getInstance().collection("Users").add(user)
                                    .addOnSuccessListener(documentReference -> Toast.makeText(this, "Conta criada com sucesso!", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e -> Log.e("Log", e.getMessage()));
                        })
                        .addOnFailureListener(e -> Log.e("Log", e.getMessage())))
                .addOnFailureListener(e -> Log.e("Log", e.getMessage()));
    }*/

    private void goToHomeActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}