package com.firstapp.hootnholler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.firstapp.hootnholler.adapter.QuizCategoryAdapter;
import com.firstapp.hootnholler.Models.QuizModel;
import com.firstapp.hootnholler.Models.TaskStatus;
import com.firstapp.hootnholler.Models.TaskToDo;
import com.firstapp.hootnholler.databinding.ActivityQuizStudentToDoBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class Student_Quiz_To_Do_Activity extends AppCompatActivity {

    ActivityQuizStudentToDoBinding binding;
    FirebaseDatabase database;
    DatabaseReference referenceStudentQuiz;
    ArrayList<String> ctgList;
    QuizCategoryAdapter ctgAdapter;
    String uid;
    TaskStatus taskStatus;
    TaskToDo taskToDo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQuizStudentToDoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        taskToDo = intent.getParcelableExtra("taskToDo");
        taskStatus = intent.getParcelableExtra("taskStatus");
        uid = intent.getStringExtra("uid");

        binding.allCompletedTV.setVisibility(View.INVISIBLE);

        ctgList = new ArrayList<>();
        database = FirebaseDatabase.getInstance();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.recyQuizCtgToDo.setLayoutManager(layoutManager);

        ctgAdapter = new QuizCategoryAdapter(this, ctgList, uid);
        binding.recyQuizCtgToDo.setAdapter(ctgAdapter);

        if(taskToDo.getQuizToDo().isEmpty()) {
            binding.allCompletedTV.setVisibility(View.VISIBLE);
        } else {
            ctgList.clear();
            ctgList.addAll(taskToDo.getQuizToDo().keySet());
            ctgAdapter.notifyDataSetChanged();
            for(String ctg : ctgList) {
                ArrayList<String> quizzes = taskToDo.getQuizToDo().get(ctg);

                for(int i=0; i<quizzes.size(); i++) {
                    String status = taskStatus.getHashMap().get(quizzes.get(i));
                    addQuizModel(status, ctg, quizzes.get(i));
                }
            }
        }

        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Student_MainActivity.class);
                intent.putExtra("FRAGMENT_TO_LOAD", "student_Quiz_Fragment"); // Pass the fragment tag or ID here
                startActivity(intent);
                finish();
            }
        });
    }

    private void addQuizModel(String status, String ctgKey, String setKey) {
        QuizModel quizModel = new QuizModel();
        referenceStudentQuiz = database.getReference()
                .child("Student").child(uid)
                .child("quiz");
        referenceStudentQuiz.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    for(DataSnapshot classSnapshot : snapshot.getChildren()) {
                        //fetch due date for every to do quiz
                        referenceStudentQuiz.child(classSnapshot.getKey()).child(ctgKey).child("setKeyInfo").child(setKey).child("dueDate").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()) {
                                    Long timestamp = snapshot.getValue(Long.class);
                                    if(timestamp != null) {
                                        Date date = new Date(timestamp);

                                        //convert the timestamp retrieved in database to due date
                                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                        String formattedDate = sdf.format(date);

                                        quizModel.setCtgKey(ctgKey);
                                        quizModel.setSetKey(setKey);
                                        quizModel.setDueDate(formattedDate);
                                        quizModel.setStatus(status);

                                        //fetch the progress of the quizzes
                                        database.getReference().child("Categories").child(ctgKey).child("Sets").child(setKey).child("setName").addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if(snapshot.exists()) {
                                                    quizModel.setTitle(snapshot.getValue(String.class));
                                                    if(quizModel.getStatus().equals("In progress")) {

                                                        //fetch the progress for the in progress quiz
                                                        database.getReference().child("Categories").child(ctgKey).child("Sets").child(setKey)
                                                                .child("Answers").child(uid).child("progress").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                        if(snapshot.exists()) {
                                                                            Integer snapshotValue = snapshot.getValue(Integer.class);
                                                                            if(snapshotValue != null) {
                                                                                //add to do item in recycler view
                                                                                quizModel.setProgress(snapshotValue);
                                                                                ctgAdapter.addQuizModel(quizModel, ctgKey);
                                                                            }
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                                    }
                                                                });
                                                    } else {
                                                        ctgAdapter.addQuizModel(quizModel, ctgKey);
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}