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

public class UserLoginActivity extends AppCompatActivity implements ShowAbleMessage {
    private Button customerBtn;
    private TextView customerNotRegistered,status;
    private EditText customerEmail;
    private EditText customerPassword;
    boolean cancel = false;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingDialog;
    private DatabaseReference customerDatabaseRef;
    private String onlineCustomerID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);
        customerBtn = findViewById(R.id.customer_login_button);
        customerNotRegistered = findViewById(R.id.customer_not_registered_textview);
        customerEmail = findViewById(R.id.customer_login_email);
        customerPassword = findViewById(R.id.customer_login_password);
        status = findViewById(R.id.customer_title_login);
        mAuth = FirebaseAuth.getInstance();
        loadingDialog = new ProgressDialog(this);

        customerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String buttonText = customerBtn.getText().toString();
                String email = customerEmail.getText().toString();
                String password = customerPassword.getText().toString();

                if (buttonText.equalsIgnoreCase("Register")){
                    registerCustomer(email,password);
                }else {
                    loginCustomer(email,password);
                }

            }
        });

        customerNotRegistered.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customerBtn.setText("Register");
                status.setText("Customer Registration");
                customerNotRegistered.setVisibility(View.GONE);
            }
        });
    }
    private void registerCustomer(String email, String password){

        if (email.isEmpty()){
            customerEmail.setError("Please Enter your Email Address");
            cancel = true;
            customerEmail.requestFocus();
        }else if (password.isEmpty()){
            customerPassword.setError("Please Enter your Password");
            cancel = true;
            customerPassword.requestFocus();
        }else if (password.length()<6){
            customerPassword.setError("Password must be Atleast 6 Characters");
            cancel = true;
            customerPassword.requestFocus();
        }else {
            loadingDialog.setTitle("Registration Processing...");
            loadingDialog.setMessage("Please wait while we are checking your credentials");
            loadingDialog.setCanceledOnTouchOutside(false);
            loadingDialog.show();
            mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){

                                onlineCustomerID = mAuth.getCurrentUser().getUid();
                                customerDatabaseRef = FirebaseDatabase.getInstance().getReference()
                                        .child("Users").child("Customers").child(onlineCustomerID);
                                customerDatabaseRef.setValue(true);

                                Intent intent = new Intent(UserLoginActivity.this,CustomerMapsActivity.class);
                                startActivity(intent);
                                loadingDialog.dismiss();
                                Toast.makeText(UserLoginActivity.this,"Customer Registered Successfully",Toast.LENGTH_SHORT).show();
                            }else {
                                loadingDialog.dismiss();
                                Toast.makeText(UserLoginActivity.this,"Error Occurred,Please Try Again",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
    private void loginCustomer(String email,String password) {

        if (email.isEmpty()) {
            customerEmail.setError("Please Enter your Email Address");
            cancel = true;
            customerEmail.requestFocus();
        } else if (password.isEmpty()) {
            customerPassword.setError("Please Enter your Password");
            cancel = true;
            customerPassword.requestFocus();
        } else {
            loadingDialog.setTitle("Login Processing...");
            loadingDialog.setMessage("Please wait while we are checking your credentials");
            loadingDialog.setCanceledOnTouchOutside(false);
            loadingDialog.show();
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                loadingDialog.dismiss();
                                Toast.makeText(UserLoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(UserLoginActivity.this,CustomerMapsActivity.class);
                                startActivity(intent);
                            } else {
                                loadingDialog.dismiss();
                                showMessage("Error", "Incorrect email or Password");
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