package com.elijah.ukeme.ride_booking_app.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.elijah.ukeme.ride_booking_app.R;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.common.base.MoreObjects;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class DriversMapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    private LocationRequest locationRequest;
    private LatLng driverLocationLatLng;
    public static final int PERMISSION_FINE_LOCATION = 99;


    private TextView driverSetting, logoutBtn,textviewDistance;
    private FirebaseAuth mAuth;
    private Boolean driverCurrentStatus = false;
    private DatabaseReference driversWorkingRef;
    private DatabaseReference assignedCustomerRef,assignedCustomerPickUpRef;
    private String driverID,customerID="",tripStatus,tripStartusText;
    private Marker pickUpMarker;
    private ValueEventListener assignedCustomerPickUpRefListener;

    private CircleImageView customerImage;
    private TextView customerName,customerPhone,customerDetails,startTrip,customerDistance;
    private ImageView callACustomerButton;
    private RelativeLayout relativeLayout;
    private float distanceCovered = 0.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drivers_maps);
        driverSetting = findViewById(R.id.driver_maps_setting);
        logoutBtn = findViewById(R.id.driver_maps_logout);
        customerImage = findViewById(R.id.customer_image);
        customerName = findViewById(R.id.customer_name);
        customerPhone = findViewById(R.id.customer_phone_number);
        customerDetails = findViewById(R.id.customer_details_view);
        callACustomerButton = findViewById(R.id.image_call_a_customer);
        textviewDistance = findViewById(R.id.tv_distance_covered);
        relativeLayout = findViewById(R.id.layout2);
        startTrip = findViewById(R.id.driver_maps_start_trip);
        customerDistance = findViewById(R.id.driver_text_distance);
        tripStartusText = startTrip.getText().toString();
        mAuth = FirebaseAuth.getInstance();
        driverID = mAuth.getCurrentUser().getUid();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        checkLocationPermission();

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                driverCurrentStatus = true;
                disconnectTheDriver();
                mAuth.signOut();
                logoutDriver();
            }
        });
        callACustomerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callThePassenger();
            }
        });
        customerDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewTheRequestedCustomerDetails();
            }
        });
        driverSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DriversMapsActivity.this,SettingsActivity.class);
                intent.putExtra("type","Driver");
                startActivity(intent);
            }
        });

        startTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tripStartusText.equalsIgnoreCase("Start A Trip")){
                    tripStatus = "start";
                    startTrip.setText("End A Trip");
                }else {
                    startTrip.setText("Start A Trip");
                    tripStatus = "stop";
                }

            }
        });
        getAssignedCustomerRequest();

    }

    private void getAssignedCustomerRequest() {

        try {


        assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users")
                .child("Drivers").child(driverID).child("customerFoundId");

        assignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    customerID = snapshot.getValue().toString();

                   relativeLayout.setVisibility(View.VISIBLE);
                   getAssignedCustomerInformation();
                    getAssignedCustomerPickUpLocation();
                }else {
                    customerID = "";
                    if (pickUpMarker != null){
                        pickUpMarker.remove();
                    }
                    if (assignedCustomerPickUpRefListener !=null){
                        assignedCustomerPickUpRef.removeEventListener(assignedCustomerPickUpRefListener);
                    }
                    relativeLayout.setVisibility(View.GONE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        }catch (Exception e){
            //Toast.makeText(DriversMapsActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
        }

    }

    private void getAssignedCustomerPickUpLocation() {
        assignedCustomerPickUpRef = FirebaseDatabase.getInstance().getReference()
                .child("Customers Request").child(customerID).child("l");

        assignedCustomerPickUpRefListener = assignedCustomerPickUpRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    List<Object> customerLocationMap = (List<Object>) snapshot.getValue();

                    double customerLatitude = 0;
                    double customerLongitude = 0;
                   // bookARide.setText("Driver Found");

                    if (customerLocationMap.get(0) !=null){
                        customerLatitude = Double.parseDouble(customerLocationMap.get(0).toString());
                    }
                    if (customerLocationMap.get(1) !=null){
                        customerLongitude = Double.parseDouble(customerLocationMap.get(1).toString());
                    }

                    driverLocationLatLng = new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
                    LatLng customerLatLong = new LatLng(customerLatitude,customerLongitude);

                    Location customerLocation = new Location("");
                    customerLocation.setLatitude(customerLatLong.latitude);
                    customerLocation.setLongitude(customerLatLong.longitude);

                    Location driverLocation = new Location("");
                    driverLocation.setLatitude(driverLocationLatLng.latitude);
                    driverLocation.setLongitude(driverLocationLatLng.longitude);

                    float distance = driverLocation.distanceTo(customerLocation);

                    if (distance <90){
                        customerDistance.setText("You Have Arrived");
                    }else {
                        customerDistance.setText("Customer's Distance "+ distance);
                    }

                    pickUpMarker = mMap.addMarker(new MarkerOptions().position(customerLatLong).title("Customer PickUp Location")
                    .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("profile",100,100))));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        buildGoogleApiClient();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(locationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

        try {
        distanceCovered =(float) distanceBetweenLocations(lastLocation,location);
            int transportFare ;

            if (lastLocation !=null){
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

                tripDetails(distanceCovered,transportFare);
                //textviewDistance.setText("Distance Covered "+distanceCovered+"\nYour Transport fare is "+transportFare);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        lastLocation = location;

        if (getApplicationContext() != null) {
           // if (location != null && mMap != null) {
                try {

                    lastLocation = location;
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(16));

                    String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference driverAvailableRef = FirebaseDatabase.getInstance().getReference().child("Drivers Available");
                    GeoFire geoFireDriverAvailability = new GeoFire(driverAvailableRef);


                    driversWorkingRef = FirebaseDatabase.getInstance().getReference().child("Drivers Working");
                    GeoFire geoFireDriverWorking = new GeoFire(driversWorkingRef);

                    switch (customerID) {
                        case "":
                            geoFireDriverWorking.removeLocation(userID, new GeoFire.CompletionListener() {
                                @Override
                                public void onComplete(String key, DatabaseError error) {

                                }
                            });
                            geoFireDriverAvailability.setLocation(userID, new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
                                @Override
                                public void onComplete(String key, DatabaseError error) {

                                }
                            });

                            break;
                        default:
                            geoFireDriverAvailability.removeLocation(userID, new GeoFire.CompletionListener() {
                                @Override
                                public void onComplete(String key, DatabaseError error) {

                                }
                            });
                            geoFireDriverWorking.setLocation(userID, new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
                                @Override
                                public void onComplete(String key, DatabaseError error) {

                                }
                            });
                            break;

                    }
                }catch (Exception e){
                    //Toast.makeText(DriversMapsActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                }
                }

            }


    protected synchronized void buildGoogleApiClient(){
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();


        if (!driverCurrentStatus){
            disconnectTheDriver();
        }
    }

    private void disconnectTheDriver() {
        try {

        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference driverAvailableRef = FirebaseDatabase.getInstance().getReference().child("Drivers Available");
        GeoFire geoFireDriverAvailability = new GeoFire(driverAvailableRef);
        geoFireDriverAvailability.removeLocation(userID, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {

            }
        });
        }catch (Exception ignored){

        }
    }

    private void logoutDriver() {
        Intent intent = new Intent(DriversMapsActivity.this,WelcomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(DriversMapsActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DriversMapsActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_FINE_LOCATION);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_FINE_LOCATION);
            }
        }
        return true;
    }

    private void getAssignedCustomerInformation(){
        try {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("Users").child("Customer").child(customerID);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String name = snapshot.child("name").getValue().toString();
                    String phone = snapshot.child("phoneNumber").getValue().toString();
                    String image = snapshot.child("ProfileImage").getValue().toString();
                    customerName.setText(name);
                    customerPhone.setText(phone);
                    Picasso.get().load(image).into(customerImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        }catch (Exception ignored){

        }
    }
    private void callThePassenger(){

        try {


        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("Users").child("Customer").child(customerID);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    try {

                    if (snapshot.hasChild("phoneNumber")){
                        String phone = snapshot.child("phoneNumber").getValue().toString();
                        if (phone !=null){
                            Intent intent = new Intent(Intent.ACTION_CALL);
                            intent.setData(Uri.parse("tel:" +phone));
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }else {
                            Toast.makeText(DriversMapsActivity.this,"Phone Number Not Available",Toast.LENGTH_SHORT).show();
                        }

                    }else {
                        Toast.makeText(DriversMapsActivity.this,"Phone Number Not Available",Toast.LENGTH_SHORT).show();
                    }}catch (Exception ignored){}
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        }catch (Exception ignored){

        }
    }
    private void viewTheRequestedCustomerDetails(){
        try {

        Intent intent = new Intent(DriversMapsActivity.this,DetailsActivity.class);
        intent.putExtra("category","driver");
        intent.putExtra("onlineUserId",customerID);
        startActivity(intent);
        }catch (Exception ignored){

        }
    }

    public static double distance(double lat1, double lat2, double lon1,
                                  double lon2, double el1, double el2) {

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        double height = el1 - el2;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance);
    }

    public static double distanceBetweenLocations(Location l1, Location l2) {
        if(l1.hasAltitude() && l2.hasAltitude()) {
            return distance(l1.getLatitude(), l2.getLatitude(), l1.getLongitude(), l2.getLongitude(), l1.getAltitude(), l2.getAltitude());
        }
        return l1.distanceTo(l2);
    }

    public Bitmap resizeMapIcons(String iconName, int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier(iconName, "drawable", getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }

    private void tripDetails(double distance, int charge){
        if (tripStatus.equalsIgnoreCase("start")){
            double dist = 0.0;
            int charges = 0;
            dist=distance;
            charges=charge;
            relativeLayout.setVisibility(View.VISIBLE);
            customerName.setText("Distance Covered: "+dist);
            customerPhone.setText("Charge: "+charges);
            customerImage.setVisibility(View.GONE);
            callACustomerButton.setVisibility(View.GONE);
            customerDetails.setVisibility(View.GONE);
            customerDistance.setVisibility(View.GONE);
        }else {
            relativeLayout.setVisibility(View.GONE);
        }

    }
}