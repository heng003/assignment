package com.firstapp.hootnholler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.firstapp.hootnholler.adapter.People_RecyclerViewAdapter;
import com.firstapp.hootnholler.databinding.TeacherActivityPeopleBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Educator_ViewPeople extends AppCompatActivity {
    TextView EducatorName;
    private String currentClassCode;
    private DatabaseReference ClassroomRef,User;
    private ArrayList<String> StudentList = new ArrayList<>();
    private RecyclerView People_RecyclerView;
    private People_RecyclerViewAdapter PeopleAdapter;
    ImageView back;
    TeacherActivityPeopleBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = TeacherActivityPeopleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        currentClassCode = getIntent().getStringExtra("classCode");

        EducatorName = findViewById(R.id.TeacherName);
        People_RecyclerView = findViewById(R.id.StudentListView);
        back=binding.back;
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        People_RecyclerView.setLayoutManager(layoutManager);
        PeopleAdapter = new People_RecyclerViewAdapter(Educator_ViewPeople.this,StudentList,currentClassCode);
        People_RecyclerView.setAdapter(PeopleAdapter);


        DatabaseReference classroomRef = FirebaseDatabase.getInstance().getReference("Classroom").child(currentClassCode);
        DatabaseReference User=FirebaseDatabase.getInstance().getReference("Users");

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Educator_ViewPeople.this, Educator_Class.class);
                intent.putExtra("classCode", currentClassCode);
                startActivity(intent);
            }
        });
        classroomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String educatorUID=snapshot.child("classOwner").getValue(String.class);
                    User.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            EducatorName.setText(snapshot.child(educatorUID).child("fullname").getValue(String.class));
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
        classroomRef.child("StudentsJoined").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear the list before adding new data
                StudentList.clear();

                for (DataSnapshot studentSnapshot : snapshot.getChildren()) {
                    String studentKey = studentSnapshot.getKey();
                    StudentList.add(studentKey);
                }

                // Notify the adapter that the data set has changed
                PeopleAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error
            }
        });
    }
}

