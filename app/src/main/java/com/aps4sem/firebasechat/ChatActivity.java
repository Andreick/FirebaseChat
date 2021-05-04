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
    private String fromUid;
    private User currentUser;
    private User contactUser;
    private TextView editMessage;
    private GroupAdapter<ViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        fromUid = Objects.requireNonNull(FirebaseAuth.getInstance().getUid());
        firestore = FirebaseFirestore.getInstance();
        firestore.collection(FirestoreCollection.Users.name())
                .document(fromUid)
                .get()
                .addOnSuccessListener(documentSnapshot -> currentUser = documentSnapshot.toObject(User.class))
                .addOnFailureListener(e -> Log.e("Log", e.getMessage()));
        contactUser = getIntent().getExtras().getParcelable("user");
        editMessage = findViewById(R.id.edit_message);
        adapter = new GroupAdapter<>();

        Objects.requireNonNull(getSupportActionBar()).setTitle(contactUser.getUsername());

        RecyclerView recyclerView = findViewById(R.id.recycler_chat);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        Button button_chat = findViewById(R.id.button_send);
        button_chat.setOnClickListener(v -> sendMessage());

        firestore.collection(FirestoreCollection.Users.name())
                .document(fromUid)
                .get()
                .addOnSuccessListener(documentSnapshot -> fetchMessages())
                .addOnFailureListener(e -> Log.e("Log", e.getMessage()));
    }

    private void fetchMessages() {
        String toId = contactUser.getId();

        firestore.collection(FirestoreCollection.Messages.name())
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
            String toUid = contactUser.getId();
            Message message = new Message(text, currentUser.getId(), toUid);

            firestore.collection(FirestoreCollection.Messages.name())
                    .document(fromUid)
                    .collection(toUid)
                    .add(message)
                    .addOnSuccessListener(documentReference -> {
                        LastMessage lastMessage = new LastMessage(contactUser, message);

                        firestore.collection(FirestoreCollection.HomeMessages.name())
                                .document(fromUid)
                                .collection(FirestoreCollection.LastMessage.name())
                                .document(toUid)
                                .set(lastMessage);
                    })
                    .addOnFailureListener(e -> Log.e("Log", e.getMessage()));

            firestore.collection(FirestoreCollection.Messages.name())
                    .document(toUid)
                    .collection(fromUid)
                    .add(message)
                    .addOnSuccessListener(documentReference -> {
                        LastMessage lastMessage = new LastMessage(currentUser, message);

                        firestore.collection(FirestoreCollection.HomeMessages.name())
                                .document(toUid)
                                .collection(FirestoreCollection.LastMessage.name())
                                .document(fromUid)
                                .set(lastMessage);
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
            Picasso.get().load(contactUser.getProfileUrl()).into(imageUserMessage);
        }

        @Override
        public int getLayout() {
            return message.getFromUid().equals(fromUid)
                    ? R.layout.item_from_message
                    : R.layout.item_to_message;
        }
    }
}