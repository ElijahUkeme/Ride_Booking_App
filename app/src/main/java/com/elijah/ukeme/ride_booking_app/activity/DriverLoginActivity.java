package com.elijah.ukeme.ride_booking_app.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.elijah.ukeme.ride_booking_app.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import interfaces.ShowAbleMessage;

public class DriverLoginActivity extends AppCompatActivity implements ShowAbleMessage {

    private Button driverBtn;
    private TextView driverNotRegistered,status;
    private EditText driverEmail;
    private EditText driverPassword;

    boolean cancel = false;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingDialog;
    private DatabaseReference driverDatabaseRef;
    private String onlineDriverID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login);
        driverBtn = findViewById(R.id.driver_login_button);
        driverNotRegistered = findViewById(R.id.driver_not_registered_textview);
        driverEmail = findViewById(R.id.driver_login_email);
        driverPassword = findViewById(R.id.driver_login_password);
        status = findViewById(R.id.driver_title_login);
        mAuth = FirebaseAuth.getInstance();
        loadingDialog = new ProgressDialog(this);


        driverNotRegistered.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                driverBtn.setText("Register");
                status.setText("Driver Registration");
                driverNotRegistered.setVisibility(View.GONE);
            }
        });

        driverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailAddress = driverEmail.getText().toString();
               String  driverPass = driverPassword.getText().toString();
               String buttonText = driverBtn.getText().toString();

                if (buttonText.equalsIgnoreCase("Register")){
                    registerDriver(emailAddress,driverPass);
                }else {
                    loginDriver(emailAddress,driverPass);
                }

            }
        });
    }
    private void registerDriver(String email, String password){

        if (email.isEmpty()){
            driverEmail.setError("Please Enter your Email Address");
            cancel = true;
            driverEmail.requestFocus();
        }else if (password.isEmpty()){
            driverPassword.setError("Please Enter your Password");
            cancel = true;
            driverPassword.requestFocus();
        }else if (password.length()<6){
            driverPassword.setError("Password must be Atleast 6 Characters");
            cancel = true;
            driverPassword.requestFocus();
        }else {
            try {


            loadingDialog.setTitle("Registration Processing...");
            loadingDialog.setMessage("Please wait while we are checking your credentials");
            loadingDialog.setCanceledOnTouchOutside(false);
            loadingDialog.show();
            mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){

                                onlineDriverID = mAuth.getCurrentUser().getUid();
                                driverDatabaseRef = FirebaseDatabase.getInstance().getReference()
                                        .child("Users").child("Drivers").child(onlineDriverID);

                                driverDatabaseRef.setValue(true);

                                Intent intent = new Intent(DriverLoginActivity.this,DriversMapsActivity.class);
                                startActivity(intent);
                                loadingDialog.dismiss();
                                Toast.makeText(DriverLoginActivity.this,"Driver Registered Successfully",Toast.LENGTH_SHORT).show();
                            }else {
                                loadingDialog.dismiss();
                                Toast.makeText(DriverLoginActivity.this,"Error Occurred,Please Try Again",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
            }catch (Exception e){
                Toast.makeText(DriverLoginActivity.this,e.toString(),Toast.LENGTH_LONG).show();
            }
        }
    }
    private void loginDriver(String email,String password){

        if (email.isEmpty()){
            driverEmail.setError("Please Enter your Email Address");
            cancel = true;
            driverEmail.requestFocus();
        }else if (password.isEmpty()) {
            driverPassword.setError("Please Enter your Password");
            cancel = true;
            driverPassword.requestFocus();
        }
        else {
            loadingDialog.setTitle("Login Processing...");
            loadingDialog.setMessage("Please wait while we are checking your credentials");
            loadingDialog.setCanceledOnTouchOutside(false);
            loadingDialog.show();
            mAuth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                loadingDialog.dismiss();
                                Toast.makeText(DriverLoginActivity.this,"Login Successful",Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(DriverLoginActivity.this,DriversMapsActivity.class);
                                startActivity(intent);
                            }
                            else {
                                loadingDialog.dismiss();
                                showMessage("Error","Incorrect email or Password");
                            }
                        }
                    });
        }
    }

    @Override
    public void showMessage(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }
}