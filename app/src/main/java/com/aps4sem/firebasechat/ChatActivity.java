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
    private String senderUid;
    private User currentUser;
    private User contactUser;
    private TextView editMessage;
    private GroupAdapter<ViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        senderUid = Objects.requireNonNull(FirebaseAuth.getInstance().getUid());
        firestore = FirebaseFirestore.getInstance();
        firestore.collection(FirestoreCollection.Users.name())
                .document(senderUid)
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
                .document(senderUid)
                .get()
                .addOnSuccessListener(documentSnapshot -> fetchMessages())
                .addOnFailureListener(e -> Log.e("Log", e.getMessage()));
    }

    private void fetchMessages() {
        String toId = contactUser.getId();

        firestore.collection(FirestoreCollection.Messages.name())
                .document(senderUid)
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
            String receiverUid = contactUser.getId();
            Message message = new Message(text, senderUid, receiverUid);

            firestore.collection(FirestoreCollection.Messages.name())
                    .document(senderUid)
                    .collection(receiverUid)
                    .add(message)
                    .addOnSuccessListener(documentReference -> {
                        LastMessage lastMessage = new LastMessage(contactUser, senderUid, text, message.getTimestamp());

                        firestore.collection(FirestoreCollection.HomeMessages.name())
                                .document(senderUid)
                                .collection(FirestoreCollection.LastMessage.name())
                                .document(receiverUid)
                                .set(lastMessage);
                    })
                    .addOnFailureListener(e -> Log.e("Log", e.getMessage()));

            firestore.collection(FirestoreCollection.Messages.name())
                    .document(receiverUid)
                    .collection(senderUid)
                    .add(message)
                    .addOnSuccessListener(documentReference -> {
                        LastMessage lastMessage = new LastMessage(currentUser, senderUid, text, message.getTimestamp());

                        firestore.collection(FirestoreCollection.HomeMessages.name())
                                .document(receiverUid)
                                .collection(FirestoreCollection.LastMessage.name())
                                .document(senderUid)
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

            if (message.getSenderUid().equals(senderUid)) {
                Picasso.get().load(currentUser.getProfileUrl()).into(imageUserMessage);
            } else {
                Picasso.get().load(contactUser.getProfileUrl()).into(imageUserMessage);
            }
        }

        @Override
        public int getLayout() {
            return message.getSenderUid().equals(senderUid)
                    ? R.layout.item_sender_message
                    : R.layout.item_receiver_message;
        }
    }
}