package com.example.uzimabetav10;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.uzimabetav10.LocationService.LocationService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;

public class FlashingActivity extends AppCompatActivity {

    private ImageButton backBtn;
    private ImageView cntrImg;

    List<Address> adresses;
    Geocoder geocoder;
    String mainAdress;
    String latitude, longitude, userCity;
    double lat, lng;


    MediaPlayer mediaPlayer;
    private FirebaseFirestore firebaseFirestore;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    private String user_id;
    public String CONDITION = "new";
    public String STATUS = "accepted";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashing);


        startService();

        /*if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //Request Location
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);


            } else {
                //Req location
                startService();

            }
        } else {
            //start the location service
            startService();
        }*/


        //initialize Firebase app and get Firebase instance
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        user_id = mAuth.getCurrentUser().getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();

        progressDialog = new ProgressDialog(this);


        backBtn = findViewById(R.id.back_btn);
        cntrImg = findViewById(R.id.cnter_img);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                sendMessage();
            }
        }, 5000);


        //start process



        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mediaPlayer.stop();


                startActivity(new Intent(FlashingActivity.this, MainActivity.class));

                finish();


            }
        });

    }

    void startService() {
        LocationBroadcastReceiver receiver = new LocationBroadcastReceiver();
        IntentFilter filter = new IntentFilter("ACTION_LOC");
        registerReceiver(receiver, filter);

        Intent intent = new Intent(FlashingActivity.this, LocationService.class);
        startService(intent);
    }

    public void startSiren() {

        mediaPlayer = MediaPlayer.create(this, R.raw.warning1);
        mediaPlayer.start();
        mediaPlayer.setLooping(true);

    }

    @SuppressLint("WrongConstant")
    public void startLights() {

        ObjectAnimator anim = ObjectAnimator.ofInt(cntrImg, "BackgroundColor", Color.RED, Color.BLUE, Color.WHITE);
        anim.setDuration(120);
        anim.setEvaluator(new ArgbEvaluator());
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        anim.start();


    }

    public class LocationBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("ACTION_LOC")) {

                lat = intent.getDoubleExtra("latitude", 0f);
                lng = intent.getDoubleExtra("longitude", 0f);

                longitude = Double.toString(lng);
                latitude = Double.toString(lat);

                Toast.makeText(FlashingActivity.this, "Help! Location:.\nLatitude:" + latitude + "\nLongitude:" + longitude, Toast.LENGTH_SHORT).show();


            }

        }
    }


    //retrieve emergency contacts
    public void sendMessage() {

        progressDialog.setMessage("Sending message...");
        progressDialog.show();


//******checking on data within the database on whether it already exist then fetch the user details and place them in the required fields************


        DocumentReference docRef = firebaseFirestore.collection("users").document(user_id);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    progressDialog.dismiss();
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        String contact1 = task.getResult().getString("emergency_contact").trim();
                        //String contact2 = task.getResult().getString("emergency_contact2");

                        //Toast.makeText(MainActivity.this, "Contact 1 is: "+contact1, Toast.LENGTH_SHORT).show();

                        //setup geocoder

                        geocoder = new Geocoder(FlashingActivity.this, Locale.getDefault());

                        if(lat!=0 && lng!=0){

                            try {

                                adresses = geocoder.getFromLocation(lat, lng, 1);

                                String address = adresses.get(0).getAddressLine(0);
                                String area = adresses.get(0).getLocality();
                                String city = adresses.get(0).getAdminArea();

                                String fulladdress = address + "" + area + "" + city;

                                mainAdress = fulladdress;
                                userCity = city;


                                // Toast.makeText(MainActivity.this, "Your Location:"+mainAdress, Toast.LENGTH_SHORT).show();

                            } catch (IOException e) {

                                e.printStackTrace();

                            }



                            //Toast.makeText(MainActivity.this, "Contacts are:"+number1 +number2, Toast.LENGTH_SHORT).show();

                            //request permission to send sms

                            ActivityCompat.requestPermissions(FlashingActivity.this, new String[]{Manifest.permission.SEND_SMS, Manifest.permission.READ_SMS}, PackageManager.PERMISSION_GRANTED);


                            String message = "Help! Location:" + mainAdress + ".\nLatitude:" + latitude + "\nLongitude:" + longitude;

                            //Getting intent and PendingIntent instance
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

                            //Get the SmsManager instance and call the sendTextMessage method to send message
                            SmsManager sms = SmsManager.getDefault();
                            sms.sendTextMessage(contact1, null, message, pi, null);

                            Toast.makeText(getApplicationContext(), "Message Sent successfully!",
                                    Toast.LENGTH_LONG).show();

                            notifyAdmin(mainAdress, lng, lat, user_id, userCity);

                            progressDialog.dismiss();

                        }else{

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                  startService();
                                }
                            }, 5000);
                        }

//19-10-1996
                    } else {

                        Toast.makeText(FlashingActivity.this, "THE USER DOES NOT HAVE ANY EMERGENCY CONTACTS LISTED", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(FlashingActivity.this, FlashingActivity.class));


                    }
                } else {

                    String error = task.getException().getMessage();
                    Toast.makeText(FlashingActivity.this, "(FIRESTORE RETRIEVE ERROR):" + error, Toast.LENGTH_LONG).show();


                }


            }


        });


    }

    private void notifyAdmin(final String mainAdress, double lng, double lat, final String user_id, final String userCity) {

        final GeoPoint geopoint = new GeoPoint(lat, lng);

        DocumentReference docRef = firebaseFirestore.collection("users").document(user_id);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        final String name = task.getResult().getString("name");
                        String phone = task.getResult().getString("phone");

                        Map<String, Object> dbMap = new HashMap<>();
                        dbMap.put("name", name);
                        dbMap.put("user_id", user_id);
                        dbMap.put("location", geopoint);
                        dbMap.put("phone", phone);
                        dbMap.put("county", userCity);
                        dbMap.put("address", mainAdress);
                        dbMap.put("timestamp", FieldValue.serverTimestamp());

                        firebaseFirestore.collection("Emergency_Channel").document()
                                .set(dbMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        Toast.makeText(FlashingActivity.this, "Distress signal Successfully broadcast to Dispatcher", Toast.LENGTH_SHORT).show();

                                        sendNotification(mainAdress, name, user_id);


                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        Toast.makeText(FlashingActivity.this, "Distress signal Failed to broadcast", Toast.LENGTH_SHORT).show();

                                    }
                                });


                    } else {
                        Toast.makeText(FlashingActivity.this, "You don't have a profile in the database", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(FlashingActivity.this, "Failed to fetch document", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    private void sendNotification(String mainAdress, String name, String user_id) {

        String message = "Distress signal from " + name + " at "+mainAdress;

        Map<String, Object> notMap = new HashMap<>();
        notMap.put("condition", CONDITION);
        notMap.put("description", message);
        notMap.put("from", user_id);
        notMap.put("status", STATUS);
        notMap.put("timestamp", FieldValue.serverTimestamp());

        firebaseFirestore.collection("Dispatcher_Notification").document()
                .set(notMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Toast.makeText(FlashingActivity.this, "Dispatcher Notified!", Toast.LENGTH_SHORT).show();

                        startLights();
                        startSiren();


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(FlashingActivity.this, "Could not send notification", Toast.LENGTH_SHORT).show();

                    }
                });


    }


}

