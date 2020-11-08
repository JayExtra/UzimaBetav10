package com.example.uzimabetav10.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.uzimabetav10.LocationService.LocationService;
import com.example.uzimabetav10.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private TextView loginText;
    private Button buttonRegister;
    private EditText emailText, passwrdText;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirebaseFirestore;
    double lat , lng;
    String latitude , longitude;
    List<Address> adresses;
    Geocoder geocoder;

    String userArea , userCity;

    //LocationBroadcastReceiver receiver;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        FirebaseApp.initializeApp(this);
        mAuth= FirebaseAuth.getInstance();
        mFirebaseFirestore = FirebaseFirestore.getInstance();


/*
        if(Build.VERSION.SDK_INT >= 23){
            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                //Request Location
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION} , 1 );


            }else{
                //Req location


                startService();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        decodeBearings();
                    }
                }, 3000);

            }
        }else{
            //start the location service
            startService();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    decodeBearings();

                }
            }, 3000);


        }*/




        //decodeBearings();

        //finding the different views
        progressDialog=new ProgressDialog(this);
        loginText=findViewById(R.id.login_text);
        buttonRegister=findViewById(R.id.button_register);
        emailText=findViewById(R.id.email_field);
        passwrdText=findViewById(R.id.password_field);





        loginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent launchLogin= new Intent(RegisterActivity.this,LoginActivity.class);
                startActivity(launchLogin);
                finish();
            }
        });

        //registration button
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startRegistration();
            }
        });
    }


    private void startRegistration() {
        //get values in the field first
        String email=emailText.getText().toString();
        String password=passwrdText.getText().toString();



        if(TextUtils.isEmpty(email)){
            emailText.setError("please add your email");
        }else if(TextUtils.isEmpty(password)){
            passwrdText.setError("please insert your password");

        }else{


            //to display progress dialog
            progressDialog.setMessage("Registering user...");
            progressDialog.show();


            //fire base authentication process

            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    progressDialog.dismiss();

                    if(task.isSuccessful()){
                        //user is successfully registered start main domicile activity and log in

                        Toast.makeText(RegisterActivity.this,"Registration Successful",Toast.LENGTH_SHORT).show();
                        //increaseCounty();
                        //createCountMonth();

                       // String token_id = FirebaseInstanceId.getInstance().getToken();
                      //  String current_id = mAuth.getCurrentUser().getUid();

                        sendVerificationEmail();

                        finish();
                        startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                        Toast.makeText(RegisterActivity.this,"Registration Successful",Toast.LENGTH_SHORT).show();

                       /* finish();
                        startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                        finish();*/

                    }else{

                        //when user registration fails
                        String errorMessage=task.getException().getMessage();
                        Toast.makeText(RegisterActivity.this,"Error:"+errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }
            });




        }


    }

    @Override
    protected void onPause() {
        super.onPause();
        //unregisterReceiver(receiver);
    }

    private void sendVerificationEmail() {


        FirebaseUser user = mAuth.getCurrentUser();
        user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                Toast.makeText(RegisterActivity.this,"Verification link Has been sent to your email",Toast.LENGTH_LONG).show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(RegisterActivity.this,"Could not send verification Email:"+e.getMessage(),Toast.LENGTH_LONG).show();

            }
        });



    }



   /* void startService(){
        receiver = new LocationBroadcastReceiver();
        IntentFilter filter = new IntentFilter("ACTION_LOC");
        registerReceiver(receiver , filter);

        Intent intent = new Intent(RegisterActivity.this , LocationService.class);
        startService(intent);
    }

    public class LocationBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("ACTION_LOC")){

                lat = intent.getDoubleExtra("latitude" , 0f);
                lng = intent.getDoubleExtra("longitude" , 0f);

                longitude=Double.toString(lng);
                latitude = Double.toString(lat);

                Toast.makeText(RegisterActivity.this ,  " Location:.\nLatitude:" +latitude+ "\nLongitude:" +longitude ,Toast.LENGTH_SHORT).show();


            }

        }
    }



    public void decodeBearings(){

        geocoder = new Geocoder(this, Locale.getDefault());

        try {

            adresses = geocoder.getFromLocation(lat, lng, 1);
            String address = adresses.get(0).getAddressLine(0);

            String fulladdress = address + "";
            String area = adresses.get(0).getLocality();
            String city = adresses.get(0).getAdminArea();

            userArea = area;
            userCity = city;

            Toast.makeText(RegisterActivity.this ,"Location:" + userArea + userCity , Toast.LENGTH_LONG).show();



        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void increaseCounty(){

        Log.d("Tag:", "increaseCounty: " + userCity);

        DocumentReference docRef = mFirebaseFirestore.collection("County_Users").document(userCity);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        DocumentReference countRef = mFirebaseFirestore.collection("County_Users").document(userCity);
// Atomically increment the population of the city by 1.
                        countRef.update("count", FieldValue.increment(1));

                    } else {


                        DocumentReference countRef = mFirebaseFirestore.collection("County_Users").document(userCity);

                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("count", FieldValue.increment(1));

// Atomically increment the population of the city by 1.
                        countRef.set(userMap);

                    }
                } else {

                    Toast.makeText(RegisterActivity.this ,"Error: could not update Count in counties"  , Toast.LENGTH_LONG).show();


                }
            }
        });


    }

    public void createCountMonth(){
        Calendar cal = Calendar.getInstance();
       // Toast.makeText(RegisterActivity.this ,"Month:" +new SimpleDateFormat("MMMM").format(cal.getTime()) , Toast.LENGTH_LONG).show();

        String month = new SimpleDateFormat("MMMM").format(cal.getTime());

        DocumentReference countRef = mFirebaseFirestore.collection("Month_Users").document(month);
// Atomically increment the population of the city by 1.
        countRef.update("count", FieldValue.increment(1));
    }*/
}

