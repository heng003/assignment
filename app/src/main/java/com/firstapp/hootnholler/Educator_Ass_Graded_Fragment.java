package com.firstapp.hootnholler;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firstapp.hootnholler.adapter.Asgm_ArrayAdapter;
import com.firstapp.hootnholler.entity.Assignment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Educator_Ass_Graded_Fragment extends Fragment {
    Asgm_ArrayAdapter Adapter;
    ArrayList<Assignment> asgmList = new ArrayList<>();
    RecyclerView recyclerView;
    String currentClassCode;
    DatabaseReference database;
    TextView noAss;
    FirebaseAuth mauth = FirebaseAuth.getInstance();
    String uid = mauth.getUid().toString();
    final String key="3";

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_teacher__ass__graded_, container, false);
        Bundle arguments = getArguments();
        if (arguments != null) {
            currentClassCode = arguments.getString("classCode");
        }

        recyclerView = view.findViewById(R.id.AssList);
        noAss = view.findViewById(R.id.TVnoAss);
        database = FirebaseDatabase.getInstance().getReference();
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        Adapter = new Asgm_ArrayAdapter(getActivity(), asgmList, currentClassCode,key);
        recyclerView.setAdapter(Adapter);

        fetchGradedAssignments();

        return view;
    }

    private void fetchGradedAssignments() {
        DatabaseReference assignmentsRef = database.child("Classroom")
                .child(currentClassCode)
                .child("Assignment");

        assignmentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                asgmList.clear();

                for (DataSnapshot assignmentSnapshot : snapshot.getChildren()) {
                    Assignment assignment = assignmentSnapshot.getValue(Assignment.class);
                    assignment.setAssKey(assignmentSnapshot.getKey());

                    if (assignment != null) {
                        // Check if submission exists under the assignment
                        if (assignmentSnapshot.child("Submission").exists()) {
                            boolean allGraded = true;

                            // Check if each student's submission has a "score" key
                            for (DataSnapshot submissionSnapshot : assignmentSnapshot.child("Submission").getChildren()) {
                                if (!submissionSnapshot.child("score").exists()) {
                                    // If any student's submission doesn't have a "score" key, it's not graded
                                    allGraded = false;
                                    break;
                                }
                            }

//                            asgmList.add(assignment);


                            if (allGraded) {
                                asgmList.add(assignment);
                            }
                        }
                    }
                }

                if (asgmList.isEmpty()) {
                    noAss.setVisibility(View.VISIBLE);
                } else {
                    noAss.setVisibility(View.GONE);
                }

                Adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle onCancelled
            }
        });
    }
}