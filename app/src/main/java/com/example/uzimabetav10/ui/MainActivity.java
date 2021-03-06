package com.example.uzimabetav10.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.uzimabetav10.ContactSupport;
import com.example.uzimabetav10.LocationService.LocationService;
import com.example.uzimabetav10.Models.SliderItem;
import com.example.uzimabetav10.R;
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
import com.google.firebase.firestore.QuerySnapshot;
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType;
import com.smarteist.autoimageslider.IndicatorView.draw.controller.DrawController;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    private CardView emergencyCard, medIdCard, ambulanceCard, walletCard, deploymentCard;
    private ImageView slideshow , circleImage;
    private FloatingActionButton panicActionButton;
    private String user_id;
    private FirebaseFirestore firebaseFirestore;
    private TextView verifyText , dateText , coverStatus;

    private Button buttonOkay , btnVerify , btnView;

    private LocationBroadcastReceiver receiver;

    double lat , lng;
    String latitude , longitude;
    List<Address> adresses;
    Geocoder geocoder;
    String userArea , userCity;
    String TAG = "MainActivity:";



    Dialog myDialog;

    Handler handler = new Handler();
    Runnable runnable;
    int delay = 10*1000; //Delay for 10 seconds.  One second = 1000 milliseconds.

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

        checkLocationPermission();

        //initialize Firebase app and get Firebase instance
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        user_id = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();




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


        }


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
                           //Toast.makeText(MainActivity.this,"Logged in",Toast.LENGTH_SHORT).show();
                            checkVerification();
                            Log.d("Sucess @ sign in check " , "success!!");
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
            Log.d("Fail @ sign in check " , "Fail!!");
        }

        //check if user is suspended
        //checkForSuspension();


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
        verifyText = findViewById(R.id.vrfy_txt);
        btnVerify = findViewById(R.id.vrfy_btn);
        dateText = findViewById(R.id.date_textview);
        coverStatus = findViewById(R.id.details_text);
        btnView = findViewById(R.id.view_btn);



        btnView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this , CoverPayment.class));
                finish();
            }
        });



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

        getCoverStatus();

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

    @Override
    protected void onStart() {
        super.onStart();

        getCoverStatus();
        checkCoverStatus();

    }

    private void getCoverStatus() {


        firebaseFirestore.collection("Members")
                .document(user_id)
                .addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                        if (e != null) {
                            Toast.makeText(MainActivity.this, "Error while loading!", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, e.toString());
                            return;
                        }
                        if (documentSnapshot.exists()) {
                            Date date = documentSnapshot.getTimestamp("expiry_date").toDate();
                            String status = documentSnapshot.getString("status");

                            if(status.equals(null) || status.equals("expired") ){

                                dateText.setText("You are not currently in our ambulance cover plan or your plan has expired" +
                                        ". Please click the button below to pay for one");
                                coverStatus.setText("N/a");
                                btnVerify.setVisibility(View.VISIBLE);

                            }else{

                                SimpleDateFormat sfd = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                                String final_date = sfd.format(date);

                                String message = "Expiry Date: "+final_date;

                                coverStatus.setText(message);
                                dateText.setText(status);
                                btnView.setVisibility(View.GONE);



                            }



                        }
                    }
                });



    }

    private void checkCoverStatus() {


        firebaseFirestore.collection("Members")
                .document(user_id)
                .addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                        if (e != null) {
                            Toast.makeText(MainActivity.this, "Error while loading!", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, e.toString());
                            return;
                        }
                        if (documentSnapshot.exists()) {
                            Date date = documentSnapshot.getTimestamp("expiry_date").toDate();
                            String status = documentSnapshot.getString("status");

                            SimpleDateFormat sfd = new SimpleDateFormat("dd-MM-yyyy");
                            String expiry_date = sfd.format(date);

                                Date c = Calendar.getInstance().getTime();
                                String todays_date = sfd.format(c);


                                if (expiry_date.compareTo(todays_date) == 0) {
                                    Log.i("app", "Date is  Date c");

                                    DocumentReference washingtonRef = firebaseFirestore.collection("Members").document(user_id);

// Set the "isCapital" field of the city 'DC'
                                    washingtonRef
                                            .update("status", "expired")
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.d(TAG, "DocumentSnapshot successfully updated!");
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.w(TAG, "Error updating document", e);
                                                }
                                            });


                                } else{

                                    Toast.makeText(MainActivity.this , "Cover Status looks okay" , Toast.LENGTH_SHORT).show();



                                }


                        }else{

                            Toast.makeText(MainActivity.this , "Cover Status Doesmt exist , please purchase one" , Toast.LENGTH_SHORT).show();

                        }
                    }
                });



    }


    @Override
    protected void onResume() {
        //start handler as activity become visible

        handler.postDelayed( runnable = new Runnable() {
            public void run() {
                //do something
                checkVerification();
                checkForSuspension();
                decodeBearings();
                handler.postDelayed(runnable, delay);
            }
        }, delay);

        super.onResume();
    }

