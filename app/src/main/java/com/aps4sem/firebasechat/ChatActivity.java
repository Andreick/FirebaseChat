package com.aps4sem.firebasechat;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.Item;
import com.xwray.groupie.ViewHolder;

import java.util.List;
import java.util.Objects;

public class ChatActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private User user;
    private String fromUid;
    private TextView editMessage;
    private GroupAdapter<ViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        firestore = FirebaseFirestore.getInstance();
        user = getIntent().getExtras().getParcelable("user");
        fromUid = FirebaseAuth.getInstance().getUid();
        editMessage = findViewById(R.id.edit_message);
        adapter = new GroupAdapter<>();

        Objects.requireNonNull(getSupportActionBar()).setTitle(user.getUsername());

        RecyclerView recyclerView = findViewById(R.id.recycler_chat);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        Button button_chat = findViewById(R.id.button_send);
        button_chat.setOnClickListener(v -> sendMessage());

        firestore.collection("Users")
                .document(fromUid)
                .get()
                .addOnSuccessListener(documentSnapshot -> fetchMessages())
                .addOnFailureListener(e -> Log.e("Log", e.getMessage()));
    }

    private void fetchMessages() {
        String toId = user.getId();

        firestore.collection("Conversations")
                .document(fromUid)
                .collection(toId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e == null) {
                        List<DocumentChange> documentChanges = Objects.requireNonNull(queryDocumentSnapshots).getDocumentChanges();

                        for (DocumentChange documentChange : documentChanges) {
                            if (documentChange.getType() == DocumentChange.Type.ADDED) {
                                Message message = documentChange.getDocument().toObject(Message.class);
                                adapter.add(new MessageItem(message));
                            }
                        }
                    }
                    else {
                        Log.e("Log", e.getMessage());
                    }
                });
    }

    private void sendMessage() {
        String text = editMessage.getText().toString();

        if (!TextUtils.isEmpty(text)) {
            String toUid = user.getId();
            Message message = new Message(text, fromUid, toUid);

            firestore.collection("Conversations")
                    .document(fromUid)
                    .collection(toUid)
                    .add(message)
                    .addOnSuccessListener(documentReference -> {
                        Log.d("Log", documentReference.getId());
                    })
                    .addOnFailureListener(e -> Log.e("Log", e.getMessage()));

            firestore.collection("Conversations")
                    .document(toUid)
                    .collection(fromUid)
                    .add(message)
                    .addOnSuccessListener(documentReference -> {
                        Log.d("Log", documentReference.getId());
                    })
                    .addOnFailureListener(e -> Log.e("Log", e.getMessage()));

            editMessage.setText(null);
        }
    }

    private class MessageItem extends Item<ViewHolder> {

        private final Message message;

        public MessageItem(Message message) {
            this.message = message;
        }

        @Override
        public void bind(@NonNull ViewHolder viewHolder, int position) {
            TextView textMessage = viewHolder.itemView.findViewById(R.id.text_message);
            ImageView imageUserMessage = viewHolder.itemView.findViewById(R.id.image_userMessage);

            textMessage.setText(message.getText());
            Picasso.get().load(user.getProfileUrl()).into(imageUserMessage);
        }

        @Override
        public int getLayout() {
            return message.getFromUid().equals(fromUid)
                    ? R.layout.item_from_message
                    : R.layout.item_to_message;
        }
    }
}