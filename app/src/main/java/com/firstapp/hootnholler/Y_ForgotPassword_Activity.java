package com.firstapp.hootnholler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Y_ForgotPassword_Activity extends AppCompatActivity {
    private Button buttonPwdReset;
    private ImageView back_button;

    private EditText editTextPwdResetEmail;
    private FirebaseAuth authProfile;
    private final static String TAG = "ForgotPassword";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Get references to UI elements
        back_button=(ImageView)findViewById(R.id.back_button);
        editTextPwdResetEmail = findViewById(R.id.forgotPassword_email);
        buttonPwdReset = findViewById(R.id.ResetPassword);

        //set click listener for the back button
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(Y_ForgotPassword_Activity.this, Y_Login_Activity.class);
                startActivity(intent);
            }
        });

        // Set click listener for the reset password button
        buttonPwdReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editTextPwdResetEmail.getText().toString();
                    //check the email field is not empty
                if (TextUtils.isEmpty(email)) {
                    editTextPwdResetEmail.setError("Email is required");
                    editTextPwdResetEmail.requestFocus();
                    //validate the email format
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    editTextPwdResetEmail.setError("Valid email is required");
                    editTextPwdResetEmail.requestFocus();
                } else {
                    resetPassword(email);
                }
            }
        });
    }

    // Method to reset the user's password
    private void resetPassword(String email) {
        authProfile = FirebaseAuth.getInstance();
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Users");
        userReference.addValueEventListener(new ValueEventListener() {
            //check is the email registered
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long totalUsers = 0;
                for (DataSnapshot userSnapshot:snapshot.getChildren()) {
                    if(userSnapshot.child("email").getValue(String.class).equals(email)){
                        break;
                    }
                    totalUsers ++;
                }
                if(totalUsers != snapshot.getChildrenCount()){
                    //send reset password link to user's email
                    authProfile.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // Password reset email sent successfully
                                Toast.makeText(Y_ForgotPassword_Activity.this, "Please check your email for the password reset link", Toast.LENGTH_SHORT).show();

                                // Redirect to the starting activity
                                Intent intent = new Intent(Y_ForgotPassword_Activity.this, Y_Login_Activity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                // Handle other exceptions
                                Log.e(TAG, task.getException().getMessage());
                                Toast.makeText(Y_ForgotPassword_Activity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else{
                    Toast.makeText(Y_ForgotPassword_Activity.this, "Email is not registered. Please register first.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}