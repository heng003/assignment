package com.firstapp.hootnholler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.firstapp.hootnholler.Models.QuestionModel;
import com.firstapp.hootnholler.Models.QuizCandidate;
import com.firstapp.hootnholler.Models.QuizRanking;
import com.firstapp.hootnholler.databinding.ActivityQuizStudentLeaderboardBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Student_Quiz_Leaderboard_Activity extends AppCompatActivity {

    ActivityQuizStudentLeaderboardBinding binding;
    FirebaseDatabase database;
    DatabaseReference referenceSet, referenceAns, referencePercentage;
    String uid, keyCtg, keySet, setName;
    ArrayList<String> keySetList, keyQuestionList;
    int score;
    QuizRanking ranking;
    QuizCandidate currCandidate;
    ArrayList<QuestionModel> questions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQuizStudentLeaderboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
        keyCtg = intent.getStringExtra("keyCtg");
        keySet = intent.getStringExtra("keySet");
        keySetList = intent.getStringArrayListExtra("keySetList");
        questions = intent.getParcelableArrayListExtra("questions");
        setName = intent.getStringExtra("setName");

        database = FirebaseDatabase.getInstance();
        referenceSet = database.getReference().child("Categories").child(keyCtg).child("Sets").child(keySet);
        referenceAns = referenceSet.child("Answers").child(uid);
        referencePercentage = referenceAns.child("percentage");


        if(keySetList == null) {
            keySetList = new ArrayList<>();
        }

        referenceAns.child("score").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    Integer totalCorr = snapshot.getValue(Integer.class);
                    if(totalCorr != null) {
                        score = totalCorr;
                        currCandidate = new QuizCandidate(uid, score);
                        obtainRanking();
                    }
                } else {
                    referenceAns.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()) {
                                score = 0;
                                int processedQuestions = 0;
                                for(DataSnapshot quesDataSnapshot : snapshot.getChildren()) {
                                    Boolean check = quesDataSnapshot.child("correctness").getValue(Boolean.class);
                                    processedQuestions++;
                                    if(check != null && check) {
                                        score++;
                                    }

                                    if(processedQuestions == questions.size()) {
                                        referenceAns.child("score").setValue(score).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(Student_Quiz_Leaderboard_Activity.this, "Error in writing score", Toast.LENGTH_SHORT).show();
                                            }
                                        }).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                double scorePercent = ((double) (score) / questions.size()) * 100;
                                                referencePercentage.setValue((int)scorePercent);
                                                currCandidate = new QuizCandidate(uid, score);
                                                obtainRanking();
                                            }
                                        });
                                    }

                                }

                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(Student_Quiz_Leaderboard_Activity.this, "Error in calculating score", Toast.LENGTH_SHORT).show();
                        }
                    });
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Student_Quiz_Leaderboard_Activity.this, "Error in retrieving score", Toast.LENGTH_SHORT).show();
            }
        });

        binding.setName.setText(setName);

        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (keySetList.isEmpty()) {
                    Intent intent = new Intent(getApplicationContext(), Student_MainActivity.class);
                    intent.putExtra("FRAGMENT_TO_LOAD", "student_Quiz_Fragment"); // Pass the fragment tag or ID here
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(Student_Quiz_Leaderboard_Activity.this, Student_Quiz_Set_Activity.class);
                    intent.putExtra("uid", uid);
                    intent.putExtra("keyCtg", keyCtg);
                    intent.putExtra("keySetList", keySetList);
                    startActivity(intent);
                    finish();
                }

            }
        });

        binding.review.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Student_Quiz_Leaderboard_Activity.this, Student_Quiz_Review_Activity.class);

                intent.putExtra("uid", uid);
                intent.putExtra("setName", setName);
                intent.putExtra("keyCtg", keyCtg);
                intent.putExtra("keySet", keySet);
                intent.putExtra("keySetList", keySetList);
                intent.putExtra("keyQuestionList", keyQuestionList);
                intent.putParcelableArrayListExtra("questions", questions);
                startActivity(intent);
            }
        });

    }

    public void addAndSaveRanking() {
        //add current student to the ranking list
        ranking.addCandidate(currCandidate);
        //sort the list of students based on the descending order of score
        ranking.sortRanking();
        //save the latest ranking to database
        referenceSet.child("Ranking").setValue(ranking.getCandidates()).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Student_Quiz_Leaderboard_Activity.this, "Error in writing ranking", Toast.LENGTH_SHORT).show();
            }
        }).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                //display ranking of the student
                displayRanking();
            }
        });
    }

    public void obtainRanking() {
        ranking = new QuizRanking();
        ranking.getCandidates().clear();
        referenceSet.child("Ranking").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    int processedCandidates = 0;
                    Long totalCandidates = snapshot.getChildrenCount();

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String uid = dataSnapshot.child("uid").getValue(String.class);
                        Long score = dataSnapshot.child("score").getValue(Long.class);

                        if (uid != null && score != null) {
                            QuizCandidate quizCandidate = new QuizCandidate(uid, score.intValue());
                            ranking.addCandidate(quizCandidate);
                            processedCandidates++;
                        }

                        if(processedCandidates == totalCandidates) {
                            if (checkExistence()) {
                                displayRanking();
                            }
                            else {
                                addAndSaveRanking();
                            }
                        }
                    }
                } else {
                    addAndSaveRanking();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Student_Quiz_Leaderboard_Activity.this, "Error in retrieving ranking", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public boolean checkExistence() {
        for(QuizCandidate candidate : ranking.getCandidates()) {
            if (candidate.getUid().equals(currCandidate.getUid())) {
                return true;
            }
        }
        return  false;
    }

    public int getPosition(String uid) {
        ArrayList<QuizCandidate> rankingList = ranking.getCandidates();
        for(int i=0; i<rankingList.size(); i++) {
            if(rankingList.get(i).getUid().equals(uid)) {
                return i;
            }
        }
        return -1;
    }

    //display the top three ranking
    public void displayRanking() {
        int position = getPosition(currCandidate.getUid()) + 1;
        displayRankingText(position, ranking.getCandidates().size());

        if (ranking.getCandidates().size() >= 1) {
            setName(binding.nameFst, ranking.getCandidates().get(0).getUid());
        }
        if (ranking.getCandidates().size() >= 2) {
            setName(binding.nameSnd, ranking.getCandidates().get(1).getUid());
        }
        if (ranking.getCandidates().size() >= 3) {
            setName(binding.nameTrd, ranking.getCandidates().get(2).getUid());
        }
    }

    //display the current student's ranking and score
    public void displayRankingText(int position, int totalCandidate) {
        binding.setMyRanking.setText(position + "/" + totalCandidate);
        binding.setMyScore.setText(score + "/" + questions.size());
    }

    public void setName(TextView view, String userID) {
        DatabaseReference userRef = database.getReference().child("Users").child(userID).child("fullname");
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.getValue(String.class);
                    view.setText(name);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Student_Quiz_Leaderboard_Activity.this, "Error in accessing top 3 students' names", Toast.LENGTH_SHORT).show();
            }
        });
    }

}