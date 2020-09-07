package com.example.uzimabetav10;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.uzimabetav10.Models.SliderItem;
import com.example.uzimabetav10.SliderAdapter.SlideAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType;
import com.smarteist.autoimageslider.IndicatorView.draw.controller.DrawController;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    private CardView emergencyCard, medIdCard, ambulanceCard, walletCard, deploymentCard;
    private ImageView slideshow , circleImage;
    private FloatingActionButton panicActionButton;
    private String user_id;
    private FirebaseFirestore firebaseFirestore;

    private Button buttonOkay;

    Dialog myDialog;

    //private RequestQueue requestQueue;

    //private static final int REQUEST_LOCATION = 1;

    private LocationManager locationManager;
    //String latitude, longitude;
    String number1, number2, mainAdress;

    //List<Address> adresses;
   // Geocoder geocoder;

    SliderView sliderView;
    private SlideAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialize Firebase app and get Firebase instance
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        user_id = mAuth.getCurrentUser().getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();

        myDialog=new Dialog(this);

        //initialize volley object
        // requestQueue = Volley.newRequestQueue(this);

        //check if user is signed in
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // User is signed in

            String status = "online";
            String device = "mobile";

            DocumentReference usersRef = firebaseFirestore.collection("users").document(user_id);

// Set the "isCapital" field of the city 'DC'
            usersRef
                    .update("status", status,
                            "device",device)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                           Toast.makeText(MainActivity.this,"Logged in",Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(MainActivity.this,"Failed",Toast.LENGTH_SHORT).show();

                        }
                    });
        } else {
            // No user is signed in
        }

        //check if user is suspended
        checkForSuspension();


        //check if user has profile
        checkIfUserExists();

        //listen for changes
        emergencyListener();

        //map widgets
        emergencyCard = findViewById(R.id.emergency_card);
        walletCard = findViewById(R.id.wallet_card);
        deploymentCard = findViewById(R.id.deployments_card);
        //medIdCard = findViewById(R.id.med_id_card);
        ambulanceCard = findViewById(R.id.ambulance_card);
        panicActionButton = findViewById(R.id.fab_panic);
        circleImage = findViewById(R.id.check_news);



       //image slider

        sliderView = findViewById(R.id.imageSlider);


        adapter = new SlideAdapter(this);
        adapter.addItem(new SliderItem("We value our work" ,"https://mindlercareerlibrarynew.imgix.net/8D-Disaster_Management.png"));
        adapter.addItem(new SliderItem("We go even further" ,"https://image.freepik.com/free-vector/emergency-ambulance-doctors-wearing-mask_23-2148527479.jpg"));
        adapter.addItem(new SliderItem("The ride of your life" ,"https://image.freepik.com/free-vector/emergency-ambulance-coronavirus_23-2148549602.jpg"));


        sliderView.setSliderAdapter(adapter);
        sliderView.setIndicatorAnimation(IndicatorAnimationType.WORM); //set indicator animation by using SliderLayout.IndicatorAnimations. :WORM or THIN_WORM or COLOR or DROP or FILL or NONE or SCALE or SCALE_DOWN or SLIDE and SWAP!!
        sliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
        sliderView.setAutoCycleDirection(SliderView.AUTO_CYCLE_DIRECTION_BACK_AND_FORTH);
        sliderView.setIndicatorSelectedColor(Color.WHITE);
        sliderView.setIndicatorUnselectedColor(Color.GRAY);
        sliderView.setScrollTimeInSec(3);
        sliderView.setAutoCycle(true);
        sliderView.startAutoCycle();


        sliderView.setOnIndicatorClickListener(new DrawController.ClickListener() {
            @Override
            public void onIndicatorClicked(int position) {
                Log.i("GGG", "onIndicatorClicked: " + sliderView.getCurrentPagePosition());
            }
        });


        //setup geocoder

        //geocoder = new Geocoder(this, Locale.getDefault());


        //String currentuser = FirebaseAuth.getInstance().getCurrentUser().getUid();


        //Toast.makeText(this,"user id is:"+currentuser,Toast.LENGTH_SHORT).show();

        Toolbar toolbar = findViewById(R.id.single_post_toolbar);
        setSupportActionBar(toolbar);

     /*   //Request permission to access user location
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        getUserLocation();*/


        //initialize the progress dialog
        progressDialog = new ProgressDialog(this);

        emergencyCard.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {

                circleImage.setImageDrawable(MainActivity.this.getDrawable(R.mipmap.ic_check_grey));
                Intent emergencyIntent = new Intent(MainActivity.this, EmergencyFeeds.class);
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
                Intent ambulance = new Intent(MainActivity.this, AmbulanceRequests.class);
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

                //Toast.makeText(MainActivity.this, "Messages have been sent to your listed Emergency contacts and Uzima call center", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this, FlashingActivity.class));

                return false;

            }
        });

    }

   /* private void getUserLocation() {

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //Check if gps is enabled
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            OnGPS();

        } else {
            getLocation();
        }
    }*/

    /*private void getLocation() {

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


    }*/

   /* private void OnGPS() {
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
    }*/


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
                finish();
                return true;

            case R.id.item1:
                startActivity(new Intent(MainActivity.this, Profile.class));
                finish();
                return true;

            case R.id.item2:
                startActivity(new Intent(MainActivity.this, Notifications.class));
                finish();
                return true;

            case R.id.logout:

                progressDialog.setMessage("Signing out...");
                progressDialog.show();

                Map<String , Object> tokenMapRemove = new HashMap<>();
                tokenMapRemove.put("token_id" ,FieldValue.delete());

                firebaseFirestore.collection("users").document(user_id).update(tokenMapRemove).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        String status = "offline";
                        String device = "mobile";

                        DocumentReference usersRef = firebaseFirestore.collection("users").document(user_id);

