package com.example.uzimabetav10;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    private CardView emergencyCard, medIdCard,ambulanceCard,walletCard,deploymentCard;
    private ImageView slideshow;
    private FloatingActionButton panicActionButton;
    private String user_id;
    private FirebaseFirestore firebaseFirestore;

    //private RequestQueue requestQueue;

    private static final int REQUEST_LOCATION = 1;

    private LocationManager locationManager;
    String latitude, longitude;
    String number1, number2,mainAdress;

    List<Address>adresses;
    Geocoder geocoder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialize Firebase app and get Firebase instance
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        user_id = mAuth.getCurrentUser().getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();

        //initialize volley object
       // requestQueue = Volley.newRequestQueue(this);


        //map widgets
        emergencyCard = findViewById(R.id.emergency_card);
        walletCard = findViewById(R.id.wallet_card);
        deploymentCard = findViewById(R.id.deployments_card);
        //medIdCard = findViewById(R.id.med_id_card);
        ambulanceCard = findViewById(R.id.ambulance_card);
        slideshow = findViewById(R.id.slide_img);
        panicActionButton = findViewById(R.id.fab_panic);


        //setup geocoder

        geocoder = new Geocoder(this, Locale.getDefault());


        String currentuser = FirebaseAuth.getInstance().getCurrentUser().getUid();


        //Toast.makeText(this,"user id is:"+currentuser,Toast.LENGTH_SHORT).show();

        Toolbar toolbar = findViewById(R.id.single_post_toolbar);
        setSupportActionBar(toolbar);

        //Request permission to access user location
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        getUserLocation();


        //initialize the progress dialog
        progressDialog = new ProgressDialog(this);

        emergencyCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent emergencyIntent = new Intent(MainActivity.this, EmergencyFeeds.class);
                emergencyIntent.putExtra("LATITUDE", latitude);
                emergencyIntent.putExtra("LONGITUDE", longitude);
                startActivity(emergencyIntent);
            }
        });

        //medIdCard.setOnClickListener(new View.OnClickListener() {
           // @Override
            //public void onClick(View view) {
               // Intent emergencyIntent = new Intent(MainActivity.this, MedicalID.class);
               // startActivity(emergencyIntent);
            //}
        //});

        ambulanceCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent ambulance = new Intent(MainActivity.this,AmbulanceRequests.class);
                ambulance.putExtra("LATITUDE",latitude);
                ambulance.putExtra("LONGITUDE",longitude);
                startActivity(ambulance);
            }
        });

        walletCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, Wallet.class));
            }
        });

        deploymentCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, Deployments.class));
            }
        });

        panicActionButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {


                getAddress();
                sendMessage();


                //Toast.makeText(MainActivity.this, "Messages have been sent to your listed Emergency contacts and Uzima call center", Toast.LENGTH_SHORT).show();


                startActivity(new Intent(MainActivity.this, FlashingActivity.class));

                return false;

            }
        });

    }

    private void getUserLocation() {

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //Check if gps is enabled
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            OnGPS();

        } else {
            getLocation();
        }
    }

    private void getLocation() {

        //Check Permissions again

        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this,

                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            Location LocationGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location LocationNetwork = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            Location LocationPassive = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

            if (LocationGps != null) {
                double lat = LocationGps.getLatitude();
                double longi = LocationGps.getLongitude();

                latitude = String.valueOf(lat);
                longitude = String.valueOf(longi);

                Toast.makeText(MainActivity.this, "Your Location:" + "\n" + "Latitude= " + latitude + "\n" + "Longitude= " + longitude, Toast.LENGTH_SHORT).show();
            } else if (LocationNetwork != null) {
                double lat = LocationNetwork.getLatitude();
                double longi = LocationNetwork.getLongitude();

                latitude = String.valueOf(lat);
                longitude = String.valueOf(longi);

                Toast.makeText(MainActivity.this, "Your Location:" + "\n" + "Latitude= " + latitude + "\n" + "Longitude= " + longitude, Toast.LENGTH_SHORT).show();
            } else if (LocationPassive != null) {
                double lat = LocationPassive.getLatitude();
                double longi = LocationPassive.getLongitude();

                latitude = String.valueOf(lat);
                longitude = String.valueOf(longi);

                Toast.makeText(MainActivity.this, "Your Location:" + "\n" + "Latitude= " + latitude + "\n" + "Longitude= " + longitude, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Can't Get Your Location", Toast.LENGTH_SHORT).show();
            }

            //Thats All Run Your App
        }


    }

    private void OnGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.profile:
                startActivity(new Intent(MainActivity.this, Profile.class));
                return true;

            case R.id.logout:
                progressDialog.setMessage("Signing out...");
                progressDialog.show();

                mAuth.signOut();
                finish();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                return true;

            case R.id.contact_sup:
                Toast.makeText(this, "contact was selected", Toast.LENGTH_SHORT).show();
                return true;

            default:

                return super.onOptionsItemSelected(item);
        }

    }


    //retrieve emergency contacts
    public void sendMessage() {


//******checking on data within the database on whether it already exist then fetch the user details and place them in the required fields************


        DocumentReference docRef = firebaseFirestore.collection("User_Med_IDs").document(user_id);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    progressDialog.dismiss();
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        String contact1 = task.getResult().getString("emergency_contact1").trim();
                        String contact2 = task.getResult().getString("emergency_contact2");




                        //Toast.makeText(MainActivity.this, "Contacts are:"+number1 +number2, Toast.LENGTH_SHORT).show();

                        //request permission to send sms

                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.SEND_SMS, Manifest.permission.READ_SMS}, PackageManager.PERMISSION_GRANTED);


                        String message = "I am currently in pain or danger and need urgent medical assistance! Please help. I am in:"+mainAdress+"/n sent through Uzima Mobile Application";

                        SmsManager mysmsManager = SmsManager.getDefault();
                        mysmsManager.sendTextMessage(contact1,null,message,null,null);

                        Toast.makeText(MainActivity.this, "success!!", Toast.LENGTH_SHORT).show();





//19-10-1996
                    } else {

                        Toast.makeText(MainActivity.this, "THE USER DOES NOT HAVE ANY EMERGENCY CONTACTS LISTED", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(MainActivity.this, FlashingActivity.class));


                    }
                } else {

                    String error = task.getException().getMessage();
                    Toast.makeText(MainActivity.this, "(FIRESTORE RETRIEVE ERROR):" + error, Toast.LENGTH_LONG).show();


                }


            }
        });


    }


    //sends the emergency messages to emergency contacts


    //get actual address of the user
    public void getAddress() {

        Double lat = Double.parseDouble(latitude);
        Double lang = Double.parseDouble(longitude);

        //setup geocoder

        geocoder = new Geocoder(this, Locale.getDefault());

        try {

            adresses = geocoder.getFromLocation(lat,lang,1);
            String address = adresses.get(0).getAddressLine(0);
            String area = adresses.get(0).getLocality();
            String city = adresses.get(0).getAdminArea();

            String fulladdress=  address+""+area+""+city;

            mainAdress = fulladdress;

            //Toast.makeText(MainActivity.this, "Your Location:"+mainAdress, Toast.LENGTH_SHORT).show();



        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
