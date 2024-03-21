package com.firstapp.hootnholler;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Y_Login_Activity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private Button LoginButton;
    private EditText UserEmail, UserPassword;
    private ImageView back_button;
    private ProgressDialog loadingBar;
    private DatabaseReference userRef;
    Y_Register_Activity object=new Y_Register_Activity();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        back_button=(ImageView)findViewById(R.id.back_button);
        UserEmail = (EditText) findViewById(R.id.input_email);
        UserPassword = (EditText) findViewById(R.id.input_password);
        LoginButton = (Button) findViewById(R.id.login_button);
        TextView forgotPassword = findViewById(R.id.TV_ForgotPassword);
        forgotPassword.setPaintFlags(forgotPassword.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        TextView signUp = findViewById(R.id.TV_SignUp);
        signUp.setPaintFlags(signUp.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        loadingBar = new ProgressDialog(this);

        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Y_Login_Activity.this, "You can reset your password now!.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), Y_ForgotPassword_Activity.class);
                startActivity(intent);
                finish();
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Y_Register_Activity.class);
                startActivity(intent);
                finish();
            }
        });

        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Login();
            }
        });
    }

    private void Login() {
        String email = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();

             //cannot leave empty space at email and password field
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter your email...", Toast.LENGTH_SHORT).show();
            //check the format of the input email
        }else if(!object.isValidEmail(email)){
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter your password...", Toast.LENGTH_SHORT).show();
        } else {
            // Loading bar message
            loadingBar.setTitle("Logging in...");
            loadingBar.setMessage("Please wait, we are logging your account...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        String userId = mAuth.getCurrentUser().getUid();
                        //different role will be navigated to different home page
                        checkRole(userId);
                    } else {
                        String message = task.getException().getMessage();
                        Toast.makeText(Y_Login_Activity.this, "Error occurred: " + message, Toast.LENGTH_SHORT).show();
                        if (!isFinishing()) {
                            loadingBar.dismiss();
                        }
                    }
                }
            });
        }
    }



    private void checkRole(String userId) {

            userRef.child(userId).child("role").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String role = dataSnapshot.getValue(String.class);

                        // Redirect based on user role
                        if ("Student".equals(role)) {
                            Toast.makeText(Y_Login_Activity.this,"You are logged in successfully", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Y_Login_Activity.this, Student_MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else if ("Educator".equals(role)) {
                            Toast.makeText(Y_Login_Activity.this,"You are logged in successfully", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Y_Login_Activity.this, Educator_Main_Activity.class);
                            startActivity(intent);
                            finish();
                        } else if ("Parent".equals(role)) {
                            Toast.makeText(Y_Login_Activity.this,"You are logged in successfully", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Y_Login_Activity.this, Parent_MainActivity.class);
                            startActivity(intent);
                            finish();

                        }
                    } else {
//                     User does not exist in the "Users" node
                        Toast.makeText(Y_Login_Activity.this, "User not found. Login failed.", Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle errors during database operation
                }
            });
        }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Dismiss the loadingBar if it's showing
        if (loadingBar != null && loadingBar.isShowing()) {
            loadingBar.dismiss();
        }
    }


}