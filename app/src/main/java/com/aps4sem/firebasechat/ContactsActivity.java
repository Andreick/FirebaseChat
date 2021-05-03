package com.aps4sem.firebasechat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.Item;
import com.xwray.groupie.ViewHolder;

import java.util.List;

public class ContactsActivity extends AppCompatActivity {

    private String currentUid;
    private GroupAdapter<ViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        currentUid = FirebaseAuth.getInstance().getUid();
        adapter = new GroupAdapter<>();

        RecyclerView recyclerView = findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener((item, view) -> {
            goToChatActivity((UserItem) item);
        });

        fetchUsers();
    }

    private void goToChatActivity(UserItem userItem) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("user", userItem.user);

        startActivity(intent);
    }

    private void fetchUsers() {
        FirebaseFirestore.getInstance().collection("Users")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e == null) {
                        List<DocumentSnapshot> docs = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot doc: docs) {
                            User user = doc.toObject(User.class);
                            if (!currentUid.equals(user.getId())) {
                                adapter.add(new UserItem(user));
                            }
                        }
                    }
                    else {
                        Log.e("Log", e.getMessage());
                    }
                });
    }

    private static class UserItem extends Item<ViewHolder> {

        private final User user;

        private UserItem(User user) {
            this.user = user;
        }

        @Override
        public void bind(@NonNull ViewHolder viewHolder, int position) {
            TextView textUsername = viewHolder.itemView.findViewById(R.id.text_username);
            ImageView imageProfile = viewHolder.itemView.findViewById(R.id.image_profile);

            textUsername.setText(user.getUsername());

            Picasso.get().load(user.getProfileUrl()).into(imageProfile);
        }

        @Override
        public int getLayout() {
            return R.layout.item_user;
        }
    }
}