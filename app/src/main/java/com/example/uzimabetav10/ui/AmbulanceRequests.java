package com.example.uzimabetav10.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.uzimabetav10.LocationService.LocationService;
import com.example.uzimabetav10.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AmbulanceRequests extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private TextView numberText, ambulanceTxt;
    private Spinner incidentSpinner, incidentPopupSpinner;
    private Button sendButton, buttonCancel,buttonYes,buttonElse, buttonSend , buttonOkay;
    String incident_type,incident_type2;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private String user_id,  userCity;
    private EditText descText ,descriptionText,patientName,latText,longText,phoneNum;

    private ImageView backImage;
    //String lat , lng;
    Dialog myDialog, myDialog2;
    List<Address> adresses;
    Geocoder geocoder;
    String latitude ,longitude;
    double lat , lng;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ambulance_requests);


        if(Build.VERSION.SDK_INT >= 23){
            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                //Request Location
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION} , 1 );


            }else{
                //Req location
                startService();

            }
        }else{
            //start the location service
            startService();
        }

        //firebase setup
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        user_id = mAuth.getCurrentUser().getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();

        //check if user is suspended
        checkForSuspension();

        //toolbar setup
        Toolbar toolbar = findViewById(R.id.single_post_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Ambulance Services");

        myDialog=new Dialog(this);
        myDialog2 =new Dialog(this);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AmbulanceRequests.this, MainActivity.class));
                finish();
            }
        });

        progressDialog=new ProgressDialog(this);


        //map widgets
        descriptionText = findViewById(R.id.description_text);
        incidentSpinner = findViewById(R.id.spinner_incident);
        sendButton = findViewById(R.id.send_button);
        ambulanceTxt = findViewById(R.id.ambulance_txt);
        buttonElse = findViewById(R.id.else_button);


        //setupp spinner adapter

        ArrayAdapter<CharSequence> incidentAdapter = ArrayAdapter.createFromResource(this,R.array.incident,android.R.layout.simple_spinner_item);
        incidentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        incidentSpinner.setAdapter(incidentAdapter);
        incidentSpinner.setOnItemSelectedListener(this);

        ambulanceTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AmbulanceRequests.this,AmbulanceCover.class));
            }
        });



        //retrieve user coordinates

       /* Intent retrieveIntent= getIntent();
        lat= retrieveIntent.getStringExtra("LATITUDE");
        lng= retrieveIntent.getStringExtra("LONGITUDE");*/



        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myDialog.setContentView(R.layout.popup);
                buttonCancel=(Button) myDialog.findViewById(R.id.button_no);
                buttonYes=(Button) myDialog.findViewById(R.id.button_yes);
                TextView dialogText = (TextView) myDialog.findViewById(R.id.dialog_text);

                dialogText.setText(" This service for people who are within Nairobi county only , if you are not please contact +254789632451");



                buttonCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        myDialog.dismiss();
                    }
                });

                buttonYes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        sendToDatabase();
                        myDialog.dismiss();

                    }
                });

                myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                myDialog.show();


            }
        });

        //ambulance for somebody else popup
        buttonElse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                myDialog2.setContentView(R.layout.popup_ambulance);
                buttonSend=(Button) myDialog2.findViewById(R.id.button_send_popup);
                incidentPopupSpinner = (Spinner) myDialog2.findViewById(R.id.spinner_situation);
                backImage = (ImageView) myDialog2.findViewById(R.id.back_arrow);
                descText = myDialog2.findViewById(R.id.descrption_words);
                patientName = myDialog2.findViewById(R.id.patient_text);
                latText = myDialog2.findViewById(R.id.lat_txt);
                longText = myDialog2.findViewById(R.id.long_txt);
                phoneNum = myDialog2.findViewById(R.id.phone_num);



                //final String description = descText.getText().toString();
                //final String patient = patientName.getText().toString();







                ArrayAdapter<CharSequence> incidentAdapter = ArrayAdapter.createFromResource(myDialog2.getContext(),R.array.incident,android.R.layout.simple_spinner_item);
                incidentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                incidentPopupSpinner.setAdapter(incidentAdapter);
                incidentPopupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                        // your code here
                        Spinner spin = (Spinner)parentView;
                        if(spin.getId() == R.id.spinner_situation)
                        {
                            String txt = parentView.getItemAtPosition(position).toString();
                            incident_type2=txt;

                            Toast.makeText(myDialog.getContext(),"Incident selected is"+incident_type2,Toast.LENGTH_SHORT).show();




                        }

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parentView) {
                        // your code here
                        Toast.makeText(myDialog.getContext(),"Please specify Incident",Toast.LENGTH_SHORT).show();
                    }

                });

                backImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        myDialog2.dismiss();
                    }
                });
                buttonSend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        progressDialog.setMessage("Sending emergency Details...");
                        progressDialog.show();

                        //updateDatabase();

                        final String description = descText.getText().toString();
                        final String patient = patientName.getText().toString();
                        final String latitude= latText.getText().toString();
                        final String longitude = longText.getText().toString();
                        final String phone = phoneNum.getText().toString();

                        Date c = Calendar.getInstance().getTime();
                       Toast.makeText(myDialog2.getContext(),"The current time is:"+c,Toast.LENGTH_SHORT).show();

                        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
                        String formattedDate = df.format(c);






                        Toast.makeText(myDialog.getContext(),"Patient details are" + patient+description,Toast.LENGTH_SHORT).show();


                        final double lat2=Double.parseDouble(latitude);
                        final double log2=Double.parseDouble(longitude);

                        final GeoPoint geopoint = new GeoPoint(lat2,log2);

                        geocoder = new Geocoder(myDialog2.getContext(), Locale.getDefault());

                        try {

                            adresses = geocoder.getFromLocation(lat2, log2, 1);
                            String address = adresses.get(0).getAddressLine(0);

                            String fulladdress = address + "";
                            //String area = adresses.get(0).getLocality();
                            String city = adresses.get(0).getAdminArea();

                            // userArea = area;
                            userCity = city;



                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                        Map<String, Object> userMap= new HashMap<>();

                        userMap.put("name",patient);
                        userMap.put("phone_number",phone);
                        userMap.put("email",null);
                        userMap.put("location",geopoint);
                        userMap.put("incident",incident_type2);
                        userMap.put("description",description);
                        userMap.put("user_id",user_id);
                        userMap.put("county",userCity);
                        userMap.put("time", formattedDate);


                        firebaseFirestore.collection("Ambulance_Requests").document()
                                .set(userMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(AmbulanceRequests.this,"Ambulance request sent",Toast.LENGTH_LONG).show();

                                        descText.setText("");
                                        progressDialog.dismiss();
                                        myDialog2.dismiss();

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        Toast.makeText(AmbulanceRequests.this,"FIRESTORE ERROR: COULD NOT SEND",Toast.LENGTH_LONG).show();

                                        finish();

                                    }




                                });

                    }
                });





                myDialog2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                myDialog2.show();



            }
        });



    }

    void startService(){
        LocationBroadcastReceiver receiver = new LocationBroadcastReceiver();
        IntentFilter filter = new IntentFilter("ACTION_LOC");
        registerReceiver(receiver , filter);

        Intent intent = new Intent(AmbulanceRequests.this , LocationService.class);
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

                Toast.makeText(AmbulanceRequests.this ,  "Help! Location:.\nLatitude:" +latitude+ "\nLongitude:" +longitude ,Toast.LENGTH_SHORT).show();


            }

        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {

        Spinner spin = (Spinner)parent;
        if(spin.getId() == R.id.spinner_incident)
        {
            String txt = parent.getItemAtPosition(position).toString();
             incident_type=txt;

        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

        Toast.makeText(AmbulanceRequests.this,"Please select blood group",Toast.LENGTH_SHORT).show();


    }

    public void sendToDatabase(){

        progressDialog.setMessage("Loading details...");
        progressDialog.show();


        final GeoPoint geopoint = new GeoPoint(lat,lng);

        geocoder = new Geocoder(this, Locale.getDefault());

        Date c = Calendar.getInstance().getTime();
        Toast.makeText(this,"The current time is:"+c,Toast.LENGTH_SHORT).show();

        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        final String formattedDate = df.format(c);


        try {

            adresses = geocoder.getFromLocation(lat, lng, 1);
            String address = adresses.get(0).getAddressLine(0);

            String fulladdress = address + "";
            //String area = adresses.get(0).getLocality();
            String city = adresses.get(0).getAdminArea();

            // userArea = area;
            userCity = city;



        } catch (IOException e) {
            e.printStackTrace();
        }

        //retrieve user details

        DocumentReference docRef = firebaseFirestore.collection("users").document(user_id);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    progressDialog.dismiss();
                    DocumentSnapshot document = task.getResult();
                    assert document != null;
                    if (document.exists()) {
                        String name2 = task.getResult().getString("name");
                        String phn_num = task.getResult().getString("phone");
                        String email = task.getResult().getString("email");

                        String descriptionTxt = descriptionText.getText().toString();



                        Map<String, Object> userMap= new HashMap<>();

                        userMap.put("name",name2);
                        userMap.put("phone_number",phn_num);
                        userMap.put("email",email);
                        userMap.put("location",geopoint);
                        userMap.put("incident",incident_type);
                        userMap.put("description",descriptionTxt);
                        userMap.put("user_id",user_id);
                        userMap.put("county",userCity);
                        userMap.put("time", formattedDate);


                        firebaseFirestore.collection("Ambulance_Requests").document()
                                .set(userMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(AmbulanceRequests.this,"Ambulance request sent",Toast.LENGTH_LONG).show();

                                       descriptionText.setText("");

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        Toast.makeText(AmbulanceRequests.this,"FIRESTORE ERROR: COULD NOT SEND",Toast.LENGTH_LONG).show();

                                        finish();

                                    }


                                });






//19-10-1996
                    } else {

                        Toast.makeText(AmbulanceRequests.this, "DATA DOES NOT EXISTS,PLEASE CREATE YOUR MEDICAL ID", Toast.LENGTH_LONG).show();



                    }
                } else {

                    String error = task.getException().getMessage();
                    Toast.makeText(AmbulanceRequests.this, "(FIRESTORE RETRIEVE ERROR):" + error, Toast.LENGTH_LONG).show();


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

                        Toast.makeText(AmbulanceRequests.this, "DATA DOES NOT EXISTS,PLEASE CREATE YOUR PROFILE", Toast.LENGTH_LONG).show();
                       // startActivity(new Intent(AmbulanceRequests.this, EditProfile.class));


                    }
                } else {

                    String error = task.getException().getMessage();
                    Toast.makeText(AmbulanceRequests.this, "(FIRESTORE RETRIEVE ERROR):" + error, Toast.LENGTH_LONG).show();


                }


            }
        });



    }



    }





