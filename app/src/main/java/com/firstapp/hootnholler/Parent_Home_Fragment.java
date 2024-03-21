package com.firstapp.hootnholler;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.firstapp.hootnholler.adapter.Monitored_Student_List;
import com.firstapp.hootnholler.databinding.FragmentParentHomeBinding;
import com.firstapp.hootnholler.entity.Student;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class Parent_Home_Fragment extends Fragment {

    private LinearLayout RiskStatusLayout;
    private View TaskStatusBtn, QuizScoreBtn, FeedbackBtn;
    private TextView StudentName, StudentClass, WeeklyAvgPerformance, RiskStatus;
    private FragmentParentHomeBinding binding;
    private Spinner Student_List;
    private ArrayList<Student> MonitoredStudentList;
    private DatabaseReference ParentRef = FirebaseDatabase.getInstance().getReference("Parent");
    private DatabaseReference StudentRef = FirebaseDatabase.getInstance().getReference("Student");
    private DatabaseReference UserRef = FirebaseDatabase.getInstance().getReference("Users");
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String UserUID = mAuth.getUid();
    public String studentUID;
    private Monitored_Student_List monitoredStudentAdapter;
    private DatabaseReference Database = FirebaseDatabase.getInstance().getReference();
    private double WeeklyAverageQuizScore, WeeklyAverageAssignmentScore, WeeklyAveragePerformanceScore;
    private Calendar calendar;
    private DecimalFormat decimalFormat = new DecimalFormat("#.##");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment using View Binding
        binding = FragmentParentHomeBinding.inflate(inflater, container, false);

        TaskStatusBtn = binding.TaskStatusBtn;
        QuizScoreBtn = binding.QuizBtn;
        FeedbackBtn = binding.FeedbackBtn;
        Student_List = binding.studentList;
        StudentName = binding.homeMonitoredStudentName;
        StudentClass = binding.homeMonitoredStudentClass;
        WeeklyAvgPerformance = binding.weeklyAvg;
        RiskStatusLayout = binding.riskStatusLayout;
        RiskStatus = binding.atRisk;

        MonitoredStudentList = new ArrayList<>();
        monitoredStudentAdapter = new Monitored_Student_List(getActivity(), MonitoredStudentList);
        getStudentUIDList();

        Student_List.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Student student = (Student) adapterView.getItemAtPosition(i);
                notifyStudentChanges(student);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // Set click listeners for each button
        TaskStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the TaskStatusFragment
                Intent intent=new Intent(getActivity(), Parent_TaskStatus_Activity.class);
                intent.putExtra("Student_UID", studentUID);
                startActivity(intent);
            }
        });

        QuizScoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the QuizScoreFragment
                Intent intent=new Intent(getActivity(), Parent_Quiz_Score_Activity.class);
                intent.putExtra("Student_UID", studentUID);
                startActivity(intent);
            }
        });

        FeedbackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the FeedbackFragment
                Intent intent=new Intent(getActivity(), Y_FeedbackList.class);
                intent.putExtra("studentUID", studentUID);
                intent.putExtra("classCode", "");
                intent.putExtra("isParent", true);
                startActivity(intent);

            }
        });
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void getStudentUIDList(){
        MonitoredStudentList.clear();
        this.ParentRef.child(UserUID).child("ConnectionKey").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    // get student uid
                    String studentUID = dataSnapshot.getValue(String.class);
                    // get student details
                    StudentRef.child(studentUID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot studentSnapshot) {
                            Student student = studentSnapshot.getValue(Student.class);
                            student.userUID = studentUID;
                            UserRef.child(studentUID).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot userSnapShot) {
                                    student.setUserName(userSnapShot.child("fullname").getValue(String.class));
//                                    student.setProfile_URL("");
                                    MonitoredStudentList.add(student);
                                    Student_List.setAdapter(monitoredStudentAdapter);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
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

    public void notifyStudentChanges(Student student){
        StudentName.setText(student.getUserName());
        StudentClass.setText(student.getStudent_class());
        studentUID = student.getUserUID();
        Parent_MainActivity.studentUID = studentUID;
        getWeeklyAveragePerformance();
    }

    public void getWeeklyAveragePerformance() {
        WeeklyAverageAssignmentScore = 0;
        WeeklyAverageQuizScore = 0;
        WeeklyAveragePerformanceScore = 0;
        this.Database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList <String> setKey = new ArrayList<>();
                double totalSetNum = 0, totalTask = 0, totalCompleted = 0, totalMarkSet = 0;
                // loop clsssroom (assignment score)
                for (DataSnapshot classSnapshot : snapshot.child("Student").child(studentUID).child("JoinedClass").getChildren()) {
                    // if classroom exists
                    if (classSnapshot.getValue(Boolean.class) == true) {
                        for (DataSnapshot assignmentSnapshot : snapshot.child("Classroom").child(classSnapshot.getKey()).child("Assignment").getChildren()) {
                            if(!assignmentSnapshot.child("dueDate").exists()){
                                continue;
                            }
                            long timeStamp = Long.parseLong(assignmentSnapshot.child("dueDate").getValue(String.class));
                            if(isThisWeek(new Date(timeStamp))){
                                totalTask++;
                                for (DataSnapshot submissionSnapShot : assignmentSnapshot.child("Submission").getChildren()) {
                                    if (submissionSnapShot.getKey().equals(studentUID)) {
                                        totalCompleted++;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }

                if(totalTask != 0){
                    WeeklyAverageAssignmentScore = totalCompleted / totalTask * 100;
                }

                // loop subject (quiz score)
                for (DataSnapshot quizSnapshot : snapshot.child("Student").child(studentUID).child("quiz").getChildren()) {
                    // get classCode
                    for (DataSnapshot subjectSnapshot : quizSnapshot.getChildren()) {
                        setKey.clear();
                        for(DataSnapshot setSubjectSnapshot : subjectSnapshot.child("setKeyInfo").getChildren()){
                            if(isThisWeek(new Date(setSubjectSnapshot.child("dueDate").getValue(Long.class)))){
                                setKey.add(setSubjectSnapshot.getKey());
                            }
                        }
                        for (DataSnapshot setSnapshot : snapshot.child("Categories").child(subjectSnapshot.getKey()).child("Sets").getChildren()) {
                            if(!setKey.contains(setSnapshot.getKey())){
                                continue;
                            }
                            for (DataSnapshot scoreSnapshot : setSnapshot.child("Answers").getChildren()) {
                                if (scoreSnapshot.getKey().equals(studentUID)) {
                                    totalMarkSet += scoreSnapshot.child("percentage").getValue(Double.class);
                                    break;
                                }
                            }
                            totalSetNum ++;
                        }
                    }
                }

                if(totalSetNum != 0){
                    WeeklyAverageQuizScore = totalMarkSet / totalSetNum;
                }

                WeeklyAveragePerformanceScore = WeeklyAverageAssignmentScore * 0.40 + WeeklyAverageQuizScore * 0.60;
                WeeklyAvgPerformance.setText(decimalFormat.format(WeeklyAveragePerformanceScore) + "%");
                calculateRiskLevelStudent(WeeklyAveragePerformanceScore);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public boolean isThisWeek(Date date){
        calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        while(calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY){
            calendar.add(Calendar.DAY_OF_WEEK, -1);
        }
        Date firstDayOfWeek = calendar.getTime();
        calendar.add(Calendar.DAY_OF_WEEK, 6);
        Date lastDayOfWeek = calendar.getTime();
        if(!date.before(firstDayOfWeek) && !date.after(lastDayOfWeek)){
            return true;
        }
        return false;
    }

    public void calculateRiskLevelStudent(double WeeklyAveragePerformanceScore){
        if(WeeklyAveragePerformanceScore >= 50){
            RiskStatusLayout.setBackgroundResource(R.drawable.green_rectangle);
            RiskStatus.setText("Not an at-risk student");
        }
        else{
            RiskStatusLayout.setBackgroundResource(R.drawable.red_reactangle);
            RiskStatus.setText("Is an at-risk student");
        }
    }
}