// If onPause() is not included the threads will double up when you
// reload the activity

    @Override
    protected void onPause() {
       handler.removeCallbacks(runnable); //stop handler when activity not visible
        unregisterReceiver(receiver);
        super.onPause();
    }


    void startService(){

        receiver = new LocationBroadcastReceiver();
        IntentFilter filter = new IntentFilter("ACTION_LOC");
        registerReceiver(receiver , filter);

        Intent intent = new Intent(MainActivity.this , LocationService.class);
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

                Toast.makeText(MainActivity.this ,  " Location:.\nLatitude:" +latitude+ "\nLongitude:" +longitude ,Toast.LENGTH_SHORT).show();


            }

        }
    }


    public void decodeBearings(){

        geocoder = new Geocoder(this, Locale.getDefault());


        if(lat!=0 && lng!=0){

            try {

                adresses = geocoder.getFromLocation(lat, lng, 1);
                String address = adresses.get(0).getAddressLine(0);

                String fulladdress = address + "";
                String area = adresses.get(0).getLocality();
                String city = adresses.get(0).getAdminArea();

                userArea = area;
                userCity = city;

                Toast.makeText(MainActivity.this ,"Location:" + userArea + userCity , Toast.LENGTH_LONG).show();



            } catch (IOException e) {
                e.printStackTrace();
            }


        }else{

            Toast.makeText(MainActivity.this , "Cannot get your current location",Toast.LENGTH_LONG).show();
        }



    }



    private void checkVerification() {
        final FirebaseUser user = mAuth.getCurrentUser();

        if(!user.isEmailVerified()){

            verifyText.setVisibility(View.VISIBLE);
            btnVerify.setVisibility(View.VISIBLE);

            btnVerify.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {



                    user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            Toast.makeText(MainActivity.this,"Verification link Has been sent to your email",Toast.LENGTH_LONG).show();
                            increaseCounty();
                            createCountMonth();



                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(MainActivity.this,"Could not send verification Email:"+e.getMessage(),Toast.LENGTH_LONG).show();

                        }
                    });

                }
            });


        }else {

            Log.d("tag", "checkVerification:account is verified ");



        }
    }

    private void checkLocationPermission() {

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //Request Location
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);


            } else {
                //Req location
                Toast.makeText(MainActivity.this,"Location permission is okay" ,Toast.LENGTH_SHORT).show();

            }
        } else {
            //start the location service
           Toast.makeText(MainActivity.this,"Requires sdk 23 or higher" ,Toast.LENGTH_SHORT).show();
        }
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

                signOut();

                return true;

            case R.id.contact_sup:
               // Toast.makeText(this, "contact was selected", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this , ContactSupport.class));
                finish();
                return true;

            default:

                return super.onOptionsItemSelected(item);
        }

    }

    private void signOut() {

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

                                mAuth.signOut();
                                progressDialog.dismiss();
                                finish();
                                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                                Toast.makeText(MainActivity.this,"Logged off",Toast.LENGTH_SHORT).show();

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Toast.makeText(MainActivity.this,"Failed",Toast.LENGTH_SHORT).show();
                                mAuth.signOut();
                                progressDialog.dismiss();
                                finish();

                            }
                        });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(MainActivity.this,"Error on token delete" + e.getMessage(),Toast.LENGTH_SHORT).show();
                mAuth.signOut();
                progressDialog.dismiss();

            }
        });


    }


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

                                signOut();
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

                        //Toast.makeText(MainActivity.this, "DATA DOES NOT EXISTS,PLEASE CREATE YOUR PROFILE", Toast.LENGTH_LONG).show();
                        //startActivity(new Intent(MainActivity.this, EditProfile.class));
                        Log.d("ACCOUNT CHECK:" , "ACCOUNT IS OKAY");
                        myDialog.dismiss();


                    }
                } else {

                    String error = task.getException().getMessage();
                    Toast.makeText(MainActivity.this, "(FIRESTORE RETRIEVE ERROR):" + error, Toast.LENGTH_LONG).show();


                }


            }
        });



    }

    public void increaseCounty(){

        Log.d("Tag:", "increaseCounty: " + userCity);

        DocumentReference docRef = firebaseFirestore.collection("County_Users").document(userCity);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        DocumentReference countRef = firebaseFirestore.collection("County_Users").document(userCity);
// Atomically increment the population of the city by 1.
                        countRef.update("count", FieldValue.increment(1));

                    } else {


                        DocumentReference countRef = firebaseFirestore.collection("County_Users").document(userCity);

                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("count", FieldValue.increment(1));

// Atomically increment the population of the city by 1.
                        countRef.set(userMap);

                    }
                } else {

                    Toast.makeText(MainActivity.this ,"Error: could not update Count in counties"  , Toast.LENGTH_LONG).show();


                }
            }
        });


    }

    public void createCountMonth(){
        Calendar cal = Calendar.getInstance();
        // Toast.makeText(RegisterActivity.this ,"Month:" +new SimpleDateFormat("MMMM").format(cal.getTime()) , Toast.LENGTH_LONG).show();

        String month = new SimpleDateFormat("MMMM").format(cal.getTime());

        Log.d("tag", "createCountMonth: " + month);

        DocumentReference countRef = firebaseFirestore.collection("Month_Users").document(month);
// Atomically increment the population of the city by 1.
        countRef.update("count", FieldValue.increment(1));
    }

}
