package com.elijah.ukeme.ride_booking_app.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.elijah.ukeme.ride_booking_app.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class DetailsActivity extends AppCompatActivity {
    private CircleImageView detailsImage,carImage;
    private String getCategory,userID="";
    private LinearLayout linearLayout;
    private TextView name,city,age,maritalStatus,carName,phoneNumber,carPlateNumber,country,carColor,address,gender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        detailsImage = findViewById(R.id.image_details);
        carImage = findViewById(R.id.details_car);
        name= findViewById(R.id.details_name);
        city = findViewById(R.id.details_city);
        age = findViewById(R.id.details_age);
        maritalStatus = findViewById(R.id.details_marital_status);
        carName  = findViewById(R.id.details_car_name);
        phoneNumber = findViewById(R.id.details_phone);
        carPlateNumber = findViewById(R.id.details_car_plate_number);
        country = findViewById(R.id.details_country);
        carColor = findViewById(R.id.details_car_colour);
        address = findViewById(R.id.details_address);
        gender = findViewById(R.id.details_gender);
        linearLayout = findViewById(R.id.layout3);
        getCategory = getIntent().getStringExtra("category");
        userID = getIntent().getStringExtra("onlineUserId");

        getTheAssignedUsersDetails();
    }

    private void getTheAssignedUsersDetails(){
        if (getCategory.equalsIgnoreCase("driver")){
            linearLayout.setVisibility(View.VISIBLE);
            try {


            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                    .child("Users").child("Customer").child(userID);

            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        String userName = snapshot.child("name").getValue().toString();
                        String userCity = snapshot.child("city").getValue().toString();
                        String age1 = snapshot.child("age").getValue().toString();
                        String maritalStatus1 = snapshot.child("maritalStatus").getValue().toString();
                        String phone = snapshot.child("phoneNumber").getValue().toString();
                        String country1 = snapshot.child("country").getValue().toString();
                        String address1 = snapshot.child("address").getValue().toString();
                        String gender1 = snapshot.child("gender").getValue().toString();
                        name.setText(userName);
                        city.setText(userCity);
                        age.setText(age1);
                        maritalStatus.setText(maritalStatus1);
                        phoneNumber.setText(phone);
                        country.setText(country1);
                        address.setText(address1);
                        gender.setText(gender1);

                        if (snapshot.hasChild("ProfileImage")){
                            String imageProfile = snapshot.child("ProfileImage").getValue().toString();
                            Picasso.get().load(imageProfile).into(detailsImage);
                        }
                        String car = snapshot.child("carName").getValue().toString();
                        String colour = snapshot.child("carColour").getValue().toString();
                        String plateNumber = snapshot.child("carPlateNumber").getValue().toString();
                        carName.setText(car);
                        carColor.setText(colour);
                        carPlateNumber.setText(plateNumber);

                        if (snapshot.hasChild("carImage")){
                            try {


                                String imageCar = snapshot.child("carImage").getValue().toString();
                                Picasso.get().load(imageCar).into(carImage);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            }catch (Exception ignored){

            }
        }else {

            linearLayout.setVisibility(View.GONE);
            try {

                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                        .child("Users").child("Customer").child(userID);

                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){

                            String userName = snapshot.child("name").getValue().toString();
                            String userCity = snapshot.child("city").getValue().toString();
                            String age1 = snapshot.child("age").getValue().toString();
                            String maritalStatus1 = snapshot.child("maritalStatus").getValue().toString();
                            String phone = snapshot.child("phoneNumber").getValue().toString();
                            String country1 = snapshot.child("country").getValue().toString();
                            String address1 = snapshot.child("address").getValue().toString();
                            String gender1 = snapshot.child("gender").getValue().toString();
                            name.setText(userName);
                            city.setText(userCity);
                            age.setText(age1);
                            maritalStatus.setText(maritalStatus1);
                            phoneNumber.setText(phone);
                            country.setText(country1);
                            address.setText(address1);
                            gender.setText(gender1);

                            if (snapshot.hasChild("profileImage")){
                                String imageProfile = snapshot.child("profileImage").getValue().toString();
                                Picasso.get().load(imageProfile).into(detailsImage);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }catch (Exception ignored){

            }

        }
    }
}