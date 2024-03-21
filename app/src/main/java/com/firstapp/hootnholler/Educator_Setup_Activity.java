package com.firstapp.hootnholler;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.firstapp.hootnholler.entity.Educator;
import com.firstapp.hootnholler.entity.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Educator_Setup_Activity extends AppCompatActivity implements View.OnClickListener {

    private EditText birthday, phonenumber,school;
    private RadioGroup gender;
    private RadioButton genderSelection;
    private ImageView back_button;
    private Button SubmitButton,AddSubject;
    LinearLayout layoutList1;
    ArrayList<String> newSubject = new ArrayList<>();
    private FirebaseAuth mAuth;
    private DatabaseReference reference;
    private ProgressDialog loadingBar;
    String currentUserID;
    private User user;
    private Educator educator;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_educator_setup);

        // Initialize Firebase authentication, retrieve the current user ID, and get the database reference
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        reference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    user = snapshot.getValue(User.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        // Initialize UI elements and set up event listeners
        back_button=(ImageView)findViewById(R.id.back_button);
        birthday = (EditText) findViewById(R.id.educator_birthday);
        phonenumber = (EditText) findViewById(R.id.educator_phonenumber);
        gender = (RadioGroup) findViewById(R.id.educator_gender);
        school=(EditText)findViewById(R.id.educator_school);
        SubmitButton = (Button) findViewById(R.id.SubmitButton);

        //dynamic view for addSubject
        layoutList1 = findViewById(R.id.subject_layout);
        AddSubject = findViewById(R.id.addSubject);
        AddSubject.setOnClickListener(this);


        loadingBar = new ProgressDialog(this);

        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // Handle the click event on the SaveInfoButton
        SubmitButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                SaveAccountSetupInformation();
            }


            @RequiresApi(api = Build.VERSION_CODES.O)
            // Method to save the user account setup information
            private void SaveAccountSetupInformation() {

                // Retrieve input values from UI elements
                String Birthday = birthday.getText().toString();
                String Phonenumber = phonenumber.getText().toString();
                String School=school.getText().toString();
                genderSelection = (RadioButton) findViewById(gender.getCheckedRadioButtonId());
                String genderEducator;




                // Check if any required field is empty, display a toast if true
                if (TextUtils.isEmpty(Birthday) || TextUtils.isEmpty(Phonenumber) ||TextUtils.isEmpty(School) ||
                        gender==null) {
                    Toast.makeText(Educator_Setup_Activity.this, "Please insert your information...", Toast.LENGTH_SHORT).show();
                }  // Validate the birthday format
                else if (!isValidBirthdayFormat(Birthday)) {
                    Toast.makeText(Educator_Setup_Activity.this, "Invalid birthday format. Please use DD/MM/YYYY.", Toast.LENGTH_SHORT).show();
                    return; // Exit the method if the format is invalid
                }else {
                    loadingBar.setTitle("Setting up account...");
                    loadingBar.setMessage("Please wait, we are saving your account information...");
                    loadingBar.show();
                    loadingBar.setCanceledOnTouchOutside(true);

                    educator = new Educator(School);
                    genderEducator=genderSelection.getText().toString();
                    // Store the user object in the "Users" node of the Firebase Realtime Database
                    FirebaseDatabase.getInstance().getReference().child("Educator").child(FirebaseAuth.getInstance().getUid()).setValue(educator);
                    //Retrieve input from addSubject dynamic view and add into ArrayList. Pass the array list into database for storing
                    newSubject.clear();
                    for (int i = 1; i < layoutList1.getChildCount(); i++) {
                        EditText ETSubject = (EditText) layoutList1.getChildAt(i).findViewById(R.id.newInput);
                        if (ETSubject.getText().toString() != null) {
                            newSubject.add(ETSubject.getText().toString());
                        }
                    }
                    //store subject into database
                    if (!newSubject.isEmpty()) {
                        DatabaseReference SubjectReference = FirebaseDatabase.getInstance().getReference().child("Educator");
                        SubjectReference.child(currentUserID).child("Subject").setValue(newSubject);
                    }

                    // Update the user object with the entered information
                    // Update the user data in the database and handle the completion
                    user.setBirthday(Birthday);
                    user.setPhone_number(Phonenumber);
                    user.setGender(genderEducator);
                    reference.setValue(user)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(Educator_Setup_Activity.this, "Saved", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                        SendUserToMainActivity();
                                    } else {
                                        Toast.makeText(Educator_Setup_Activity.this, "Something error", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                }
            }
        });
    }

    @Override
    //methods to control dynamic view
    public void onClick(View v) {
        if (v.getId() == R.id.addSubject) {
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
                removeSubjectView(subjectView);
            }
        });

        layoutList1.addView(subjectView);

        // Only add the key to the list if the user has entered some text
        String key = subjectEdit.getText().toString().trim();
        if (!TextUtils.isEmpty(key)) {
            newSubject.add(key);
        }
    }


    public void removeSubjectView(View v) {
        layoutList1.removeView(v);
    }

    //after setting up the account, users will be sent to main activity
    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(Educator_Setup_Activity.this, Y_Starting_Activity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    //Method to validate the birthday format (dd/MM/yyyy)
    public static boolean isValidBirthdayFormat(String birthday) {
        String regex = "\\d{2}/\\d{2}/\\d{4}";
        return birthday.matches(regex);
    }
}



