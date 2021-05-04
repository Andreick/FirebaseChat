package com.aps4sem.firebasechat;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.Item;
import com.xwray.groupie.ViewHolder;

import java.util.List;
import java.util.Objects;

public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private String currentUid;
    private GroupAdapter<ViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        auth = FirebaseAuth.getInstance();
        currentUid = auth.getUid();

        if (currentUid == null) goToLoginActivity();

        adapter = new GroupAdapter<>();
        
        RecyclerView recyclerView = findViewById(R.id.recycler_contact);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        fetchLastMessages();

        adapter.setOnItemClickListener((item, view) -> goToChatActivity((ContactItem) item));
    }

    private void goToChatActivity(ContactItem contactItem) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("user", contactItem.user);

        startActivity(intent);
    }

    private void fetchLastMessages() {
        FirebaseFirestore.getInstance().collection(FirestoreCollection.HomeMessages.name())
                .document(currentUid)
                .collection(FirestoreCollection.LastMessage.name())
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e == null) {
                        List<DocumentChange> documentChanges = Objects.requireNonNull(queryDocumentSnapshots).getDocumentChanges();

                        for (DocumentChange documentChange : documentChanges) {
                            if (documentChange.getType() == DocumentChange.Type.ADDED) {
                                LastMessage lastMessage = documentChange.getDocument().toObject(LastMessage.class);

                                adapter.add(new ContactItem(lastMessage));
                            }
                        }
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.contacts) {
            goToContactsActivity();
        }
        else if (id == R.id.logout) {
            auth.signOut();
            goToLoginActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    private void goToContactsActivity() {
        Intent intent = new Intent(this, ContactsActivity.class);
        startActivity(intent);
    }

    private void goToLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private class ContactItem extends Item<ViewHolder> {

        private final User user;
        private final Message lastMessage;

        public ContactItem(LastMessage lastMessage) {
            this.user = lastMessage.getUser();
            this.lastMessage = lastMessage.getMessage();
        }

        @Override
        public void bind(@NonNull ViewHolder viewHolder, int position) {
            TextView textUsername = viewHolder.itemView.findViewById(R.id.text_username);
            TextView textLastMessage = viewHolder.itemView.findViewById(R.id.text_lastMessage);
            ImageView imageProfile = viewHolder.itemView.findViewById(R.id.image_profile);

            textUsername.setText(user.getUsername());
            textLastMessage.setText(lastMessage.getText());
            Picasso.get().load(user.getProfileUrl()).into(imageProfile);
        }

        @Override
        public int getLayout() {
            return R.layout.item_user_message;
        }
    }
}