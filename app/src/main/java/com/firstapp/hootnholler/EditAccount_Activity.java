package com.firstapp.hootnholler;

import static com.firstapp.hootnholler.Student_Setup_Activity.isValidBirthdayFormat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.media.Image;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.firstapp.hootnholler.entity.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class EditAccount_Activity extends AppCompatActivity implements View.OnClickListener {

    ImageView back_button;
    EditText fullname,birthday,phonenumber,school,student_class;
    LinearLayout layoutList1,layoutList2;
    View school_layout,studentLevel_layout,studentClass_layout,parentConnectionkey_layout,eduSubject_layout;
    Button submit,addSubject,addConnectionKey;
    Spinner level;
    RadioGroup gender;
    RadioButton genderSelection,female,male;
    String student_level,role,Gender;
    ArrayList<String> schoolLevel;
    ArrayList<String> ConnectionKey = new ArrayList<>();
    ArrayList<String> Subject = new ArrayList<>();
    DatabaseReference UserRef,StudentRef,ParentRef,EduRef;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_account);
        // Initialize Firebase authentication, retrieve the current user ID, and get the database reference
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String currentUserID = mAuth.getCurrentUser().getUid();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        StudentRef = FirebaseDatabase.getInstance().getReference().child("Student").child(currentUserID);
        ParentRef = FirebaseDatabase.getInstance().getReference().child("Parent").child(currentUserID);
        EduRef = FirebaseDatabase.getInstance().getReference().child("Educator").child(currentUserID);
        //Edit Text
        fullname = findViewById(R.id.fullname);
        birthday = findViewById(R.id.birthday);
        phonenumber = findViewById(R.id.phonenumber);
        school = findViewById(R.id.school);
        student_class = findViewById(R.id.student_class);

        //radio button
        gender = (RadioGroup) findViewById(R.id.gender);
        genderSelection = (RadioButton) findViewById(gender.getCheckedRadioButtonId());
        male = findViewById(R.id.male);
        female = findViewById(R.id.female);

        //button
        addSubject = findViewById(R.id.addSubject);
        addConnectionKey = findViewById(R.id.addKey);
        back_button = findViewById(R.id.back_button);
        submit = findViewById(R.id.SubmitButton);

        //dynamic view for subject and connection key
        layoutList1 = findViewById(R.id.key_layout);
        addConnectionKey = findViewById(R.id.addKey);
        addConnectionKey.setOnClickListener(this);

        layoutList2 = findViewById(R.id.subject_layout);
        addSubject = findViewById(R.id.addSubject);
        addSubject.setOnClickListener(this);

        //Spinner
        level = findViewById(R.id.student_level);
        schoolLevel = new ArrayList<>();
        schoolLevel.add("Standard 1");
        schoolLevel.add("Standard 2");
        schoolLevel.add("Standard 3");
        schoolLevel.add("Standard 4");
        schoolLevel.add("Standard 5");
        schoolLevel.add("Standard 6");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, schoolLevel);
        adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        level.setAdapter(adapter);

        //layout
        school_layout = findViewById(R.id.school_layout);
        studentLevel_layout = findViewById(R.id.student_level_layout);
        studentClass_layout = findViewById(R.id.student_class_layout);
        parentConnectionkey_layout = findViewById(R.id.parent_connectionkey_layout);
        eduSubject_layout = findViewById(R.id.educator_subject_layout);

        //Visibility
        school_layout.setVisibility(View.GONE);
        studentLevel_layout.setVisibility(View.GONE);
        studentClass_layout.setVisibility(View.GONE);
        parentConnectionkey_layout.setVisibility(View.GONE);
        eduSubject_layout.setVisibility(View.GONE);


        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        loadUserData(currentUserID);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // toString
                String Fullname = fullname.getText().toString();
                String Birthday = birthday.getText().toString();
                String PhoneNumber = phonenumber.getText().toString();

                if (TextUtils.isEmpty(Fullname) || TextUtils.isEmpty(Birthday) || TextUtils.isEmpty(PhoneNumber)) {
                    Toast.makeText(EditAccount_Activity.this, "Please fill in all the required information....", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Validate the birthday format
                if (!isValidBirthdayFormat(Birthday)) {
                    Toast.makeText(EditAccount_Activity.this, "Invalid birthday format. Please use DD/MM/YYYY.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Update user information
                Gender = genderSelection.getText().toString();
                UserRef.child("fullname").setValue(Fullname);
                UserRef.child("birthday").setValue(Birthday);
                UserRef.child("phone_number").setValue(PhoneNumber);
                UserRef.child("gender").setValue(Gender);
                updateRoleData(role,currentUserID);


            }
        });
    }

    private void updateRoleData(String role, String currentUserID) {
        if (role.equalsIgnoreCase("student")) {
            level.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    student_level = adapterView.getItemAtPosition(i).toString();
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
            String School = school.getText().toString();
            String Class = student_class.getText().toString();
            if (TextUtils.isEmpty(student_level) || TextUtils.isEmpty(School) || TextUtils.isEmpty(Class)) {
                Toast.makeText(EditAccount_Activity.this, "Please fill in all the required information....", Toast.LENGTH_SHORT).show();
            } else {
                StudentRef.child("school").setValue(School);
                StudentRef.child("student_class").setValue(Class);
                StudentRef.child("level").setValue(student_level);
            }
        } else if (role.equalsIgnoreCase("parent")) {
            //Retrieve input from addHobby dynamic view and add into ArrayList. Pass the array list into database for storing
            ConnectionKey.clear();
            for (int i = 1; i < layoutList1.getChildCount(); i++) {
                EditText ETKey = (EditText) layoutList1.getChildAt(i).findViewById(R.id.newInput);
                if (ETKey != null && ETKey.getText() != null) {
                    ConnectionKey.add(ETKey.getText().toString());
                }

            }

            // Store the ConnectionKey in the "Parent" collection under the current user ID
            if (!ConnectionKey.isEmpty()) {
                DatabaseReference ConnectionKeyReference = FirebaseDatabase.getInstance().getReference().child("Parent");
                ConnectionKeyReference.child(currentUserID).child("ConnectionKey").setValue(ConnectionKey);

            } else {
                String School = school.getText().toString();
                if (TextUtils.isEmpty(School)) {
                    Toast.makeText(EditAccount_Activity.this, "Please fill in all the required information....", Toast.LENGTH_SHORT).show();

                } else {
                    EduRef.child("school").setValue(School);
                    Subject.clear();
                    for (int i = 1; i < layoutList1.getChildCount(); i++) {
                        EditText ETSubject = (EditText) layoutList1.getChildAt(i).findViewById(R.id.newInput);
                        if (ETSubject.getText().toString() != null) {
                            Subject.add(ETSubject.getText().toString());
                        }
                    }

                    if (!Subject.isEmpty()) {
                        DatabaseReference SubjectReference = FirebaseDatabase.getInstance().getReference().child("Educator");
                        SubjectReference.child(currentUserID).child("Subject").setValue(Subject);
                    }

                }
            }
        }
    }

    private void loadUserData(String currentUserID) {
        UserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                fullname.setText(snapshot.child("fullname").getValue(String.class));
                birthday.setText(snapshot.child("birthday").getValue(String.class));
                phonenumber.setText(snapshot.child("phone_number").getValue(String.class));
                String genderValue = snapshot.child("gender").getValue(String.class);
                if (genderValue != null) {
                    if (genderValue.equals("Male")) {
                        male.setChecked(true);
                    } else if (genderValue.equals("Female")) {
                        female.setChecked(true);
                    }
                }
                role = snapshot.child("role").getValue(String.class);
                checkRole(role, currentUserID);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkRole(String role,String currentUserID) {
                if (role.equalsIgnoreCase("student")) {
                    school_layout.setVisibility(View.VISIBLE);
                    studentLevel_layout.setVisibility(View.VISIBLE);
                    studentClass_layout.setVisibility(View.VISIBLE);
                    StudentRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                school.setText(snapshot.child("school").getValue(String.class));
                                student_class.setText(snapshot.child("student_class").getValue(String.class));
                                // Get the student level from the database
                                String studentLevelFromDatabase = snapshot.child("level").getValue(String.class);
                                int position = schoolLevel.indexOf(studentLevelFromDatabase);
                                level.setSelection(position);

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    //update latest info to
                } else if (role.equalsIgnoreCase("parent")) {
                    parentConnectionkey_layout.setVisibility(View.VISIBLE);

                    ParentRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                // Get connection keys data from the database
                                DataSnapshot connectionKeysDatabase = snapshot.child("ConnectionKey");
                                if (connectionKeysDatabase.exists()) {
                                    // Iterate through connection keys and create editable input fields
                                    for (DataSnapshot keySnapshot : connectionKeysDatabase.getChildren()) {
                                        String connectionKey = keySnapshot.getValue(String.class);
                                        addEditableKeyView(connectionKey);


                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


                } else {
                    school_layout.setVisibility(View.VISIBLE);
                    eduSubject_layout.setVisibility(View.VISIBLE);

                    EduRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                school.setText(snapshot.child("school").getValue(String.class));
                                DataSnapshot subjectsDatabase = snapshot.child("Subject");
                                if (subjectsDatabase.exists()) {
                                    for (DataSnapshot subjectSnapshot : subjectsDatabase.getChildren()) {
                                        String subject = subjectSnapshot.getValue(String.class);
                                        addEditableSubjectView(subject);
                                    }
                                }
                            }
                        }


                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    }
                }

            public void onClick(View v) {
                if (v.getId() == R.id.addKey) {
                    addKeyView();

                } else if (v.getId() == R.id.addSubject) {
                    addSubjectView();
                }
            }

            private void addSubjectView() {
                final View subjectView = getLayoutInflater().inflate(R.layout.dynamic_view, null, false);
                final EditText subjectEdit = (EditText) subjectView.findViewById(R.id.newInput);
                ImageView removeSubject = (ImageView) subjectView.findViewById(R.id.remove_button);

                removeSubject.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        removeView(subjectView);
                    }
                });

                layoutList1.addView(subjectView);

                // Only add the key to the list if the user has entered some text
                String key = subjectEdit.getText().toString().trim();
                if (!TextUtils.isEmpty(key)) {
                    Subject.add(key);
                }
            }

            private void addKeyView() {
                final View keyView = getLayoutInflater().inflate(R.layout.dynamic_view, null, false);
                final EditText keyEdit = (EditText) keyView.findViewById(R.id.newInput);
                ImageView removeKey = (ImageView) keyView.findViewById(R.id.remove_button);

                removeKey.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        removeView(keyView);
                    }
                });

                layoutList1.addView(keyView);

                // Only add the key to the list if the user has entered some text
                String key = keyEdit.getText().toString().trim();
                if (!TextUtils.isEmpty(key)) {
                    ConnectionKey.add(key);
                }
            }

            private void addEditableKeyView(String connectionKey) {
                View keyView = getLayoutInflater().inflate(R.layout.dynamic_view, null, false);
                EditText keyEdit = keyView.findViewById(R.id.newInput);
                ImageView removeKey = keyView.findViewById(R.id.remove_button);

                // Set the connection key as the initial text in the editable input field
                keyEdit.setText(connectionKey);

                removeKey.setOnClickListener(v -> removeView(keyView));

                layoutList1.addView(keyView);
            }

            public void removeView(View v) {
                layoutList1.removeView(v);
            }


            private void addEditableSubjectView(String subject) {
                View subjectView = getLayoutInflater().inflate(R.layout.dynamic_view, null, false);
                EditText subjectEdit = subjectView.findViewById(R.id.newInput);
                ImageView removeSubject = subjectView.findViewById(R.id.remove_button);

                subjectEdit.setText(subject);

                removeSubject.setOnClickListener(v -> removeView(subjectView));

                layoutList2.addView(subjectView);
            }



}