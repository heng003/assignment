package com.firstapp.hootnholler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.firstapp.hootnholler.adapter.QuizStudentAdapter;
import com.firstapp.hootnholler.Models.QuizStudentModel;
import com.firstapp.hootnholler.databinding.ActivityQuizEducatorReviewBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Educator_Quiz_Review_Activity extends AppCompatActivity implements RecyViewInterface {

    ActivityQuizEducatorReviewBinding binding;
    String uid, keyCtg, keySet, imageUrl, keyClassroom;
    FirebaseDatabase database;
    DatabaseReference referenceStudentList, referenceStudentAnswers;
    ArrayList<QuizStudentModel> list;
    QuizStudentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQuizEducatorReviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
        keyCtg = intent.getStringExtra("key");
        keySet = intent.getStringExtra("keySet");
        imageUrl = intent.getStringExtra("categoryImage");
        keyClassroom = intent.getStringExtra("classroomKey");

        list = new ArrayList<>();
        database = FirebaseDatabase.getInstance();
        referenceStudentList = database.getReference().child("Classroom").child(keyClassroom).child("StudentsJoined");
        referenceStudentAnswers = database.getReference().child("Categories").child(keyCtg).child("Sets").child(keySet).child("Answers");

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.recyReviewList.setLayoutManager(layoutManager);

        adapter = new QuizStudentAdapter(this, this, list);
        binding.recyReviewList.setAdapter(adapter);

        referenceStudentList.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                if(snapshot.exists()) {
                    for(DataSnapshot studentSnapshot : snapshot.getChildren()) {
                        String student = studentSnapshot.getKey();
                        QuizStudentModel model = new QuizStudentModel();

                        //fetch every students' names
                        database.getReference().child("Users").child(student).child("fullname").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()) {
                                    model.setStudentName(snapshot.getValue(String.class));

                                    //fetch completion status of every students
                                    referenceStudentAnswers.child(student).child("status").addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if(snapshot.exists()) {
                                                String status = snapshot.getValue(String.class);
                                                model.setStudentQuizStatus(status);

                                                if(status.equals("Completed")) {
                                                    //fetch the student's score
                                                    referenceStudentAnswers.child(student).child("score").addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            if(snapshot.exists()) {
                                                                Integer score = snapshot.getValue(Integer.class);
                                                                model.setScore(String.valueOf(score));
                                                                //add the student's data to recycler view
                                                                list.add(model);
                                                                adapter.notifyItemInserted(list.size());
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                        }
                                                    });
                                                } else {
                                                    //add the student's data to recycler view
                                                    list.add(model);
                                                    adapter.notifyItemInserted(list.size());
                                                }

                                            } else {
                                                model.setStudentQuizStatus("Incomplete");
                                                //add the student's data to recycler view
                                                list.add(model);
                                                adapter.notifyItemInserted(list.size());
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });

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

        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Educator_Quiz_Review_Activity.this, Educator_Quiz_Post_Activity.class);
                intent.putExtra("uid", uid);
                intent.putExtra("key", keyCtg);
                intent.putExtra("keySet", keySet);
                intent.putExtra("categoryImage", imageUrl);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onItemLongClick(int position) {

    }

    @Override
    public void onItemClick(int position) {

    }
}