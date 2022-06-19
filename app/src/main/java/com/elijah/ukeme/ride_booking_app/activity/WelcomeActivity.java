package com.elijah.ukeme.ride_booking_app.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.elijah.ukeme.ride_booking_app.R;

public class WelcomeActivity extends AppCompatActivity {

    private Button driverLogin;
    private Button customerLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        driverLogin = findViewById(R.id.driver_login_button_main);
        customerLogin = findViewById(R.id.user_login_button);

        driverLogin.setOnClickListener(view -> {
            Intent intent = new Intent(WelcomeActivity.this,DriverLoginActivity.class);
            startActivity(intent);
        });

        customerLogin.setOnClickListener(view -> {
            Intent intent = new Intent(WelcomeActivity.this,UserLoginActivity.class);
            startActivity(intent);
        });

    }
}