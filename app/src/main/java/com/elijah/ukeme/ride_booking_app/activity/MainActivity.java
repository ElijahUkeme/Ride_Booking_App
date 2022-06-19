package com.elijah.ukeme.ride_booking_app.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.elijah.ukeme.ride_booking_app.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    TextView tv_lat, tv_lon, tv_altitude, tv_accuracy, tv_speed, tv_address, tv_sensor, tv_updates,distanceTV,tvFare;
    Switch sw_locationupdates, sw_gps;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;
    boolean updatesOn = false;
    LocationRequest locationRequest;
    public static final int DEFAULT_UPDATE_INTERVAL = 30;
    public static final int FAST_UPDATE_INTERVAL = 5;
    public static final int PERMISSION_FINE_LOCATION = 99;
    private Location currentLocation=null;
    private float distanceCovered = 0.0f;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_accuracy = findViewById(R.id.tv_accuracy);
        tv_address = findViewById(R.id.tv_address);
        tv_altitude = findViewById(R.id.tv_altitude);
        tv_sensor = findViewById(R.id.tv_sensor);
        tv_speed = findViewById(R.id.tv_speed);
        tv_updates = findViewById(R.id.tv_updates);
        tv_lat = findViewById(R.id.tv_lat);
        tv_lon = findViewById(R.id.tv_lon);
        sw_gps = findViewById(R.id.sw_gps);
        sw_locationupdates = findViewById(R.id.sw_locationsupdates);
        distanceTV = findViewById(R.id.tv_distance_covered);
        tvFare = findViewById(R.id.tv_transport_fare);

        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000 * DEFAULT_UPDATE_INTERVAL);
        locationRequest.setFastestInterval(1000 * FAST_UPDATE_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        locationCallback = new LocationCallback() {

            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location location = locationResult.getLastLocation();
                updatesUIValues(location);
            }
        };

        updateGPS();

        sw_gps.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                if (sw_gps.isChecked()) {
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    tv_sensor.setText("Using GPS Sensor");
                    Log.d("Main", "Testing logcat");

                } else {
                    locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                    tv_sensor.setText("Using Towers + Wifi");
                }
            }
        });
        sw_locationupdates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sw_locationupdates.isChecked()) {
                    startLocationUpdates();
                } else {
                    stopLocationUpdates();
                }
            }
        });
    }

    private void stopLocationUpdates() {
        tv_updates.setText("Location is not Tracked");
        tv_lon.setText("Not Tracking Location");
        tv_lat.setText("Not Tracking Location");
        tv_updates.setText("Not Tracking Location");
        tv_speed.setText("Not Tracking Location");
        tv_altitude.setText("Not Tracking Location");
        tv_accuracy.setText("Not Tracking Location");
        tv_address.setText("Not Tracking Location");
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void startLocationUpdates() {

        tv_updates.setText("Location is being Tracked");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
        updateGPS();

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void updateGPS(){
        try {

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {

                    try {
                        int transportFare ;

                        if (currentLocation !=null){
                            distanceCovered +=currentLocation.distanceTo(location)*1000;
                            distanceTV.setText("Distance Covered "+ distanceCovered);
                            if (distanceCovered <=100){
                                transportFare = 50;
                            }else if (distanceCovered >100 && distanceCovered <=200){
                                transportFare = 100;
                            }else if (distanceCovered >200 && distanceCovered <=300){
                                transportFare = 150;
                            }else if (distanceCovered >300 && distanceCovered <500){
                                transportFare = 200;
                            }else if (distanceCovered >500 && distanceCovered <=1000){
                                transportFare = 250;
                            }else if (distanceCovered > 1000 && distanceCovered <= 1500){
                                transportFare = 300;
                            }else {
                                transportFare = 500;
                            }
                            tvFare.setText("Your Transport fare is "+transportFare);
                        }

                    }catch (Exception e){
                        e.printStackTrace();
                    }


                    updatesUIValues(location);


                }

            });
        }else {
            if (Build.VERSION.SDK_INT >=Build.VERSION_CODES.M){
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},PERMISSION_FINE_LOCATION);
            }
        }
        }catch (Exception e){
            Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    private void updatesUIValues(Location location) {
        try {

        tv_lat.setText(String.valueOf(location.getLatitude()));
        tv_lon.setText(String.valueOf(location.getLongitude()));
        tv_accuracy.setText(String.valueOf(location.getAccuracy()));
        System.out.println("The latitude is "+location.getLatitude());
        System.out.println("The longitude is "+location.getLongitude());
        Log.d("Main","The latitude is "+location.getLatitude());
        Log.d("Main","The longitude is "+location.getLongitude());

        if (location.hasAltitude()){
            tv_altitude.setText(String.valueOf(location.getAltitude()));
        }else {
            tv_altitude.setText("Not Available");
        }
        if (location.hasSpeed()){
            tv_speed.setText(String.valueOf(location.getSpeed()));
        }else {
            tv_speed.setText("Not Available");
        }
        }catch (Exception e){

        }
        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
            tv_address.setText(addresses.get(0).getAddressLine(0));


        }catch (Exception e){
            tv_address.setText("Unable to get the Street Address");
            Toast.makeText(this,e.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case PERMISSION_FINE_LOCATION:
                if (grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    updateGPS();
                }else {
                    Toast.makeText(MainActivity.this,"This app requires permission to be granted",Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

}


