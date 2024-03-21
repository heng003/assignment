package com.firstapp.hootnholler;

import static android.app.PendingIntent.getActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.firstapp.hootnholler.adapter.Chat_ArrayAdapter;
import com.firstapp.hootnholler.databinding.ActivityChatBinding;
import com.firstapp.hootnholler.entity.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Y_ChatActivity extends AppCompatActivity {

    private String conversationKey, uid;
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Conversation");
    private RecyclerView chatList;
    private Chat_ArrayAdapter adapter;
    private ArrayList<Message> messages = new ArrayList<>();
    private EditText msg;
    private ImageView sendMsg;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private TextView chatName;
    private ActivityChatBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        conversationKey = getIntent().getStringExtra("conversationKey");
        adapter = new Chat_ArrayAdapter(messages);
        chatList = findViewById(R.id.chatList);
        chatList.setAdapter(adapter);
        chatList.setLayoutManager(new LinearLayoutManager(this));
        msg = findViewById(R.id.message);
        sendMsg = findViewById(R.id.sendIcon);
        chatName = findViewById(R.id.chatName);
        sendMsg.setVisibility(View.GONE);
        uid = mAuth.getUid();
        getConversationDetails();
        msg.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                checkMessage();
            }
        });

        sendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String filteredMessage = filterMessage(msg.getText().toString());
                sendMessage(filteredMessage);
            }
        });
        getMessage();

        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    //sensitive word detection
    private String filterMessage(String originalMessage) {
        //list of sensitive/rude words
        String[] rudeWords = {"fork", "shat", "badword"};

        //replace rude words with asterisks
        String filteredMessage = originalMessage.toLowerCase();
        for (String rudeWord : rudeWords) {
            if (filteredMessage.contains(rudeWord)) {
                StringBuilder asterisks = new StringBuilder();
                for (int i = 0; i < rudeWord.length(); i++) {
                    asterisks.append('*');
                }
                filteredMessage = filteredMessage.replace(rudeWord, asterisks.toString());
            }
        }

        return filteredMessage;
    }

    //to retrieve group chat name
    public void getConversationDetails(){
        databaseReference.child("GroupChat").child(conversationKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //retrieve conversation name from the database
                String conversationName = snapshot.child("Name").getValue(String.class);
                //set name in the ui
                chatName.setText(conversationName != null ? conversationName : "Chat");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //to send message in group chat
    public void sendMessage(String filteredMessage){
        //get current timestamp
        long currentTime = System.currentTimeMillis();

        //set message content in database
        databaseReference.child("GroupChat")
                .child(conversationKey)
                .child("message")
                .child(String.valueOf(currentTime))
                .child("content")
                .setValue(filteredMessage);

        //set sender's uid in database
        databaseReference.child("GroupChat")
                .child(conversationKey)
                .child("message")
                .child(String.valueOf(currentTime))
                .child("senderUID")
                .setValue(uid);

        //clear input field after sending message
        msg.setText("");
    }

    //check if message present in input field
    public void checkMessage(){
        if(msg.getText().toString().isEmpty()){
            //hide the send button if there is no message
            sendMsg.setVisibility(View.GONE);
        }
        else{
            //show the send button if there is a message
            sendMsg.setVisibility(View.VISIBLE);
        }
    }

    //to retrieve and display messages from the group chat
    public void getMessage(){
        databaseReference.child("GroupChat").child(conversationKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messages.clear();
                for (DataSnapshot msgSnapshot : snapshot.child("message").getChildren()) {
                    //deserialize the message data into a Message object
                    Message message = msgSnapshot.getValue(Message.class);
                    //set the timestamp for the message
                    message.setTimestamp(Long.parseLong(msgSnapshot.getKey()));
                    //add message to the list
                    messages.add(message);
                    //notify adapter
                    adapter.notifyItemChanged(adapter.getItemCount() - 1);
                    //scroll the chat list to the latest message
                    chatList.smoothScrollToPosition(adapter.getItemCount() - 1);
                    //mark message as read
                    readMessage(message);
                }
                adapter.notifyDataSetChanged();
                //scroll the chat list to the latest message
                chatList.scrollToPosition(messages.size()-1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //mark message as read
    public void readMessage(Message message){
        // if not the user message
        if(message.getSenderUID() != null){
            if(!message.getSenderUID().equals(uid)){
                Map<String, Boolean> readStatus;
                //check if the message already has read status
                if(message.getReadStatus() != null){
                    readStatus = message.getReadStatus();
                    readStatus.put(uid, true);
                }
                else{
                    readStatus = new HashMap<>();
                    readStatus.put(uid, true);
                }
                //set the updated read status in the message and the database
                message.setReadStatus(readStatus);
                databaseReference.child("GroupChat").child(conversationKey).child("message").child(String.valueOf(message.getTimestamp())).child("readStatus").setValue(readStatus);
            }
        }
    }
}