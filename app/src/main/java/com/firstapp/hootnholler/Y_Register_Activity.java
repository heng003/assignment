package com.firstapp.hootnholler;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.firstapp.hootnholler.entity.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class Y_Register_Activity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText UserFullname,UserEmail,UserPassword,UserConfirmPassword;
    private ImageView back_button;
    private ProgressDialog loadingBar;
    private Button CreateAccountButton;

    private User user;
    private RadioGroup UserRole;
    private RadioButton RoleSelection;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth= FirebaseAuth.getInstance();

        back_button=(ImageView)findViewById(R.id.back_button);
        UserFullname=(EditText) findViewById(R.id.register_fullname);
        UserEmail=(EditText) findViewById(R.id.register_email);
        UserPassword=(EditText) findViewById(R.id.register_password);
        UserConfirmPassword=(EditText) findViewById(R.id.register_cofirmpassword);
        UserRole=(RadioGroup)findViewById(R.id.roleselection);
        CreateAccountButton=(Button) findViewById(R.id.register_create_account);

        loadingBar=new ProgressDialog(this);

        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        CreateAccountButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //method of creating new account
                CreateNewAccount();
            }
        });

    }
    public void CreateNewAccount(){

        //get text from user input
        String fullname=UserFullname.getText().toString();
        String email=UserEmail.getText().toString();
        String password=UserPassword.getText().toString();
        String passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";
        String confirmPassword=UserConfirmPassword.getText().toString();
        RoleSelection= (RadioButton) findViewById(UserRole.getCheckedRadioButtonId());
        String role;

        //prompt user to key in the information if the field is still empty
        if(TextUtils.isEmpty(fullname)){
            Toast.makeText(Y_Register_Activity.this,"Please insert your fullname...",Toast.LENGTH_SHORT).show();

        }else if(TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please insert your email...", Toast.LENGTH_SHORT).show();
        //check the format of email
        }else if (!isValidEmail(email)) {
            UserEmail.setError("Please enter a valid email.");
            UserEmail.requestFocus();

        }else if(TextUtils.isEmpty(password)){
            Toast.makeText(this,"Please insert your password...",Toast.LENGTH_SHORT).show();

        }else if(TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this,  "Please insert your password...", Toast.LENGTH_SHORT).show();
        //check whether the password meet the minimum requirement
        }else if (!password.matches(passwordPattern)) {
                UserPassword.setError("Password must contain at least 8 characters, including uppercase, lowercase, number, and special characters.");
                UserPassword.requestFocus();
            //check whether the password is match with the confirm password
        }else if(!(password.equals(confirmPassword))) {
            Toast.makeText(this, "Your password do not match with your confirm password...", Toast.LENGTH_SHORT).show();

        }else if (RoleSelection == null) {
                Toast.makeText(this, "Please select the your role...", Toast.LENGTH_SHORT).show();
        }else{
            role = RoleSelection.getText().toString();
            //loading bar message
            loadingBar.setTitle("Creating New Account...");
            loadingBar.setMessage("Please wait, we are creating your new account...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);
            //use email and password to create a new account
            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    // Tell the user whether they were successful in creating an account or not
                    if (task.isSuccessful()) {
                        Toast.makeText(Y_Register_Activity.this, "You are authenticated successfully...", Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();

                        // Create a new User object with the provided information
                        user = new User(FirebaseAuth.getInstance().getUid(), fullname, email, role,"","","");

                        // Store the user object in the "Users" node of the Firebase Realtime Database
                        FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getUid()).setValue(user);
                        //check the role of user and navigates to different pages of set up account
                        if(role.equalsIgnoreCase("student")) {
                            // Start the Setup_Activity
                            Intent mainIntent = new Intent(Y_Register_Activity.this, Student_Setup_Activity.class);
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(mainIntent);
                            finish();
                        } else if (role.equalsIgnoreCase("parent")) {
                            Intent mainIntent = new Intent(Y_Register_Activity.this, Parent_Setup_Activity.class);
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(mainIntent);
                            finish();

                        }else if (role.equalsIgnoreCase("educator")) {
                            Intent mainIntent = new Intent(Y_Register_Activity.this, Educator_Setup_Activity.class);
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(mainIntent);
                            finish();}
                    } else {
                        // Display the error message to the user
                        String message = task.getException().getMessage();
                        Toast.makeText(Y_Register_Activity.this, "Error Occurred: " + message, Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }

            });
        }

    }

    public boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);

        return matcher.matches();
    }

}