package com.firstapp.hootnholler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firstapp.hootnholler.Models.QuestionModel;
import com.firstapp.hootnholler.databinding.ActivityQuizStudentReviewBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Student_Quiz_Review_Activity extends AppCompatActivity {

    ActivityQuizStudentReviewBinding binding;
    FirebaseDatabase database;
    DatabaseReference referenceSet, referenceAnswer;
    String uid, keyCtg, keySet, setName;
    ArrayList<String> keySetList;
    ArrayList<QuestionModel> list;
    QuestionModel model;
    int queIndex;
    Button[] options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQuizStudentReviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
        setName = intent.getStringExtra("setName");
        keyCtg = intent.getStringExtra("keyCtg");
        keySet = intent.getStringExtra("keySet");
        queIndex = intent.getIntExtra("queIndex", 0);
        list = intent.getParcelableArrayListExtra("questions");
        keySetList = intent.getStringArrayListExtra("keySetList");

        model = list.get(queIndex);

        database = FirebaseDatabase.getInstance();
        referenceSet = database.getReference().child("Categories").child(keyCtg).child("Sets").child(keySet);
        referenceAnswer = referenceSet.child("Answers").child(uid).child(model.getKeyQuestion());

        model = list.get(queIndex);
        options = new Button[]{binding.answerA, binding.answerB, binding.answerC, binding.answerD};
        binding.questionTV.setText(model.getQuestion());
        options[0].setText(model.getOptionA());
        options[1].setText(model.getOptionB());
        options[2].setText(model.getOptionC());
        options[3].setText(model.getOptionD());

        binding.questionNo.setText((queIndex + 1) + "/" + list.size());

        if(list.size() == (queIndex + 1)) {
            binding.submit.setText("View Ranking");
        } else {
            binding.submit.setText("Next");
        }

        if((queIndex - 1) < 0) {
            binding.previous.setVisibility(View.GONE);
        }

        if((queIndex + 1) >= list.size()) {
            binding.next.setVisibility(View.GONE);
        }

        for (Button option : options) {
            if(model.getCorrectAns().equals(option.getText().toString())) {
                updateCorrectButtonColor(option);
                break;
            }
        }

        //fetch student's answer and correct answer
        referenceAnswer.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    String optionSelected = snapshot.child("optionSelected").getValue(String.class);
                    Boolean correctness = snapshot.child("correctness").getValue(Boolean.class);
                    if(!correctness) {
                        for (Button option : options) {
                            if(option.getText().toString().equals(optionSelected)) {
                                updateWrongButtonColor(option);
                            }
                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Student_Quiz_Review_Activity.this, "Error in checking correctness", Toast.LENGTH_SHORT).show();
            }
        });

        binding.previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passToNextQue(queIndex - 1);
            }
        });

        binding.next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passToNextQue(queIndex + 1);
            }
        });

        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Student_Quiz_Review_Activity.this, Student_Quiz_Leaderboard_Activity.class);
                intent.putExtra("uid", uid);
                intent.putExtra("keyCtg", keyCtg);
                intent.putExtra("keySet", keySet);
                intent.putExtra("keySetList", keySetList);
                intent.putExtra("setName", setName);
                intent.putParcelableArrayListExtra("questions", list);
                intent.putExtra("uid", uid);

                startActivity(intent);
                finish();
            }
        });

        binding.submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(queIndex + 1 == list.size()) {
                    Intent intent = new Intent(Student_Quiz_Review_Activity.this, Student_Quiz_Leaderboard_Activity.class);
                    intent.putExtra("uid", uid);
                    intent.putExtra("keyCtg", keyCtg);
                    intent.putExtra("keySet", keySet);
                    intent.putExtra("keySetList", keySetList);
                    intent.putExtra("setName", setName);
                    intent.putParcelableArrayListExtra("questions", list);
                    intent.putExtra("uid", uid);

                    startActivity(intent);
                } else {
                    passToNextQue(queIndex + 1);
                }
            }
        });
    }

    private void passToNextQue(int index) {
        Intent intent = new Intent(Student_Quiz_Review_Activity.this, Student_Quiz_Review_Activity.class);
        intent.putExtra("uid", uid);
        intent.putExtra("keyCtg", keyCtg);
        intent.putExtra("keySet", keySet);
        intent.putExtra("queIndex", index);
        intent.putParcelableArrayListExtra("questions", list);
        intent.putStringArrayListExtra("keySetList", keySetList);
        intent.putExtra("setName", setName);
        intent.putExtra("uid", uid);
        startActivity(intent);
    }

    //mark the correct option as green color
    public void updateCorrectButtonColor(Button selectedOption) {
        for (Button option : options) {
            if (option == selectedOption) {
                option.setBackgroundResource(R.drawable.correctans);
                option.setTextColor(-16777216);
            }
        }
    }

    //mark the wrong answer to red color
    public void updateWrongButtonColor(Button selectedOption) {
        for (Button option : options) {
            if (option == selectedOption) {
                option.setBackgroundResource(R.drawable.wrongans);
                option.setTextColor(-16777216);
            }
        }
    }

}