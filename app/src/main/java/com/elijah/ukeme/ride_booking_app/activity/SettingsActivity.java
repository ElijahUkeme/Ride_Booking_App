package com.elijah.ukeme.ride_booking_app.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.elijah.ukeme.ride_booking_app.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {
    private TextView profileImagePicker, carImagePicker;
    private TextInputEditText name, city, age, maritalStatus, carName, phoneNumber, carPlateNumber, country, carColor, address;
    private CircleImageView profileImage, carImage;
    private RadioGroup radioGroup;
    private RadioButton male, female;
    private ImageView saveButton, closeButton;
    String selectedGender, getType, checker = "", uploadCar = "";
    private LinearLayout linearLayout;
    private Uri profileImageUri, carImageUri;
    private String myProfileImaeUri = "", myCarImageUri = "";
    private StorageTask uploadProfileTask, uploadCarImageTask;
    private StorageReference storageProfilePicsRef, storageCarImageRef;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    boolean cancel = false;
    private int imageToUpload = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        profileImage = findViewById(R.id.profile_image);
        carImage = findViewById(R.id.car_image);
        radioGroup = findViewById(R.id.gender);
        male = findViewById(R.id.radio_male);
        female = findViewById(R.id.radio_female);
        name = findViewById(R.id.editText_name);
        city = findViewById(R.id.editText_city_settings);
        age = findViewById(R.id.editText_age_settings);
        maritalStatus = findViewById(R.id.editText_marital_status_settings);
        carName = findViewById(R.id.editText_car_name_settings);
        phoneNumber = findViewById(R.id.phone_number_settings);
        carPlateNumber = findViewById(R.id.editText_car_plate_number_settings);
        country = findViewById(R.id.editText_country_settings);
        carColor = findViewById(R.id.editText_car_colour_settings);
        address = findViewById(R.id.editText_address_settings);
        profileImagePicker = findViewById(R.id.textview_profile_image_picker);
        carImagePicker = findViewById(R.id.textview_car_image_picker);
        saveButton = findViewById(R.id.save_button);
        closeButton = findViewById(R.id.close_button);
        linearLayout = findViewById(R.id.layout_driver_details);
        mAuth = FirebaseAuth.getInstance();
        storageProfilePicsRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        storageCarImageRef = FirebaseStorage.getInstance().getReference().child("Car Images");
        getType = getIntent().getStringExtra("type");
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(getType);
        if (getType.equalsIgnoreCase("Driver")) {
            linearLayout.setVisibility(View.VISIBLE);
        }
        getUserInfoDisplay();

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getType.equalsIgnoreCase("Driver")) {
                    startActivity(new Intent(SettingsActivity.this, DriversMapsActivity.class));
                } else {
                    startActivity(new Intent(SettingsActivity.this, CustomerMapsActivity.class));
                }
            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checker.equalsIgnoreCase("clicked")) {
                    validateInputs();
                } else {
                    validateOnlyUserInfo();
                }
            }
        });
        profileImagePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checker = "clicked";
                imageToUpload = 1;
                CropImage.activity()
                        .setAspectRatio(1, 1)
                        .start(SettingsActivity.this);
            }
        });
        carImagePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadCar = "car";
                imageToUpload = 2;
                CropImage.activity()
                        .setAspectRatio(1, 1)
                        .start(SettingsActivity.this);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK
                && data != null && imageToUpload==1) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (checker.equalsIgnoreCase("clicked")) {
                profileImageUri = result.getUri();
                profileImage.setImageURI(profileImageUri);
            }

            }else if (requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode==RESULT_OK
                && data !=null && imageToUpload==2){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (uploadCar.equalsIgnoreCase("car")){
                    carImageUri = result.getUri();
                    carImage.setImageURI(carImageUri);
            }


        } else {
            if (getType.equalsIgnoreCase("Driver")) {
                startActivity(new Intent(SettingsActivity.this, DriversMapsActivity.class));
                Toast.makeText(SettingsActivity.this, "Error Occurred, Try Again", Toast.LENGTH_SHORT).show();
            } else {
                startActivity(new Intent(SettingsActivity.this, CustomerMapsActivity.class));
                Toast.makeText(SettingsActivity.this, "Error Occurred, Try Again", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void validateInputs() {
        if (getType.equalsIgnoreCase("Driver")) {
            if (name.getText().toString().isEmpty()) {
                name.setError("Please Enter Your Name");
                cancel = true;
                name.requestFocus();
            } else if (city.getText().toString().isEmpty()) {
                city.setError("Please Enter Your City Name");
                cancel = true;
                city.requestFocus();
            } else if (age.getText().toString().isEmpty()) {
                age.setError("Please Enter Your Age");
                cancel = true;
                age.requestFocus();
            } else if (maritalStatus.getText().toString().isEmpty()) {
                maritalStatus.setError("Please Enter Your Marital Status");
                cancel = true;
                maritalStatus.requestFocus();
            } else if (phoneNumber.getText().toString().isEmpty()) {
                phoneNumber.setError("Please Enter Your Phone Number");
                cancel = true;
                phoneNumber.requestFocus();
            } else if (country.getText().toString().isEmpty()) {
                country.setError("Please Enter Your Country Name");
                cancel = true;
                country.requestFocus();
            } else if (address.getText().toString().isEmpty()) {
                address.setError("Please Enter Your Address");
                cancel = true;
                address.requestFocus();
            } else if (radioGroup.getCheckedRadioButtonId() == -1) {
                Toast.makeText(SettingsActivity.this, "Please Select Your Gender", Toast.LENGTH_SHORT).show();
            } else if (carName.getText().toString().isEmpty()) {
                carName.setError("Please Enter The Name of Your Car");
                cancel = true;
                carName.requestFocus();
            } else if (carPlateNumber.getText().toString().isEmpty()) {
                carPlateNumber.setError("Please Enter Your Car Plate Number");
                cancel = true;
                carPlateNumber.requestFocus();
            } else if (carColor.getText().toString().isEmpty()) {
                carColor.setError("Please Specify The Colour of Your Car");
                cancel = true;
                carColor.requestFocus();
            } else if (carImageUri == null) {
                carImagePicker.setText("Please Upload Your Car Image");
                carImagePicker.setTextColor(Color.RED);

            } else if (checker.equalsIgnoreCase("clicked")) {
                uploadProfilePicture();
            //} else if (uploadCar.equalsIgnoreCase("car")) {

            } else {
                //go ahead and save the details into the driver database
                validateOnlyUserInfo();
            }
        } else {
            if (name.getText().toString().isEmpty()) {
                name.setError("Please Enter Your Name");
                cancel = true;
                name.requestFocus();
            } else if (city.getText().toString().isEmpty()) {
                city.setError("Please Enter Your City Name");
                cancel = true;
                city.requestFocus();
            } else if (age.getText().toString().isEmpty()) {
                age.setError("Please Enter Your Age");
                cancel = true;
                age.requestFocus();
            } else if (maritalStatus.getText().toString().isEmpty()) {
                maritalStatus.setError("Please Enter Your Marital Status");
                cancel = true;
                maritalStatus.requestFocus();
            } else if (phoneNumber.getText().toString().isEmpty()) {
                phoneNumber.setError("Please Enter Your Phone Number");
                cancel = true;
                phoneNumber.requestFocus();
            } else if (country.getText().toString().isEmpty()) {
                country.setError("Please Enter Your Country Name");
                cancel = true;
                country.requestFocus();
            } else if (address.getText().toString().isEmpty()) {
                address.setError("Please Enter Your Address");
                cancel = true;
                address.requestFocus();
            } else if (radioGroup.getCheckedRadioButtonId() == -1) {
                Toast.makeText(SettingsActivity.this, "Please Select Your Gender", Toast.LENGTH_SHORT).show();
            } else if (checker.equalsIgnoreCase("clicked")) {
                uploadProfilePicture();
            } else {
                //go ahead and save the details into the database
                validateOnlyUserInfo();
            }
        }
    }

    private void uploadCarImage() {
        if (carImageUri != null) {
            final StorageReference storageReference = storageCarImageRef.child(mAuth.getCurrentUser().getUid() + ".jpg");
            uploadCarImageTask = storageReference.putFile(carImageUri);

            uploadCarImageTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return storageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadCarUri = task.getResult();

                        myCarImageUri = downloadCarUri.toString();

                        HashMap<String,Object> hashMap = new HashMap<>();
                        hashMap.put("carImage", myCarImageUri);
                        databaseReference.child(mAuth.getCurrentUser().getUid()).updateChildren(hashMap);
                    }
                }
            });
        } else {
            Toast.makeText(SettingsActivity.this, "Car Image Not Selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadProfilePicture() {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Updating Profile");
        progressDialog.setMessage("Please wait while we are updating your account information");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        if (profileImageUri != null) {

            final StorageReference fileRef = storageProfilePicsRef.child(mAuth.getCurrentUser().getUid() + ".jpg");
            uploadProfileTask = fileRef.putFile(profileImageUri);

            uploadProfileTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return fileRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        myProfileImaeUri = downloadUri.toString();

                        int selectedID = radioGroup.getCheckedRadioButtonId();
                        male = (RadioButton)findViewById(selectedID);
                        female = (RadioButton) findViewById(selectedID);
                        if (male.isChecked()){
                            selectedGender = male.getText().toString();
                        }else if (female.isChecked()){
                            selectedGender = female.getText().toString();
                        }else {
                            selectedGender = "Not specify";
                        }

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("ProfileImage", myProfileImaeUri);
                        hashMap.put("name", name.getText().toString());
                        hashMap.put("city", city.getText().toString());
                        hashMap.put("age", age.getText().toString());
                        hashMap.put("maritalStatus", maritalStatus.getText().toString());
                        hashMap.put("phoneNumber", phoneNumber.getText().toString());
                        hashMap.put("country", country.getText().toString());
                        hashMap.put("address", address.getText().toString());
                        hashMap.put("gender", selectedGender);

                        if (getType.equalsIgnoreCase("Driver")) {
                            uploadCarImage();
                            hashMap.put("carName", carName.getText().toString());
                            hashMap.put("carColour", carColor.getText().toString());
                            hashMap.put("carPlateNumber", carPlateNumber.getText().toString());

                        }
                        databaseReference.child(mAuth.getCurrentUser().getUid()).updateChildren(hashMap);

                        progressDialog.dismiss();
                        if (getType.equalsIgnoreCase("Driver")) {

                            startActivity(new Intent(SettingsActivity.this, DriversMapsActivity.class));
                            Toast.makeText(SettingsActivity.this, "Profile Information Updated Successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            startActivity(new Intent(SettingsActivity.this, CustomerMapsActivity.class));
                            Toast.makeText(SettingsActivity.this, "Profile Information Updated Successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(SettingsActivity.this, "Error Occurred", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }

                }
            });
        } else {
            Toast.makeText(SettingsActivity.this, "Profile Image Not Selected", Toast.LENGTH_SHORT).show();
        }

    }

    private void validateOnlyUserInfo() {

        if (getType.equalsIgnoreCase("Driver")) {
            if (name.getText().toString().isEmpty()) {
                name.setError("Please Enter Your Name");
                cancel = true;
                name.requestFocus();
            } else if (city.getText().toString().isEmpty()) {
                city.setError("Please Enter Your City Name");
                cancel = true;
                city.requestFocus();
            } else if (age.getText().toString().isEmpty()) {
                age.setError("Please Enter Your Age");
                cancel = true;
                age.requestFocus();
            } else if (maritalStatus.getText().toString().isEmpty()) {
                maritalStatus.setError("Please Enter Your Marital Status");
                cancel = true;
                maritalStatus.requestFocus();
            } else if (phoneNumber.getText().toString().isEmpty()) {
                phoneNumber.setError("Please Enter Your Phone Number");
                cancel = true;
                phoneNumber.requestFocus();
            } else if (country.getText().toString().isEmpty()) {
                country.setError("Please Enter Your Country Name");
                cancel = true;
                country.requestFocus();
            } else if (address.getText().toString().isEmpty()) {
                address.setError("Please Enter Your Address");
                cancel = true;
                address.requestFocus();
            } else if (radioGroup.getCheckedRadioButtonId() == -1) {
                Toast.makeText(SettingsActivity.this, "Please Select Your Gender", Toast.LENGTH_SHORT).show();
            } else if (carName.getText().toString().isEmpty()) {
                carName.setError("Please Enter The Name of Your Car");
                cancel = true;
                carName.requestFocus();
            } else if (carPlateNumber.getText().toString().isEmpty()) {
                carPlateNumber.setError("Please Enter Your Car Plate Number");
                cancel = true;
                carPlateNumber.requestFocus();
            } else if (carColor.getText().toString().isEmpty()) {
                carColor.setError("Please Specify The Colour of Your Car");
                cancel = true;
                carColor.requestFocus();
            } else if (carImageUri == null) {
                carImagePicker.setText("Please Upload Your Car Image");
                carImagePicker.setTextColor(Color.RED);
            } else {
                //go ahead and save only the user information and leave the profile picture

                int selectedID = radioGroup.getCheckedRadioButtonId();
                male = (RadioButton) findViewById(selectedID);
                female = (RadioButton) findViewById(selectedID);
                if (male.isChecked()) {
                    selectedGender = male.getText().toString();
                } else if (female.isChecked()) {
                    selectedGender = female.getText().toString();
                } else {
                    selectedGender = "Not specify";
                }

                final ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setTitle("Updating Profile");
                progressDialog.setMessage("Please wait while we are updating your account information");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                HashMap<String, Object> hashMap = new HashMap<>();
                //hashMap.put("ProfileImage", myProfileImaeUri);
                hashMap.put("name", name.getText().toString());
                hashMap.put("city", city.getText().toString());
                hashMap.put("age", age.getText().toString());
                hashMap.put("maritalStatus", maritalStatus.getText().toString());
                hashMap.put("phoneNumber", phoneNumber.getText().toString());
                hashMap.put("country", country.getText().toString());
                hashMap.put("address", address.getText().toString());
                hashMap.put("gender", selectedGender);

                if (getType.equalsIgnoreCase("Driver")) {
                    uploadCarImage();
                    hashMap.put("carName", carName.getText().toString());
                    hashMap.put("carColour", carColor.getText().toString());
                    hashMap.put("carPlateNumber", carPlateNumber.getText().toString());
                }
                databaseReference.child(mAuth.getCurrentUser().getUid()).updateChildren(hashMap);

                progressDialog.dismiss();
                if (getType.equalsIgnoreCase("Driver")) {

                    startActivity(new Intent(SettingsActivity.this, DriversMapsActivity.class));
                    Toast.makeText(SettingsActivity.this, "Profile Information Updated Successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    startActivity(new Intent(SettingsActivity.this, CustomerMapsActivity.class));
                    Toast.makeText(SettingsActivity.this, "Profile Information Updated Successfully", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        } else {
            if (name.getText().toString().isEmpty()) {
                name.setError("Please Enter Your Name");
                cancel = true;
                name.requestFocus();
            } else if (city.getText().toString().isEmpty()) {
                city.setError("Please Enter Your City Name");
                cancel = true;
                city.requestFocus();
            } else if (age.getText().toString().isEmpty()) {
                age.setError("Please Enter Your Age");
                cancel = true;
                age.requestFocus();
            } else if (maritalStatus.getText().toString().isEmpty()) {
                maritalStatus.setError("Please Enter Your Marital Status");
                cancel = true;
                maritalStatus.requestFocus();
            } else if (phoneNumber.getText().toString().isEmpty()) {
                phoneNumber.setError("Please Enter Your Phone Number");
                cancel = true;
                phoneNumber.requestFocus();
            } else if (country.getText().toString().isEmpty()) {
                country.setError("Please Enter Your Country Name");
                cancel = true;
                country.requestFocus();
            } else if (address.getText().toString().isEmpty()) {
                address.setError("Please Enter Your Address");
                cancel = true;
                address.requestFocus();
            } else if (radioGroup.getCheckedRadioButtonId() == -1) {
                Toast.makeText(SettingsActivity.this, "Please Select Your Gender", Toast.LENGTH_SHORT).show();
            } else {


                final ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setTitle("Updating Profile");
                progressDialog.setMessage("Please wait while we are updating your account information");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                int selectedID = radioGroup.getCheckedRadioButtonId();
                male = (RadioButton) findViewById(selectedID);
                female = (RadioButton) findViewById(selectedID);
                if (male.isChecked()) {
                    selectedGender = male.getText().toString();
                } else if (female.isChecked()) {
                    selectedGender = female.getText().toString();
                } else {
                    selectedGender = "Not specify";
                }
                HashMap<String, Object> hashMap = new HashMap<>();
                //hashMap.put("ProfileImage", myProfileImaeUri);
                hashMap.put("name", name.getText().toString());
                hashMap.put("city", city.getText().toString());
                hashMap.put("age", age.getText().toString());
                hashMap.put("maritalStatus", maritalStatus.getText().toString());
                hashMap.put("phoneNumber", phoneNumber.getText().toString());
                hashMap.put("country", country.getText().toString());
                hashMap.put("address", address.getText().toString());
                hashMap.put("gender", selectedGender);

//                if (getType.equalsIgnoreCase("Driver")) {
//                    uploadCarImage();
//                    hashMap.put("carName", carName.getText().toString());
//                    hashMap.put("carColour", carColor.getText().toString());
//                    hashMap.put("carPlateNumber", carPlateNumber.getText().toString());
//                    hashMap.put("carImage", myCarImageUri);
//                }
                databaseReference.child(mAuth.getCurrentUser().getUid()).updateChildren(hashMap);


//                if (getType.equalsIgnoreCase("Driver")) {
//
//                    startActivity(new Intent(SettingsActivity.this, DriversMapsActivity.class));
//                    Toast.makeText(SettingsActivity.this, "Profile Information Updated Successfully", Toast.LENGTH_SHORT).show();
//                    finish();
//                } else {
                progressDialog.dismiss();
                startActivity(new Intent(SettingsActivity.this, CustomerMapsActivity.class));
                Toast.makeText(SettingsActivity.this, "Profile Information Updated Successfully", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void getUserInfoDisplay(){
        databaseReference.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getChildrenCount()>0){
                    String name1 = snapshot.child("name").getValue().toString();
                    String city1 = snapshot.child("city").getValue().toString();
                    String age1 = snapshot.child("age").getValue().toString();
                    String maritalStatus1 = snapshot.child("maritalStatus").getValue().toString();
                    String phone = snapshot.child("phoneNumber").getValue().toString();
                    String country1 = snapshot.child("country").getValue().toString();
                    String address1 = snapshot.child("address").getValue().toString();
                    String gender1 = snapshot.child("gender").getValue().toString();


                    name.setText(name1);
                    city.setText(city1);
                    age.setText(age1);
                    maritalStatus.setText(maritalStatus1);
                    phoneNumber.setText(phone);
                    country.setText(country1);
                    address.setText(address1);
                    if (snapshot.hasChild("ProfileImage")){
                        String imageProfile = snapshot.child("ProfileImage").getValue().toString();
                        Picasso.get().load(imageProfile).into(profileImage);
                    }

                    if (getType.equalsIgnoreCase("Driver")){
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}