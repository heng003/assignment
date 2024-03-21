package com.firstapp.hootnholler;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.firstapp.hootnholler.adapter.Student_Classroom_List;
import com.firstapp.hootnholler.databinding.ActivityEditAccountBinding;
import com.firstapp.hootnholler.entity.Classroom;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class Parent_Statistic_Fragment extends Fragment {

    private TextView AvgPerformance, AvgAtRisk, AvgPositive, AvgNegative;
    private String studentUID;
    private DatabaseReference Database = FirebaseDatabase.getInstance().getReference();
    private Calendar calendar;
    private double AverageQuizScore, AverageAssignmentScore, AveragePerformanceScore;
    private Spinner classroomSpinner;
    private Student_Classroom_List adapter;
    private ArrayList<Classroom> classroomList;
    private Classroom selectedClassroom;
    private DecimalFormat decimalFormat = new DecimalFormat("#.##");

    public Parent_Statistic_Fragment(String studentUID) {
        this.studentUID = studentUID;
    }

    public Parent_Statistic_Fragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_parent__statistic_, container, false);
        classroomSpinner = view.findViewById(R.id.classroom_list);
        AvgPerformance = view.findViewById(R.id.avgPerformance);
        AvgPositive = view.findViewById(R.id.avg_positive);
        AvgNegative = view.findViewById(R.id.avg_negative);

        classroomList = new ArrayList<>();
        adapter = new Student_Classroom_List(getActivity(), android.R.layout.simple_spinner_item, classroomList);
        classroomSpinner.setAdapter(adapter);
        getClassroom();
        this.classroomSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedClassroom = classroomList.get(i);
                getAveragePerformance();
                getFeedback();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        return view;
    }

    public void getClassroom(){
        classroomList.clear();
        this.Database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot joinedClassSnapshot : snapshot.child("Student").child(studentUID).child("JoinedClass").getChildren()) {
                    Classroom classroom = snapshot.child("Classroom").child(joinedClassSnapshot.getKey()).getValue(Classroom.class);
                    classroom.classCode = joinedClassSnapshot.getKey();
                    classroomList.add(classroom);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void getAveragePerformance() {
        // Initialize variables to store weekly average scores
        AverageAssignmentScore = 0;
        AverageQuizScore = 0;
        AveragePerformanceScore = 0;

        // Listen for a single value event from the database
        this.Database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Lists to store set keys and calculate quiz scores
                ArrayList<String> setKey = new ArrayList<>();
                double totalSetNum = 0, totalTask = 0, totalCompleted = 0, totalMarkSet = 0;

                // Calculate completion percentage for assignments
                for (DataSnapshot assignmentSnapshot : snapshot.child("Classroom").child(selectedClassroom.getClassCode()).child("Assignment").getChildren()) {
                    totalTask++;
                    for (DataSnapshot submissionSnapShot : assignmentSnapshot.child("Submission").getChildren()) {
                        if (submissionSnapShot.getKey().equals(studentUID)) {
                            totalCompleted++;
                            break;
                        }
                    }
                }

                // Calculate average assignment score
                if (totalTask != 0) {
                   AverageAssignmentScore = totalCompleted / totalTask * 100;
                }

                // Loop through subjects (quiz score)
                for (DataSnapshot subjectSnapshot : snapshot.child("Student").child(studentUID).child("quiz").child(selectedClassroom.getClassCode()).getChildren()) {
                    setKey.clear();

                    // Collect set keys for the subject
                    for (DataSnapshot setSubjectSnapshot : subjectSnapshot.child("setKeyInfo").getChildren()) {
                        setKey.add(setSubjectSnapshot.getKey());
                    }

                    // Loop through sets to calculate quiz scores
                    for (DataSnapshot setSnapshot : snapshot.child("Categories").child(subjectSnapshot.getKey()).child("Sets").getChildren()) {
                        // Skip sets not associated with the subject
                        if (!setKey.contains(setSnapshot.getKey())) {
                            continue;
                        }

                        // Calculate quiz score based on answers
                        for (DataSnapshot scoreSnapshot : setSnapshot.child("Answers").getChildren()) {
                            if (scoreSnapshot.getKey().equals(studentUID)) {
                                totalMarkSet += scoreSnapshot.child("percentage").getValue(Double.class);
                                break;
                            }
                        }
                        totalSetNum++;
                    }
                }

                // Calculate average quiz score
                if (totalSetNum != 0) {
                    AverageQuizScore = totalMarkSet / totalSetNum;
                }

                // Calculate overall average performance score
                AveragePerformanceScore = AverageAssignmentScore * 0.40 + AverageQuizScore * 0.60;

                // Display the average performance score
                AvgPerformance.setText(decimalFormat.format(AveragePerformanceScore) + "%");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    public void getFeedback(){
        this.Database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int numPositive = 0, numNegative = 0;

                // fetch the feedback from database
                for(DataSnapshot feedbackSnapshot : snapshot.child("Feedback")
                        .child(selectedClassroom.getClassCode())
                        .child(studentUID)
                        .getChildren()){

                        if(feedbackSnapshot.child("positive").getValue(Boolean.class) == null){
                            continue;
                        }
                        //if the status of feedback is positive,number of positive feedback +1
                        if(feedbackSnapshot.child("positive").getValue(Boolean.class)){
                            numPositive ++;
                        }
                        //if the status of feedback is negative,number of negative feedback +1
                        else if(!feedbackSnapshot.child("positive").getValue(Boolean.class)){
                            numNegative ++;
                        }

                }
                AvgPositive.setText(String.valueOf(numPositive));
                AvgNegative.setText(String.valueOf(numNegative));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}