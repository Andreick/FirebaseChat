package com.aps4sem.firebasechat;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.UUID;

public class RegisterActivity extends AppCompatActivity {

    private EditText editUsername, editEmail, editPassword;
    private Button buttonPhoto, buttonRegister;
    private ImageView imagePhoto;

    private Uri selectedUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editUsername = findViewById(R.id.edit_username);
        editEmail = findViewById(R.id.edit_email);
        editPassword = findViewById(R.id.edit_password);
        buttonPhoto = findViewById(R.id.button_photo);
        buttonRegister = findViewById(R.id.button_register);
        imagePhoto = findViewById(R.id.image_photo);

        buttonPhoto.setOnClickListener(v -> pickPhoto());
        buttonRegister.setOnClickListener(v -> register());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null) {
            if (requestCode == 0) {
                selectedUri = data.getData();

                Bitmap bitmap;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedUri);
                    imagePhoto.setImageDrawable(new BitmapDrawable(getApplicationContext().getResources(), bitmap));
                    buttonPhoto.setAlpha(0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void pickPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 0);
    }

    private void register() {
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
                            Log.i("Teste", task.getResult().getUser().getUid());

                            saveUserInFirebase();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.i("Teste", e.getMessage());
                    });
        }
    }

    private void saveUserInFirebase() {
        String filename = UUID.randomUUID().toString();
        final StorageReference ref = FirebaseStorage.getInstance().getReference("images/" + filename);
        ref.putFile(selectedUri)
                .addOnSuccessListener(taskSnapshot -> {
                    ref.getDownloadUrl().addOnSuccessListener(uri -> {
                        Log.i("Teste", uri.toString());
                    });
                })
                .addOnFailureListener(e -> {
                    Log.i("Teste", e.getMessage());
                });
    }
}