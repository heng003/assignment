package com.firstapp.hootnholler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.firstapp.hootnholler.Models.QuestionModel;
import com.firstapp.hootnholler.databinding.ActivityQuizEducatorAddQuestionBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Educator_Quiz_Add_Question_Activity extends AppCompatActivity {

    ActivityQuizEducatorAddQuestionBinding binding;
    FirebaseDatabase database;
    DatabaseReference referenceQuestions;
    int questionNo;
    String uid, keyCtg, keySet, keyQuestion, imageUrl;
    boolean blockUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQuizEducatorAddQuestionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
        keyCtg = intent.getStringExtra("key");
        keySet = intent.getStringExtra("keySet");
        keyQuestion = intent.getStringExtra("keyQuestion");
        questionNo = intent.getIntExtra("questionNo", -1);
        imageUrl = intent.getStringExtra("categoryImage");
        blockUpdate = intent.getBooleanExtra("blockUpdate", false);

        if(blockUpdate) {
            binding.uploadBtn.setVisibility(View.GONE);
        }

        database = FirebaseDatabase.getInstance();
        referenceQuestions = database.getReference().child("Categories").child(keyCtg).child("Sets").child(keySet).child("Questions");

        if(questionNo == -1) {
            Toast.makeText(this, "Missing data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if(keyQuestion != null) {
            referenceQuestions.child(keyQuestion).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()) {
                        QuestionModel model = snapshot.getValue(QuestionModel.class);
                        binding.enterQuestion.setText(model.getQuestion());
                        binding.enterAnsA.setText(model.getOptionA());
                        binding.enterAnsB.setText(model.getOptionB());
                        binding.enterAnsC.setText(model.getOptionC());
                        binding.enterAnsD.setText(model.getOptionD());

                        String[] options = {model.getOptionA(), model.getOptionB(), model.getOptionC(), model.getOptionD()};
                        for(int i=0; i<options.length; i++) {
                            if(model.getCorrectAns().equals(options[i])) {
                                binding.RGAnswers.check(binding.RGAnswers.getChildAt(i).getId());
                                break;
                            }
                        }
                        binding.uploadBtn.setText("Update");
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(Educator_Quiz_Add_Question_Activity.this, "Question not exist", Toast.LENGTH_SHORT).show();
                }
            });
        }

        binding.textView.setText("Question " + questionNo);

        binding.uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String questionText = binding.enterQuestion.getText().toString().trim();
                //educator does not enter quetsion
                if (questionText.isEmpty()) {
                    Toast.makeText(Educator_Quiz_Add_Question_Activity.this, "Please enter the question", Toast.LENGTH_SHORT).show();
                    return;
                }

                int checkedRadioButtonId = binding.RGAnswers.getCheckedRadioButtonId();

                //educator does not select the correct option
                if (checkedRadioButtonId == -1) {
                    Toast.makeText(Educator_Quiz_Add_Question_Activity.this, "Please mark the correct option", Toast.LENGTH_SHORT).show();
                    return;
                }

                EditText selectedEditText = null;

                if (checkedRadioButtonId == R.id.A) {
                    selectedEditText = binding.enterAnsA;
                } else if (checkedRadioButtonId == R.id.B) {
                    selectedEditText = binding.enterAnsB;
                } else if (checkedRadioButtonId == R.id.C) {
                    selectedEditText = binding.enterAnsC;
                } else if (checkedRadioButtonId == R.id.D) {
                    selectedEditText = binding.enterAnsD;
                }

                String correctAnswer;
                if (selectedEditText != null) {
                    correctAnswer = selectedEditText.getText().toString().trim();
                } else {
                    Toast.makeText(Educator_Quiz_Add_Question_Activity.this, "Please mark the correct option", Toast.LENGTH_SHORT).show();
                    return;
                }

                String optionA = ((EditText) binding.enterAnsA).getText().toString().trim();
                String optionB = ((EditText) binding.enterAnsB).getText().toString().trim();
                String optionC = ((EditText) binding.enterAnsC).getText().toString().trim();
                String optionD = ((EditText) binding.enterAnsD).getText().toString().trim();

                //create a question model and insert the details inside the model
                QuestionModel model = new QuestionModel();
                model.setQuestion(questionText);
                model.setOptionA(optionA);
                model.setOptionB(optionB);
                model.setOptionC(optionC);
                model.setOptionD(optionD);
                model.setCorrectAns(correctAnswer);

                //check whether educator want to create a new question or modify a existing question
                if(keyQuestion != null) {
                    //modify an existing question in database
                    model.setKeyQuestion(keyQuestion);
                    referenceQuestions.child(keyQuestion).setValue(model).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "Question updated successfully", Toast.LENGTH_SHORT).show();

                            } else {
                                Toast.makeText(getApplicationContext(), "Failed to update question. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else {
                    //insert a new question to database
                    String questionKey = referenceQuestions.push().getKey();
                    model.setKeyQuestion(questionKey);
                    referenceQuestions.child(questionKey).setValue(model).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "Question added successfully", Toast.LENGTH_SHORT).show();

                            } else {
                                Toast.makeText(getApplicationContext(), "Failed to add question. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                //navigate to quiz question activity
                Intent intent = new Intent(Educator_Quiz_Add_Question_Activity.this, Educator_Quiz_Question_Activity.class);
                intent.putExtra("uid", uid);
                intent.putExtra("key", keyCtg);
                intent.putExtra("keySet", keySet);
                intent.putExtra("categoryImage", imageUrl);
                startActivity(intent);
                finish();

            }
        });

        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Educator_Quiz_Add_Question_Activity.this, Educator_Quiz_Question_Activity.class);
                intent.putExtra("uid", uid);
                intent.putExtra("key", keyCtg);
                intent.putExtra("keySet", keySet);
                intent.putExtra("categoryImage", imageUrl);
                startActivity(intent);
                finish();
            }
        });
    }
}