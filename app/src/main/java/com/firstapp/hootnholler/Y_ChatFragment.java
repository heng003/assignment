package com.firstapp.hootnholler;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firstapp.hootnholler.adapter.Conversation_ArrayAdapter;
import com.firstapp.hootnholler.entity.Conversation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Y_ChatFragment extends Fragment {

    private DatabaseReference database = FirebaseDatabase.getInstance().getReference();
    private RecyclerView conversationList;
    private Conversation_ArrayAdapter adapter;
    private ArrayList<Conversation> conversations;
    private String userUID;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public Y_ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        //find RecyclerView
        conversationList = view.findViewById(R.id.conversationlist);

        //setup linearLayoutManager for RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        conversationList.setLayoutManager(layoutManager);

        //array list to store conversations and create adapter
        conversations = new ArrayList<>();
        adapter = new Conversation_ArrayAdapter(getActivity(), conversations);

        //set adapter for RecyclerView
        conversationList.setAdapter(adapter);

        //get the user's UID using FirebaseAuth
        userUID = mAuth.getUid();

        //SearchView in the layout
        SearchView searchView = view.findViewById(R.id.searchView);

        //listener for the SearchView to handle query text changes
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //handle search submission if needed
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //handle search text change by filtering conversations
                filterConversations(newText);
                return true;
            }
        });

        // Fetch and display initial conversations
        getConversation();

        return view;
    }


    //method to filter conversations based on a search query
    private void filterConversations(String searchText) {
        //new ArrayList to store filtered conversations
        ArrayList<Conversation> filteredList = new ArrayList<>();

        //loop through the existing conversations
        for (Conversation conversation : conversations) {
            //check if the conversation name contains the search text (case-insensitive)
            if (conversation.getName().toLowerCase().contains(searchText.toLowerCase())) {
                //matches the search criteria, add it to the filtered list
                filteredList.add(conversation);
            }
        }

        //update the adapter with the filtered list
        adapter.updateConversation(filteredList);

        //notify the adapter that the data set has changed
        adapter.notifyDataSetChanged();
    }


    public void getConversation(){
        //ValueEventListener for changes in the database
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //clear existing conversations list to avoid duplicate
                conversations.clear();

                //loop through the joined group chat keys for the user
                for (DataSnapshot grpChatKeySnapshot : snapshot.child("Users").child(userUID).child("joinedGrpChatKey").getChildren()) {
                    //check if the user has joined this group chat
                    if(grpChatKeySnapshot.getValue(Boolean.class)){
                        //get the key of the group chat
                        String grpChatKey = grpChatKeySnapshot.getKey();

                        //make sure the group chat key is not null
                        if(grpChatKey != null){
                            //retrieve the conversation details for the group chat from the database
                            DataSnapshot conversationSnapshot = snapshot.child("Conversation")
                                    .child("GroupChat").child(grpChatKey);

                            //deserialize the conversation data into a Conversation object
                            Conversation conversation = conversationSnapshot.getValue(Conversation.class);

                            //check if the conversation object is not null
                            if(conversation != null){
                                //set the conversation ID and add it to the list
                                conversation.setConversationID(grpChatKey);
                                conversations.add(conversation);
                            }
                        }
                    }
                    //notify the adapter that the data set has changed
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //handle database error if needed
            }
        });
    }
}