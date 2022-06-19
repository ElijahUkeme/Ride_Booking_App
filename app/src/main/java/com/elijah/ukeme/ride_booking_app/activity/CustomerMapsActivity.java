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
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CustomerMapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener{


    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    private LocationRequest locationRequest;
    public static final int PERMISSION_FINE_LOCATION = 99;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private String customerID;
    private DatabaseReference customerRef;
    private LatLng customerPickUpLocation;
    private DatabaseReference driverAvailableRef;
    private DatabaseReference driverRef;
    private DatabaseReference driverLocationRef;
    private int radius = 1;
    private Boolean driverFound = false, requestType = false;
    private String driverFoundID,theRoot="",tripStatus,tripStartusText;
    Marker driverMarker,pickUpMarker;
    private ValueEventListener listenerRefForDriverLocation;
    private GeoQuery geoQuery;

    private Button  bookARide;
    private TextView customerLogout,customerSettings,customerStartATrip;
    private CircleImageView driverImage;
    private TextView driverName,driverPhone,driverDetails;
    private ImageView callADriverButton;
    private RelativeLayout relativeLayout;
    private double distanceCovered = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_maps);
        customerLogout = findViewById(R.id.customer_maps_logout);
        customerSettings = findViewById(R.id.customer_maps_setting);
        bookARide = findViewById(R.id.customer_book_a_ride_button);
        driverImage = findViewById(R.id.driver_image);
        driverName = findViewById(R.id.driver_name);
        driverPhone = findViewById(R.id.driver_phone_number);
        driverDetails = findViewById(R.id.driver_details_view);
        callADriverButton = findViewById(R.id.image_call_a_driver);
        relativeLayout = findViewById(R.id.layout1);
        customerStartATrip = findViewById(R.id.customer_maps_start_trip);
        tripStartusText = customerStartATrip.getText().toString();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        customerRef = FirebaseDatabase.getInstance().getReference().child("Customers Request");
        driverAvailableRef = FirebaseDatabase.getInstance().getReference().child("Drivers Available");
        driverLocationRef = FirebaseDatabase.getInstance().getReference().child("Drivers Working");
        customerID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        checkLocationPermission();

        customerLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customerRef.child(customerID).removeValue();
                driverAvailableRef.child(customerID).removeValue();
                mAuth.signOut();
                logoutCustomer();
            }
        });
        customerSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CustomerMapsActivity.this,SettingsActivity.class);
                intent.putExtra("type","Customer");
                startActivity(intent);
            }
        });

        callADriverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callTheDriver();
            }
        });
        driverDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewTheRequestedCustomerDetails();
            }
        });

        customerStartATrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tripStartusText.equalsIgnoreCase("Start A Trip")){
                    tripStatus = "start";
                    customerStartATrip.setText("End A Trip");
                }else {
                    customerStartATrip.setText("Start A Trip");
                    tripStatus = "stop";
                }
            }
        });

        bookARide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

               if (requestType){

                   try {

                   requestType = false;
                   geoQuery.removeAllListeners();
                   if (driverLocationRef !=null){
                       driverLocationRef.removeEventListener(listenerRefForDriverLocation);
                   }


                   if (driverFound !=null){
                       driverRef = FirebaseDatabase.getInstance().getReference().child("Users")
                               .child("Drivers").child(driverFoundID).child("customerFoundId");
                       driverRef.removeValue();
                       driverFoundID = null;
                   } }catch (Exception ignored){}
                   driverFound = false;
                   radius = 1;
                   GeoFire geoFire = new GeoFire(customerRef);
                   geoFire.removeLocation(customerID, new GeoFire.CompletionListener() {
                       @Override
                       public void onComplete(String key, DatabaseError error) {

                       }
                   });
                   if (pickUpMarker !=null){
                       pickUpMarker.remove();
                       Toast.makeText(CustomerMapsActivity.this,"Trip Cancelled",Toast.LENGTH_LONG).show();
                   }
                   if (driverMarker !=null){
                       driverMarker.remove();
                   }
                   bookARide.setText("Book a Ride");
                   relativeLayout.setVisibility(View.GONE);

               }else {
                   if (lastLocation != null) {
                       requestType = true;
                       GeoFire geoFire = new GeoFire(customerRef);
                       geoFire.setLocation(customerID, new GeoLocation(lastLocation.getLatitude(), lastLocation.getLongitude()), new GeoFire.CompletionListener() {
                           @Override
                           public void onComplete(String key, DatabaseError error) {

                           }
                       });
                       customerPickUpLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                       pickUpMarker = mMap.addMarker(new MarkerOptions().position(customerPickUpLocation).title("My Location")
                               .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("profile", 100, 100))));

                       bookARide.setText("Getting a Cab....");

                       getTheClosestDriver();

                   }


               }


               }
        });
    }

   private void getTheClosestDriver() {
        GeoFire geoFire = new GeoFire(driverAvailableRef);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(customerPickUpLocation.latitude,customerPickUpLocation.longitude),radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                 if (!driverFound && requestType){
                    driverFound = true;
                    driverFoundID = key;

                    driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundID);

                    HashMap driverMap = new HashMap();
                    driverMap.put("customerFoundId",customerID);
                    driverRef.updateChildren(driverMap);

                    gettingTheDriverLocation();
                     bookARide.setText("Searching For a Driver Location");
                }

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

           @Override
            public void onGeoQueryReady() {
                if (!driverFound){
                    radius = radius + 1;

                    getTheClosestDriver();

                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                Toast.makeText(CustomerMapsActivity.this,error.getMessage(),Toast.LENGTH_LONG).show();

            }
        });
    }

        private void gettingTheDriverLocation() {
        listenerRefForDriverLocation = driverLocationRef.child(driverFoundID).child("l")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists() && requestType){
                            List<Object> driverLocationMap = (List<Object>) snapshot.getValue();
                            double driverLatitude = 0;
                            double driverLongitude = 0;
                            bookARide.setText("Driver Found");
                            relativeLayout.setVisibility(View.VISIBLE);
                            getAssignedDriverInformation();

                            if (driverLocationMap.get(0) !=null){
                                driverLatitude = Double.parseDouble(driverLocationMap.get(0).toString());
                            }
                            if (driverLocationMap.get(1) !=null){
                                driverLongitude = Double.parseDouble(driverLocationMap.get(1).toString());
                            }
                            LatLng driverLatLong = new LatLng(driverLatitude,driverLongitude);

                            if (driverMarker !=null){
                                driverMarker.remove();
                            }

                            Location customerLocation = new Location("");
                            customerLocation.setLatitude(customerPickUpLocation.latitude);
                            customerLocation.setLongitude(customerPickUpLocation.longitude);

                            Location driverLocation = new Location("");
                            driverLocation.setLatitude(driverLatLong.latitude);
                            driverLocation.setLongitude(driverLatLong.longitude);

                            float distance = customerLocation.distanceTo(driverLocation);
                            if (distance <90){
                                bookARide.setText("Your Driver Has Arrived");
                            }else {
                                bookARide.setText("Driver's Distance "+ distance);
                            }


                            driverMarker = mMap.addMarker(new MarkerOptions().position(driverLatLong).title("Your Driver is Here")
                            .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("car",100,100))));

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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        buildGoogleApiClient();
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
        distanceCovered = distanceBetweenLocations(lastLocation,location);

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
                    //customerStartATrip.setText("Distance Covered "+distanceCovered+"\nYour Transport fare is "+transportFare);
                }

            }catch (Exception e){
                e.printStackTrace();
        }
        lastLocation = location;
        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(16));

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    protected synchronized void buildGoogleApiClient(){
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        googleApiClient.connect();
    }
    private void logoutCustomer() {
        Intent intent = new Intent(CustomerMapsActivity.this,WelcomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(CustomerMapsActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(CustomerMapsActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_FINE_LOCATION);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_FINE_LOCATION);
            }
        }
        return true;
    }

    private void getAssignedDriverInformation(){
        try {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("Users").child("Driver").child(driverFoundID);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    try {


                    String name = snapshot.child("name").getValue().toString();
                    String phone = snapshot.child("phoneNumber").getValue().toString();
                    String image = snapshot.child("profileImage").getValue().toString();
                    driverName.setText(name);
                    driverPhone.setText(phone);
                    Picasso.get().load(image).into(driverImage);
                    }catch (Exception ignored){}
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        }catch (Exception ignored){

        }
    }
    private void callTheDriver() {
        try {

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                    .child("Users").child("Driver").child(driverFoundID);
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        if (snapshot.hasChild("phoneNumber")) {
                            try {

                            String phone = snapshot.child("phoneNumber").getValue().toString();
                            Intent intent = new Intent(Intent.ACTION_CALL);
                            intent.setData(Uri.parse("tel:" + phone));
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            }catch (Exception ignored){}
                        } else {
                            Toast.makeText(CustomerMapsActivity.this, "Phone Number Not Available", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });

        } catch (Exception ignored) {
        }

    }

    private void viewTheRequestedCustomerDetails(){
        try {

            Intent intent = new Intent(CustomerMapsActivity.this,DetailsActivity.class);
            intent.putExtra("category","customer");
            intent.putExtra("onlineUserId",driverFoundID);
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

    public Bitmap resizeMapIcons(String iconName,int width, int height){
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
            driverName.setText("Distance Covered: "+dist);
            driverPhone.setText("Charge: "+charges);
            driverImage.setVisibility(View.GONE);
            callADriverButton.setVisibility(View.GONE);
            driverDetails.setVisibility(View.GONE);
            bookARide.setVisibility(View.GONE);
        }else {
            relativeLayout.setVisibility(View.GONE);
            bookARide.setVisibility(View.VISIBLE);
        }

    }
}