// Set the "isCapital" field of the city 'DC'
                        usersRef
                                .update("status", status)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(MainActivity.this,"Logged off",Toast.LENGTH_SHORT).show();

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        Toast.makeText(MainActivity.this,"Failed",Toast.LENGTH_SHORT).show();

                                    }
                                });

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(MainActivity.this,"Error on token delete" + e.getMessage(),Toast.LENGTH_SHORT).show();

                    }
                });


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


   /* //retrieve emergency contacts
    public void sendMessage() {


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

                        double lat = Double.parseDouble(latitude);
                        double lang = Double.parseDouble(longitude);

                        //setup geocoder

                        geocoder = new Geocoder(MainActivity.this, Locale.getDefault());

                        try {

                            adresses = geocoder.getFromLocation(lat, lang, 1);
                            String address = adresses.get(0).getAddressLine(0);
                            String area = adresses.get(0).getLocality();
                            String city = adresses.get(0).getAdminArea();

                            String fulladdress = address + "" + area + "" + city;

                            mainAdress = fulladdress;

                           // Toast.makeText(MainActivity.this, "Your Location:"+mainAdress, Toast.LENGTH_SHORT).show();


                        } catch (IOException e) {
                            e.printStackTrace();
                        }





                        //Toast.makeText(MainActivity.this, "Contacts are:"+number1 +number2, Toast.LENGTH_SHORT).show();

                        //request permission to send sms

                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.SEND_SMS, Manifest.permission.READ_SMS}, PackageManager.PERMISSION_GRANTED);


                        String message = "Help! Location:"+mainAdress+".\nLatitude:" +latitude+ "\nLongitude:" +longitude;

                        //Getting intent and PendingIntent instance
                        Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                        PendingIntent pi=PendingIntent.getActivity(getApplicationContext(), 0, intent,0);

                        //Get the SmsManager instance and call the sendTextMessage method to send message
                        SmsManager sms=SmsManager.getDefault();
                        sms.sendTextMessage(contact1, null, message, pi,null);

                        Toast.makeText(getApplicationContext(), "Message Sent successfully!",
                                Toast.LENGTH_LONG).show();

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
*/

    //sends the emergency messages to emergency contacts


    //get actual address of the user
   /* public void getAddress() {

        Double lat = Double.parseDouble(latitude);
        Double lang = Double.parseDouble(longitude);

        //setup geocoder

        geocoder = new Geocoder(this, Locale.getDefault());

        try {

            adresses = geocoder.getFromLocation(lat, lang, 1);
            String address = adresses.get(0).getAddressLine(0);
            String area = adresses.get(0).getLocality();
            String city = adresses.get(0).getAdminArea();

            String fulladdress = address + "" + area + "" + city;

            mainAdress = fulladdress;

            //Toast.makeText(MainActivity.this, "Your Location:"+mainAdress, Toast.LENGTH_SHORT).show();


        } catch (IOException e) {
            e.printStackTrace();
        }


    }*/

    public void checkIfUserExists() {

        // progressDialog.setMessage("Checking details...");
        //progressDialog.show();


        DocumentReference docRef = firebaseFirestore.collection("users").document(user_id);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    // progressDialog.dismiss();
                    DocumentSnapshot document = task.getResult();
                    assert document != null;
                    if (document.exists()) {
                        String name2 = task.getResult().getString("name");

                        Toast.makeText(MainActivity.this, "Welcome back " + name2, Toast.LENGTH_LONG).show();


//19-10-1996
                    } else {

                        //progressDialog.dismiss();

                        Toast.makeText(MainActivity.this, "DATA DOES NOT EXISTS,PLEASE CREATE YOUR PROFILE", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(MainActivity.this, EditProfile.class));


                    }
                } else {

                    String error = task.getException().getMessage();
                    Toast.makeText(MainActivity.this, "(FIRESTORE RETRIEVE ERROR):" + error, Toast.LENGTH_LONG).show();


                }


            }
        });


    }

    public void emergencyListener() {

        firebaseFirestore.collection("Emergency_Feeds")
                .whereEqualTo("priority", 1)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Toast.makeText(MainActivity.this, "listen:error", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            switch (dc.getType()) {
                                case ADDED:
                                   // Log.d(TAG, "New city: " + dc.getDocument().getData());

                                    circleImage.setImageDrawable(MainActivity.this.getDrawable(R.mipmap.ic_check_orange));

                                    break;
                                case MODIFIED:
                                    //Log.d(TAG, "Modified city: " + dc.getDocument().getData());
                                    break;
                                case REMOVED:
                                    //Log.d(TAG, "Removed city: " + dc.getDocument().getData());
                                    break;
                            }
                        }

                    }
                });

    }

    public void checkForSuspension(){

        DocumentReference docRef = firebaseFirestore.collection("Suspended_Accounts").document(user_id);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    // progressDialog.dismiss();
                    DocumentSnapshot document = task.getResult();
                    assert document != null;
                    if (document.exists()) {
                        String name2 = task.getResult().getString("name");

                        myDialog.setContentView(R.layout.popup_suspended);
                        buttonOkay=(Button) myDialog.findViewById(R.id.okay_btn);



                        buttonOkay.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                finish();
                                System.exit(0);
                            }
                        });



                        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        myDialog.show();




                        //Toast.makeText(MainActivity.this, "Welcome back " + name2, Toast.LENGTH_LONG).show();


//19-10-1996
                    } else {

                        //progressDialog.dismiss();

                        Toast.makeText(MainActivity.this, "DATA DOES NOT EXISTS,PLEASE CREATE YOUR PROFILE", Toast.LENGTH_LONG).show();
                        //startActivity(new Intent(MainActivity.this, EditProfile.class));


                    }
                } else {

                    String error = task.getException().getMessage();
                    Toast.makeText(MainActivity.this, "(FIRESTORE RETRIEVE ERROR):" + error, Toast.LENGTH_LONG).show();


                }


            }
        });



    }
}
