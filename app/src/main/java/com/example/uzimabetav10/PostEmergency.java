package com.example.uzimabetav10;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;


public class PostEmergency extends AppCompatActivity {
    private TextInputEditText titleText, detailsText;
    private ImageView imageSelect;
    private Button sendButton;
    private Uri postImageUri1 = null;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference;
    private String current_user_id , userArea, userCity;

    private ProgressDialog progressDialog;
    public static final int MAX_LENGTH = 100;
    private Double lat, lng;

    List<Address> adresses;
    Geocoder geocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_emergency);


        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseApp.initializeApp(PostEmergency.this);
        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();

        current_user_id = firebaseAuth.getCurrentUser().getUid();


        //setup widgets
        titleText = findViewById(R.id.edit_title);
        detailsText = findViewById(R.id.edit_details);
        imageSelect = findViewById(R.id.image_select);
        sendButton = findViewById(R.id.button_send);

        progressDialog = new ProgressDialog(this);


        //Toolbar settings
        Toolbar toolbar = findViewById(R.id.single_post_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Post Emergency");

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PostEmergency.this, EmergencyFeeds.class));
            }
        });


        imageSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BringImagePicker();
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendTodDatabase();

                //Toast.makeText(PostEmergency.this,"Your Location Latitude and Longitude= "+geoPoint,Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void SendTodDatabase() {

        final String title = titleText.getText().toString();
        final String details = detailsText.getText().toString();

        Intent retrieveIntent = getIntent();
        final String lat = retrieveIntent.getStringExtra("LATITUDE");
        final String log = retrieveIntent.getStringExtra("LONGITUDE");

        Toast.makeText(this, "Your Location:" + "\n" + "Latitude= " + lat + "\n" + "Longitude= " + log, Toast.LENGTH_SHORT).show();

        final Double lat2 = Double.parseDouble(lat);
        final Double log2 = Double.parseDouble(log);

        final GeoPoint geoPoint = new GeoPoint(lat2, log2);


        geocoder = new Geocoder(this, Locale.getDefault());

        Date c = Calendar.getInstance().getTime();
        Toast.makeText(this,"The current time is:"+c,Toast.LENGTH_SHORT).show();

        SimpleDateFormat df = new SimpleDateFormat("dd-M-yyyy");
        final String formattedDate = df.format(c);

        try {

            adresses = geocoder.getFromLocation(lat2, log2, 1);
            String address = adresses.get(0).getAddressLine(0);

            String fulladdress = address + "";
            //String area = adresses.get(0).getLocality();
            String city = adresses.get(0).getAdminArea();

           // userArea = area;
            userCity = city;



            Toast.makeText(PostEmergency.this, "Your area:"+userArea, Toast.LENGTH_SHORT).show();


        } catch (IOException e) {
            e.printStackTrace();
        }



        if(postImageUri1!=null){

            progressDialog.setMessage("Sending emergency...");
            progressDialog.show();

            final String randomName = UUID.randomUUID().toString();


            StorageReference filePath= storageReference.child("Emergency_Images").child(randomName+".jpg");
            filePath.putFile(postImageUri1).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {


                    if(task.isSuccessful()){
                        progressDialog.dismiss();

                        storageReference.child("Emergency_Images").child(randomName+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(final Uri download_uri) {

                                //retrieve user's name first

                                DocumentReference docRef = firebaseFirestore.collection("users").document(current_user_id);
                                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @RequiresApi(api = Build.VERSION_CODES.N)
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {

                                            DocumentSnapshot document = task.getResult();
                                            if (document.exists()) {

                                                String name2 = task.getResult().getString("name");

                                                //combine all details and send to database


                                                Map<String,Object> houseMap =new HashMap<>();
                                                houseMap.put("house_image_uri",download_uri.toString());
                                                houseMap.put("title",title);
                                                houseMap.put("details",details);
                                                houseMap.put("user_id",current_user_id);
                                                houseMap.put("timestamp", FieldValue.serverTimestamp());
                                                houseMap.put("location",geoPoint);
                                                houseMap.put("name",name2);
                                                //houseMap.put("area",userArea);
                                                houseMap.put("county",userCity);
                                                houseMap.put("post_date" , formattedDate);

                                                final Map<String,Object> notificationMap =new HashMap<>();
                                                notificationMap.put("title",title);
                                                notificationMap.put("details",details);
                                                notificationMap.put("user_id",current_user_id);
                                                notificationMap.put("timestamp", FieldValue.serverTimestamp());






                                                firebaseFirestore.collection("Emergency_Posts").document()
                                                        .set(houseMap)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Toast.makeText(PostEmergency.this,"Emergency details sent",Toast.LENGTH_LONG).show();


                                                                //notification
                                                                firebaseFirestore.collection("Notifications").document().set(notificationMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {

                                                                        Toast.makeText(PostEmergency.this,"Good",Toast.LENGTH_LONG).show();


                                                                    }
                                                                });

                                                                startActivity(new Intent(PostEmergency.this,EmergencyFeeds.class));




                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {

                                                                Toast.makeText(PostEmergency.this,"Error sending",Toast.LENGTH_LONG).show();


                                                            }


                                                        });



                                            } else {


                                                Toast.makeText(PostEmergency.this, "DATA DOES NOT EXISTS,PLEASE REGISTER", Toast.LENGTH_LONG).show();


                                            }
                                        } else {


                                            String error = task.getException().getMessage();
                                            Toast.makeText(PostEmergency.this, "(FIRESTORE RETRIEVE ERROR):" + error, Toast.LENGTH_LONG).show();


                                        }


                                    }
                                });





                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {


                                Toast.makeText(PostEmergency.this,"Error uploading",Toast.LENGTH_LONG).show();


                            }
                        });







                    }else{

                        Toast.makeText(PostEmergency.this,"ERROR UPLOADING DETAILS",Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();


                    }

                }
            });


        } else {

            DocumentReference docRef = firebaseFirestore.collection("users").document(current_user_id);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {

                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {

                            String name2 = task.getResult().getString("name");

                            //combine all details and send to database


                            Map<String,Object> houseMap =new HashMap<>();
                            houseMap.put("title",title);
                            houseMap.put("details",details);
                            houseMap.put("user_id",current_user_id);
                            houseMap.put("timestamp", FieldValue.serverTimestamp());
                            houseMap.put("location",geoPoint);
                            houseMap.put("house_image_uri",null);
                            houseMap.put("name",name2);
                            //houseMap.put("area",userArea);
                            houseMap.put("county",userCity);




                            firebaseFirestore.collection("Emergency_Posts").document()
                                    .set(houseMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(PostEmergency.this,"Emergency details sent",Toast.LENGTH_LONG).show();

                                            startActivity(new Intent(PostEmergency.this,EmergencyFeeds.class));


                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                            Toast.makeText(PostEmergency.this,"Error sending",Toast.LENGTH_LONG).show();


                                        }


                                    });








                        } else {


                            Toast.makeText(PostEmergency.this, "DATA DOES NOT EXISTS,PLEASE REGISTER", Toast.LENGTH_LONG).show();


                        }
                    } else {


                        String error = task.getException().getMessage();
                        Toast.makeText(PostEmergency.this, "(FIRESTORE RETRIEVE ERROR):" + error, Toast.LENGTH_LONG).show();


                    }


                }
            });








        }





    }



    private void BringImagePicker() {

        // start picker to get image for cropping and then use the image in cropping activity
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(2, 1)
                .setMinCropResultSize(520,520)
                .start(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == Activity.RESULT_OK) {

                postImageUri1=result.getUri();
                imageSelect.setImageURI(postImageUri1);



            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error= result.getError();

            }
        }


    }
}
