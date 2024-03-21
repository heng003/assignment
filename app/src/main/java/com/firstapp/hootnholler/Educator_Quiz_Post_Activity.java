package com.firstapp.hootnholler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.firstapp.hootnholler.adapter.ClassroomAdapter;
import com.firstapp.hootnholler.Models.ClassroomModel;
import com.firstapp.hootnholler.Models.PostedQuizModel;
import com.firstapp.hootnholler.Models.QuizCandidate;
import com.firstapp.hootnholler.databinding.ActivityQuizEducatorPostBinding;
import com.firstapp.hootnholler.entity.Classroom;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Educator_Quiz_Post_Activity extends AppCompatActivity implements RecyViewInterface {

    ActivityQuizEducatorPostBinding binding;
    FirebaseDatabase database;
    DatabaseReference referenceClassroom, referenceEducatorClassroom, referencePostedClassroom, referencePostedSet, referenceAnswers, referenceRanking;
    String uid, keyCtg, keySet, imageUrl;
    ArrayList<String> classKeyList;
    ArrayList<ClassroomModel> list;
    ClassroomAdapter adapter;
    long timestamp;
    Dialog setDateDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQuizEducatorPostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
        keyCtg = intent.getStringExtra("key");
        keySet = intent.getStringExtra("keySet");
        imageUrl = intent.getStringExtra("categoryImage");

        list = new ArrayList<>();
        classKeyList = new ArrayList<>();
        GridLayoutManager layoutManager = new GridLayoutManager(this,2);
        binding.recyClass.setLayoutManager(layoutManager);

        adapter = new ClassroomAdapter(this, this,list);
        binding.recyClass.setAdapter(adapter);

        setDateDialog = new Dialog(this);
        setDateDialog.setContentView(R.layout.item_select_due_date_dialog);

        if(setDateDialog.getWindow()!=null) {
            setDateDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            setDateDialog.setCancelable(true);
            setDateDialog.setCanceledOnTouchOutside(true);
        }

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String currentDate = dateFormat.format(calendar.getTime());
        ((TextView)setDateDialog.findViewById(R.id.setCurrentDate)).setText(currentDate);

        database = FirebaseDatabase.getInstance();
        referenceEducatorClassroom = database.getReference().child("Educator").child(uid).child("Classroom");
        referencePostedClassroom = database.getReference().child("Categories").child(keyCtg).child("Sets").child(keySet).child("postedClassroom");
        referencePostedSet = database.getReference().child("Categories").child(keyCtg).child("postedSet");
        referenceRanking = database.getReference().child("Categories").child(keyCtg).child("Sets").child(keySet).child("Ranking");
        referenceAnswers = database.getReference().child("Categories").child(keyCtg).child("Sets").child(keySet).child("Answers");
        referenceClassroom = database.getReference().child("Classroom");

        referencePostedClassroom.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    ArrayList<String> postedClassroom = (ArrayList<String>) snapshot.getValue();

                    referenceClassroom.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            list.clear();
                            classKeyList.clear();
                            if(dataSnapshot.exists()) {
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    if (snapshot.child("classOwner").getValue(String.class).equals(uid)) {

                                        String className = snapshot.child("className").getValue(String.class);
                                        String classKey = snapshot.getKey();
                                        classKeyList.add(classKey);

                                        ClassroomModel model = new ClassroomModel(classKey, className);
                                        list.add(model);

                                        adapter.notifyItemInserted(list.size());

                                        if(postedClassroom != null && postedClassroom.contains(classKey)) {
                                            int pos = list.size() - 1;
                                            adapter.setSelectedItems(pos);
                                        }

                                    }
                                }
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle error
                        }
                    });


                } else {
                    referenceClassroom.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            list.clear();
                            classKeyList.clear();
                            if(dataSnapshot.exists()) {
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    if (snapshot.child("classOwner").getValue(String.class).equals(uid)) {

                                        String className = snapshot.child("className").getValue(String.class);
                                        String classKey = snapshot.getKey();
                                        classKeyList.add(classKey);

                                        ClassroomModel model = new ClassroomModel(classKey, className);
                                        list.add(model);

                                        adapter.notifyItemInserted(list.size());

                                    }
                                }
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle error
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Educator_Quiz_Post_Activity.this, Educator_Quiz_Question_Activity.class);
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
        String classroomKey = list.get(position).getClassroomKey();

        referencePostedClassroom.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    ArrayList<String> postedClassroom = (ArrayList<String>) snapshot.getValue();

                    if(postedClassroom != null && !postedClassroom.contains(classroomKey)) {
                        Toast.makeText(Educator_Quiz_Post_Activity.this, "You haven't posted the quiz to this classroom.", Toast.LENGTH_SHORT).show();
                    } else if(postedClassroom != null && postedClassroom.contains(classroomKey)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(Educator_Quiz_Post_Activity.this);
                        builder.setTitle("Confirm Deletion of Posted Quiz From Students");
                        builder.setMessage("Are you sure you want to delete this quiz from students, including their answers and ranking?");

                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                postedClassroom.remove(classroomKey);
                                referencePostedClassroom.setValue(postedClassroom).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(postedClassroom.size() == 0) {
                                            referenceAnswers.setValue(null);
                                            referenceRanking.setValue(null);
                                            referencePostedSet.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    if(snapshot.exists()) {
                                                        ArrayList<String> postedSet = (ArrayList<String>) snapshot.getValue();
                                                        if(postedSet != null && postedSet.contains(keySet)) {
                                                            postedSet.remove(keySet);
                                                            referencePostedSet.setValue(postedSet);
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });
                                        } else {
                                            deleteStudentAnswerAndRanking(classroomKey, postedClassroom);
                                        }
                                        deleteQuizFromStudent(classroomKey, position);
                                    }
                                });
                            }
                        }).setNegativeButton("No", null);
                        builder.show();
                    }
                } else {
                    Toast.makeText(Educator_Quiz_Post_Activity.this, "You haven't posted the quiz to this classroom.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void deleteStudentAnswerAndRanking(String classroomKey, ArrayList<String> postedClassroom) {
        DatabaseReference studentReference = database.getReference().child("Classroom").child(classroomKey).child("StudentsJoined");
        studentReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Map<String, Boolean> studentsMap = (Map<String, Boolean>) snapshot.getValue();
                    ArrayList<String> students = new ArrayList<>(studentsMap.keySet());
                    ArrayList<String> studentsLeft = students;
                    int cntProgress = 0;
                    int ttlStudent = students.size();
                    for (String studentKey : students) {
                        cntProgress++;
                        if (studentKey != null) {
                            for(String otherClass : postedClassroom) {
                                int finalCntProgress = cntProgress;
                                database.getReference().child("Classroom").child(otherClass).child("StudentsJoined").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if(snapshot.exists()) {
                                            Map<String, Boolean> studentsMap = (Map<String, Boolean>) snapshot.getValue();
                                            ArrayList<String> studentJoined = new ArrayList<>(studentsMap.keySet());

                                            if(studentJoined!=null && studentJoined.contains(studentKey)) {
                                                studentsLeft.remove(studentKey);
                                            }
                                        }

                                        if(finalCntProgress == ttlStudent) {
                                            for(String student : studentsLeft) {
                                                referenceAnswers.child(student).setValue(null);
                                                deleteFromRanking(student);
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

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void deleteFromRanking(String student) {
        referenceRanking.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    ArrayList<QuizCandidate> quizCandidates = (ArrayList<QuizCandidate>) snapshot.getValue();
                    if(quizCandidates != null) {
                        for(int i=0; i<quizCandidates.size(); i++) {
                            if(quizCandidates.get(i).getUid().equals(student)) {
                                quizCandidates.remove(i);
                                break;
                            }

                            if(i == quizCandidates.size() - 1) {
                                referenceRanking.setValue(quizCandidates);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void deleteQuizFromStudent(String classroomKey, int position) {
        DatabaseReference studentReference = database.getReference().child("Classroom").child(classroomKey).child("StudentsJoined");

        studentReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int totalStudents = (int) snapshot.getChildrenCount();
                    final int[] processedStudents = {0};
                    classKeyList.remove(classroomKey);
                    adapter.deleteSelectedItems(position);

                    for (DataSnapshot studentSnapshot : snapshot.getChildren()) {
                        String studentKey = studentSnapshot.getKey();
                        if (studentKey != null) {
                            DatabaseReference studentQuizReference = database.getReference()
                                    .child("Student")
                                    .child(studentKey)
                                    .child("quiz")
                                    .child(classroomKey)
                                    .child(keyCtg)
                                    .child("setKeyInfo");

                            studentQuizReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        HashMap<String, SetInfo> setKeyInfo = (HashMap<String, SetInfo>) dataSnapshot.getValue();
                                        if(setKeyInfo.containsKey(keySet)) {
                                            setKeyInfo.remove(keySet);
                                        }
                                        if(setKeyInfo.size() == 0) {
                                            database.getReference().child("Student").child(studentKey).child("quiz").child(classroomKey).child(keyCtg).setValue(null);
                                        } else {
                                            studentQuizReference.setValue(setKeyInfo);
                                        }

//                                        processedStudents[0]++;
//                                        if (processedStudents[0] == totalStudents) {
//                                            adapter.deleteSelectedItems(position);
//                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
//                                    processedStudents[0]++;
//                                    if (processedStudents[0] == totalStudents) {
//                                        adapter.deleteSelectedItems(position);
//                                    }
                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onItemClick(int position) {
        final ArrayList<String>[] postedClassroom = new ArrayList[]{new ArrayList<>()};
        String classroomKey = list.get(position).getClassroomKey();
        final ArrayList<String>[] studentList = new ArrayList[]{new ArrayList<>()};

        referencePostedClassroom.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    postedClassroom[0] = (ArrayList<String>) snapshot.getValue();

                    if(postedClassroom[0] != null && postedClassroom[0].contains(classroomKey)) {
                        Intent intent = new Intent(Educator_Quiz_Post_Activity.this, Educator_Quiz_Review_Activity.class);
                        intent.putExtra("classroomKey", classroomKey);
                        intent.putExtra("uid", uid);
                        intent.putExtra("key", keyCtg);
                        intent.putExtra("keySet", keySet);
                        intent.putExtra("categoryImage", imageUrl);

                        startActivity(intent);
                    } else {
                        postedClassroom[0].add(classroomKey);
                        postToStudent(classroomKey, position, postedClassroom[0]);
                    }
                } else {
                    postedClassroom[0].add(classroomKey);
                    postToStudent(classroomKey, position, postedClassroom[0]);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void postToStudent(String classroomKey, int position, ArrayList<String> postedClassroom) {
        database.getReference("Classroom").child(classroomKey).child("StudentsJoined").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    Map<String, Boolean> studentsMap = (Map<String, Boolean>) snapshot.getValue();
                    ArrayList<String> studentList = new ArrayList<>(studentsMap.keySet());

                    if(studentList != null && studentList.size() > 0) {
                        //set due date
                        setDateDialog.show();
                        setDateDialog.findViewById(R.id.setDueDate).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                final Calendar currentDate = Calendar.getInstance();
                                int year = currentDate.get(Calendar.YEAR);
                                int month = currentDate.get(Calendar.MONTH);
                                int day = currentDate.get(Calendar.DAY_OF_MONTH);

                                DatePickerDialog datePickerDialog = new DatePickerDialog(Educator_Quiz_Post_Activity.this,
                                        new DatePickerDialog.OnDateSetListener() {
                                            @Override
                                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                                setDateDialog.dismiss();
                                                Calendar calendar = Calendar.getInstance();
                                                calendar.set(year, monthOfYear, dayOfMonth);
                                                timestamp = calendar.getTimeInMillis();

                                                adapter.setSelectedItems(position);

                                                for(String studentKey : studentList) {
                                                    database.getReference().child("Student").child(studentKey).child("quiz").child(classroomKey).child(keyCtg).addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            if(snapshot.exists()) {
                                                                PostedQuizModel categoryModel = snapshot.getValue(PostedQuizModel.class);
                                                                if(categoryModel.getSetKeyInfo() != null ) {
                                                                    categoryModel.addSetInfo(keySet, System.currentTimeMillis(), timestamp);
                                                                    postToStudent(classroomKey, categoryModel, studentKey, position, postedClassroom);
                                                                }
                                                            } else {
                                                                PostedQuizModel categoryModel = new PostedQuizModel();
                                                                categoryModel.setCtgKey(keyCtg);
                                                                categoryModel.addSetInfo(keySet, System.currentTimeMillis(), timestamp);
                                                                postToStudent(classroomKey, categoryModel, studentKey, position, postedClassroom);
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                        }
                                                    });
                                                }
                                                noticePostQuiz(position);
                                            }
                                        }, year, month, day);

                                datePickerDialog.show();
                            }
                        });

                    } else {
                        Toast.makeText(Educator_Quiz_Post_Activity.this, "You have 0 student in this classroom, please add student first.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(Educator_Quiz_Post_Activity.this, "You have 0 student in this classroom, please add student first.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void postToStudent(String classroomKey, PostedQuizModel categoryModel, String studentKey, int position, ArrayList<String> postedClassroom) {
        database.getReference().child("Student").child(studentKey).child("quiz").child(classroomKey).child(keyCtg)
            .setValue(categoryModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    //insert the quiz to the students in database
                    referencePostedSet.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()) {
                                ArrayList<String> postedSet = (ArrayList<String>) snapshot.getValue();
                                if(postedSet != null && postedSet.contains(keySet)) {
                                    referencePostedClassroom.setValue(postedClassroom);
                                } else if(postedSet != null && !postedSet.contains(keySet)){
                                    postedSet.add(keySet);
                                    referencePostedSet.setValue(postedSet);
                                    referencePostedClassroom.setValue(postedClassroom);
                                }
                            } else {
                                ArrayList<String> postedSet = new ArrayList<>();
                                postedSet.add(keySet);
                                referencePostedSet.setValue(postedSet);
                                referencePostedClassroom.setValue(postedClassroom);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            });

    }

    public void noticePostQuiz(int position) {
        Toast.makeText(Educator_Quiz_Post_Activity.this, "Quiz is posted successfully.", Toast.LENGTH_LONG).show();
        AlertDialog.Builder builder = new AlertDialog.Builder(Educator_Quiz_Post_Activity.this);
        builder.setTitle("Notice");
        builder.setMessage("Currently, modifications or deletions of questions in this set are not allowed as it has already been shared with students. " +
                "\n\nTo delete this particular set or its parent category, you need to cancel all the posting of quiz to classroom(s). " +
                "\nYou can cancel the posting by performing a long-click on the respective classroom.");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }
    private class SetInfo {
        private long postedTime;
        private long dueDate;

        public SetInfo(long postedTime, long dueDate) {
            this.postedTime = postedTime;
            this.dueDate = dueDate;
        }

        public SetInfo() {
        }

        public long getPostedTime() {
            return postedTime;
        }

        public void setPostedTime(long postedTime) {
            this.postedTime = postedTime;
        }

        public long getDueDate() {
            return dueDate;
        }

        public void setDueDate(long dueDate) {
            this.dueDate = dueDate;
        }
    }

}