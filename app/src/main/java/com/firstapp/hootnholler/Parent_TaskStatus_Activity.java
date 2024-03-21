package com.firstapp.hootnholler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.firstapp.hootnholler.adapter.Student_Assignment;
import com.firstapp.hootnholler.databinding.ActivityTaskStatusBinding;
import com.firstapp.hootnholler.entity.Assignment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.SQLOutput;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Parent_TaskStatus_Activity extends AppCompatActivity {

    private RecyclerView StudentAssignmentList;
    private TextView DateRange, NumOfTask, NumOfCompletedTask, NumOfIncompletedTask, NumOfInProgressTask;
    private ImageView MoveForwardWeekBtn, MoveBackwardWeekBtn;
    private int WeekFromToday, TotalTask = 0, TotalCompleted = 0, TotalIncompleted = 0, TotalInProgress = 0;
    private Calendar calendar;
    private Date firstDayOfWeek, lastDayOfWeek;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private String StudentUid; //auth.getCurrentUser().getUid();
    private DatabaseReference StudentRef;
    private DatabaseReference ClassroomRef = FirebaseDatabase.getInstance().getReference("Classroom");
    private ArrayList<Assignment> Assignment;
    private ArrayList<String> Submissions;
    private Student_Assignment Student_Assignment_Adapter;
    private ActivityTaskStatusBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTaskStatusBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Bundle data = getIntent().getExtras();

        StudentUid = data.getString("Student_UID");
        StudentRef = FirebaseDatabase.getInstance().getReference("Student").child(StudentUid);
        DateRange = findViewById(R.id.dateRange);
        MoveForwardWeekBtn = findViewById(R.id.moveForwardWeekBtn);
        MoveBackwardWeekBtn = findViewById(R.id.moveBackwardWeekBtn);
        StudentAssignmentList = findViewById(R.id.assignment_list);
        NumOfTask = findViewById(R.id.Number_Of_Task);
        NumOfCompletedTask = findViewById(R.id.Number_Completed_Task);
        NumOfIncompletedTask = findViewById(R.id.Number_Incomplete_Task);
        Assignment = new ArrayList<>();
        Submissions = new ArrayList<>();
        Student_Assignment_Adapter = new Student_Assignment(Assignment);
        StudentAssignmentList.setAdapter(Student_Assignment_Adapter);
        StudentAssignmentList.setLayoutManager(new LinearLayoutManager(this));
        WeekFromToday = 0;
        TotalCompleted = 0;
        TotalIncompleted = 0;
        formDate(WeekFromToday);
        MoveForwardWeekBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WeekFromToday--;
                formDate(WeekFromToday);
            }
        });

        MoveBackwardWeekBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WeekFromToday++;
                formDate(WeekFromToday);
            }
        });

        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Parent_MainActivity.class);
                intent.putExtra("FRAGMENT_TO_LOAD", "home_Fragment");
                startActivity(intent);
                finish();
            }
        });

    }

    public void formDate(int selectedWeekFromToday){
        calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_WEEK, -7 * selectedWeekFromToday);
        while(calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY){
            calendar.add(Calendar.DAY_OF_WEEK, -1);
        }
        firstDayOfWeek = calendar.getTime();
        calendar.add(Calendar.DAY_OF_WEEK, 6);
        lastDayOfWeek = calendar.getTime();
        DateRange.setText(sdf.format(firstDayOfWeek) + "-" + sdf.format(lastDayOfWeek));
        notifyWeekChange();
    }

    public void notifyWeekChange(){
        checkWeek();
        loadTaskData();
    }

    public void checkWeek(){
        if(WeekFromToday == 0){
            MoveForwardWeekBtn.setVisibility(View.GONE);
        }
        else{
            MoveForwardWeekBtn.setVisibility(View.VISIBLE);
        }
    }

    public void loadTaskData(){
        // initialise all data
        TotalTask = 0;
        TotalIncompleted = 0;
        TotalCompleted = 0;
        TotalInProgress = 0;
        Submissions = new ArrayList<>();
        // get the list of class code from student
        this.StudentRef.child("JoinedClass").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                final int[] numOfTask = {0};
                Assignment.clear();
                for (DataSnapshot dataSnapShot : snapshot.getChildren()) {
                    String classCode = dataSnapShot.getKey();
                    System.out.println("je;l");
                    System.out.println(classCode);
                    ClassroomRef.child(classCode).child("Assignment").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot classRoomSnapshot) {
                            for (DataSnapshot assignmentSnapShot: classRoomSnapshot.getChildren()) {
                                int TaskStatus = 0;
                                Assignment assignment = assignmentSnapShot.getValue(Assignment.class);
                                System.out.println("je;l");
                                System.out.println(assignment.getDescription());
                                if(isBetween(new Date(Long.parseLong(assignment.getUploadDate()))
                                        ,new Date(Long.parseLong(assignment.getDueDate())),
                                        firstDayOfWeek, lastDayOfWeek)){

                                    // iterate submission inside the assignment
                                    for (DataSnapshot submissionSnapShot : assignmentSnapShot.child("Submission").getChildren()){
                                        if(submissionSnapShot.getKey().equals(StudentUid)){
                                            TaskStatus = checkTaskStatus(assignment, true);
                                            break;
                                        }
                                        else{
                                            TaskStatus = checkTaskStatus(assignment, false);
                                        }
                                    }
                                    assignment.setTaskStatus(TaskStatus);
                                    assignment.setClassCode(classCode);
                                    Assignment.add(assignment);
                                    numOfTask[0]++;
                                    if(TaskStatus == 0){
                                        TotalCompleted++;
                                    }
//                                    else if(TaskStatus == 1){
//                                        TotalInProgress++;
//                                    }
                                    else{
                                        TotalIncompleted++;
                                    }
                                }
                            }
                            if(Student_Assignment_Adapter != null){
                                Student_Assignment_Adapter.notifyDataSetChanged();
                            }

                            TotalTask = numOfTask[0];
                            NumOfTask.setText(String.valueOf(Assignment.size()));
                            NumOfCompletedTask.setText(String.valueOf(TotalCompleted));
                            NumOfIncompletedTask.setText(String.valueOf(TotalIncompleted));
//                            NumOfInProgressTask.setText(String.valueOf(TotalInProgress));
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

    public static boolean isBetween(Date uploadDate, Date dueDate, Date startDate, Date endDate){
        if(uploadDate.before(new Date())){
            if(dueDate.after(startDate) && dueDate.before(endDate)){
                return true;
            }
            return false;
        }
        return false;
    }

    public int checkTaskStatus(Assignment assignment, boolean isSubmit){
        Date dueDate = new Date(Long.parseLong(assignment.getDueDate()));
        Date currentDate = new Date();

        // if due
        if(dueDate.before(currentDate)){
            if(isSubmit){
                return 0;
            }
            else{
                return 2;
            }
        }
        // if not due
        else if(dueDate.after(currentDate)){
            if(isSubmit){
                return 0;
            }
            else{
                return 1;
            }
        }

        return -1;
    }
}