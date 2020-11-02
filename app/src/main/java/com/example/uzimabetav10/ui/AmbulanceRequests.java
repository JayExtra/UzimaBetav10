package com.example.uzimabetav10.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.app.Activity;
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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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

import static androidx.constraintlayout.widget.Constraints.TAG;

public class AmbulanceRequests extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private TextView numberText, ambulanceTxt;
    private Spinner incidentSpinner, incidentPopupSpinner;
    private Button sendButton, buttonCancel, buttonYes, buttonElse, buttonSend, buttonOkay;
    String incident_type, incident_type2;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference;
    private String user_id, userCity;
    private EditText descText, descriptionText, patientName, latText, longText, phoneNum;
    private Uri mainImageURI=null;
    private Uri otherImageUri = null;
    public boolean isChanged = false;

    private ImageView backImage;
    //String lat , lng;
    Dialog myDialog, myDialog2;
    List<Address> adresses;
    Geocoder geocoder;
    String latitude, longitude;
    ImageButton otherImageBtn;
    TextView otherImageUrl;
    double lat, lng;

    private Button attatchImage;
    private TextView imageUrl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ambulance_requests);


        if (Build.VERSION.SDK_INT >= 23) {
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
        }

        //firebase setup
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        user_id = mAuth.getCurrentUser().getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        //check if user is suspended
        checkForSuspension();

        //toolbar setup
        Toolbar toolbar = findViewById(R.id.single_post_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Ambulance Services");

        myDialog = new Dialog(this);
        myDialog2 = new Dialog(this);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AmbulanceRequests.this, MainActivity.class));
                finish();
            }
        });

        progressDialog = new ProgressDialog(this);


        //map widgets
        descriptionText = findViewById(R.id.description_text);
        incidentSpinner = findViewById(R.id.spinner_incident);
        sendButton = findViewById(R.id.send_button);
        ambulanceTxt = findViewById(R.id.ambulance_txt);
        buttonElse = findViewById(R.id.else_button);
        attatchImage = findViewById(R.id.button_attatch_image);
        imageUrl = findViewById(R.id.img_txt);


        //setupp spinner adapter

        ArrayAdapter<CharSequence> incidentAdapter = ArrayAdapter.createFromResource(this, R.array.incident, android.R.layout.simple_spinner_item);
        incidentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        incidentSpinner.setAdapter(incidentAdapter);
        incidentSpinner.setOnItemSelectedListener(this);

        ambulanceTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AmbulanceRequests.this, AmbulanceCover.class));
            }
        });

        attatchImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BringImagePicker();
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
                buttonCancel = (Button) myDialog.findViewById(R.id.button_no);
                buttonYes = (Button) myDialog.findViewById(R.id.button_yes);
                TextView dialogText = (TextView) myDialog.findViewById(R.id.dialog_text);

                dialogText.setText("You are about to send an ambulance request, do you wish to proceed?");


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
                buttonSend = (Button) myDialog2.findViewById(R.id.button_send_popup);
                incidentPopupSpinner = (Spinner) myDialog2.findViewById(R.id.spinner_situation);
                backImage = (ImageView) myDialog2.findViewById(R.id.back_arrow);
                descText = myDialog2.findViewById(R.id.descrption_words);
                patientName = myDialog2.findViewById(R.id.patient_text);
                phoneNum = myDialog2.findViewById(R.id.phone_num);

                otherImageBtn = myDialog2.findViewById(R.id.other_image_btn);
                otherImageUrl = myDialog2.findViewById(R.id.otherImageUrl);


                //final String description = descText.getText().toString();
                //final String patient = patientName.getText().toString();


                ArrayAdapter<CharSequence> incidentAdapter = ArrayAdapter.createFromResource(myDialog2.getContext(), R.array.incident, android.R.layout.simple_spinner_item);
                incidentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                incidentPopupSpinner.setAdapter(incidentAdapter);
                incidentPopupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                        // your code here
                        Spinner spin = (Spinner) parentView;
                        if (spin.getId() == R.id.spinner_situation) {
                            String txt = parentView.getItemAtPosition(position).toString();
                            incident_type2 = txt;

                            Toast.makeText(myDialog.getContext(), "Incident selected is" + incident_type2, Toast.LENGTH_SHORT).show();


                        }

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parentView) {
                        // your code here
                        Toast.makeText(myDialog.getContext(), "Please specify Incident", Toast.LENGTH_SHORT).show();
                    }

                });

                backImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        myDialog2.dismiss();
                    }
                });

                otherImageBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        BringImagePicker();
                    }
                });
                buttonSend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {



                        //updateDatabase();

                        final String description = descText.getText().toString();
                        final String patient = patientName.getText().toString();
                        final String phone = phoneNum.getText().toString();

                        Date c = Calendar.getInstance().getTime();
                        Toast.makeText(myDialog2.getContext(), "The current time is:" + c, Toast.LENGTH_SHORT).show();

                        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
                        final String formattedDate = df.format(c);


                        Toast.makeText(myDialog.getContext(), "Patient details are" + patient + description, Toast.LENGTH_SHORT).show();


                        final GeoPoint geopoint = new GeoPoint(lat, lng);

                        geocoder = new Geocoder(myDialog2.getContext(), Locale.getDefault());

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

                        if(TextUtils.isEmpty(description)){

                            descText.setError("Please input the description");
                        }else if(TextUtils.isEmpty(patient)){

                            patientName.setError("Please indicate the patients name");
                        }else if(TextUtils.isEmpty(phone)){

                            phoneNum.setError("A phone number is required to be contacted");
                        }else{

                            if (isChanged) {
                                ///checks first if the profile image is changed if it is then proceeds

                                progressDialog.setMessage("Sending emergency Details...");
                                progressDialog.show();


                                StorageReference image_path = storageReference.child("Request_Pictures").child(user_id + ".jpg");

                                image_path.putFile(mainImageURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                        if (task.isSuccessful()) {

                                            //****call on method that will store the user data on the firestore database****

                                            storeFirestore2(task,patient,phone,description,geopoint,userCity,formattedDate,incident_type2);


                                        } else {

                                            String error = task.getException().getMessage();
                                            progressDialog.dismiss();

                                            Toast.makeText(AmbulanceRequests.this, "(IMAGE ERROR):" + error, Toast.LENGTH_LONG).show();

                                            finish();

                                        }



                                    }
                                });

                            }else{

                                storeFirestore2(null,patient,phone,description,geopoint,userCity,formattedDate,incident_type2);
                                Toast.makeText(AmbulanceRequests.this, "FIRESTORE ERROR 1:", Toast.LENGTH_LONG).show();



                            }


                        }



                    }
                });


                myDialog2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                myDialog2.show();


            }
        });


    }

    private void storeFirestore2(Task<UploadTask.TaskSnapshot> task, final String patient, final String phone, final String description, final GeoPoint geopoint, final String userCity, final String formattedDate, final String incident_type2) {

        if (task!=null) {

            storageReference.child("Request_Pictures").child(user_id + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {



                    Map<String, Object> userMap = new HashMap<>();

                    userMap.put("name", patient);
                    userMap.put("phone_number", phone);
                    userMap.put("email", "n/a");
                    userMap.put("location", geopoint);
                    userMap.put("incident", incident_type2);
                    userMap.put("description", description);
                    userMap.put("user_id", user_id);
                    userMap.put("county", userCity);
                    userMap.put("time", formattedDate);
                    userMap.put("image" , uri.toString());
                    userMap.put("condition", "reported");
                    userMap.put("status", "new");
                    userMap.put("timestamp" , FieldValue.serverTimestamp());


                    firebaseFirestore.collection("Ambulance_Requests").document()
                            .set(userMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(AmbulanceRequests.this, "Ambulance request sent", Toast.LENGTH_LONG).show();

                                    descText.setText("");
                                    progressDialog.dismiss();

                                    sendNotificationElse(user_id, description, patient);

                                    updateRequestCount();

                                    updateCountyCount(userCity);

                                    myDialog2.dismiss();

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    Toast.makeText(AmbulanceRequests.this, "FIRESTORE ERROR: COULD NOT SEND", Toast.LENGTH_LONG).show();

                                    finish();

                                }


                            });

                }
            });


        }else{


            storageReference.child("Request_Pictures").child(user_id + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {

                    uri = mainImageURI;



                    Map<String, Object> userMap = new HashMap<>();

                    userMap.put("name", patient);
                    userMap.put("phone_number", phone);
                    userMap.put("email", "n/a");
                    userMap.put("location", geopoint);
                    userMap.put("incident", incident_type2);
                    userMap.put("description", description);
                    userMap.put("user_id", user_id);
                    userMap.put("county", userCity);
                    userMap.put("time", formattedDate);
                    userMap.put("image" , uri.toString());
                    userMap.put("condition", "reported");
                    userMap.put("status", "new");
                    userMap.put("timestamp" , FieldValue.serverTimestamp());


                    firebaseFirestore.collection("Ambulance_Requests").document()
                            .set(userMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(AmbulanceRequests.this, "Ambulance request sent", Toast.LENGTH_LONG).show();

                                    descText.setText("");
                                    progressDialog.dismiss();

                                    sendNotificationElse(user_id, description, patient);

                                    updateRequestCount();

                                    updateCountyCount(userCity);

                                    myDialog2.dismiss();

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    Toast.makeText(AmbulanceRequests.this, "FIRESTORE ERROR: COULD NOT SEND", Toast.LENGTH_LONG).show();

                                    finish();

                                }


                            });

                }
            });







        }


    }

    private void updateRequestCount() {

//get todays month
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat month_date = new SimpleDateFormat("MMMM");
        String month_name = month_date.format(cal.getTime());

        DocumentReference monthRef = firebaseFirestore.collection("Dispatch_Counts").document("Requests");

// Atomically increment the population of the city by 50.
        monthRef.update(month_name, FieldValue.increment(1));


    }

    void startService() {
        LocationBroadcastReceiver receiver = new LocationBroadcastReceiver();
        IntentFilter filter = new IntentFilter("ACTION_LOC");
        registerReceiver(receiver, filter);

        Intent intent = new Intent(AmbulanceRequests.this, LocationService.class);
        startService(intent);
    }

    public class LocationBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("ACTION_LOC")) {

                lat = intent.getDoubleExtra("latitude", 0f);
                lng = intent.getDoubleExtra("longitude", 0f);

                longitude = Double.toString(lng);
                latitude = Double.toString(lat);

                Toast.makeText(AmbulanceRequests.this, "Help! Location:.\nLatitude:" + latitude + "\nLongitude:" + longitude, Toast.LENGTH_SHORT).show();


            }

        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {

        Spinner spin = (Spinner) parent;
        if (spin.getId() == R.id.spinner_incident) {
            String txt = parent.getItemAtPosition(position).toString();
            incident_type = txt;

        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

        Toast.makeText(AmbulanceRequests.this, "Please select blood group", Toast.LENGTH_SHORT).show();


    }

    public void sendToDatabase() {

        progressDialog.setMessage("Loading details...");
        progressDialog.show();


        final GeoPoint geopoint = new GeoPoint(lat, lng);

        geocoder = new Geocoder(this, Locale.getDefault());

        Date c = Calendar.getInstance().getTime();
        Toast.makeText(this, "The current time is:" + c, Toast.LENGTH_SHORT).show();

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
                        final String name2 = task.getResult().getString("name");
                        final String phn_num = task.getResult().getString("phone");
                        final String email = task.getResult().getString("email");

                        final String descriptionTxt = descriptionText.getText().toString();

                        if (isChanged) {        ///checks first if the profile image is changed if it is then proceeds


                            StorageReference image_path = storageReference.child("Request_Pictures").child(user_id + ".jpg");

                            image_path.putFile(mainImageURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                    if (task.isSuccessful()) {

                                        //****call on method that will store the user data on the firestore database****

                                        storeFirestore(task,name2,phn_num,email,descriptionTxt,geopoint,userCity,formattedDate,incident_type);


                                    } else {

                                        String error = task.getException().getMessage();
                                        progressDialog.dismiss();

                                        Toast.makeText(AmbulanceRequests.this, "(IMAGE ERROR):" + error, Toast.LENGTH_LONG).show();

                                        finish();

                                    }


                                }
                            });


                        }else{

                            storeFirestore(null,name2,phn_num,email,descriptionTxt,geopoint,userCity,formattedDate,incident_type);
                            Toast.makeText(AmbulanceRequests.this, "FIRESTORE ERROR 1:", Toast.LENGTH_LONG).show();




                        }




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

    private void storeFirestore(Task<UploadTask.TaskSnapshot> task, final String name2, final String phn_num, final String email, final String descriptionTxt, final GeoPoint geopoint, final String userCity, final String formattedDate, final String incident_type) {



        if (task!=null) {

            storageReference.child("Request_Pictures").child(user_id+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {



                    Map<String, Object> userMap = new HashMap<>();

                    userMap.put("name", name2);
                    userMap.put("phone_number", phn_num);
                    userMap.put("email", email);
                    userMap.put("location", geopoint);
                    userMap.put("incident", incident_type);
                    userMap.put("description", descriptionTxt);
                    userMap.put("user_id", user_id);
                    userMap.put("county", userCity);
                    userMap.put("time", formattedDate);
                    userMap.put("condition", "personal");
                    userMap.put("status", "new");
                    userMap.put("image" , uri.toString());
                    userMap.put("timestamp" , FieldValue.serverTimestamp());







                    firebaseFirestore.collection("Ambulance_Requests").document()
                            .set(userMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(AmbulanceRequests.this, "Ambulance request sent", Toast.LENGTH_LONG).show();


                                    sendNotification(user_id, descriptionTxt, name2);
                                    updateRequestCount();
                                    updateCountyCount(userCity);

                                    descriptionText.setText("");
                                    imageUrl.setText("");
                                    progressDialog.dismiss();

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    Toast.makeText(AmbulanceRequests.this, "FIRESTORE ERROR: COULD NOT SEND", Toast.LENGTH_LONG).show();

                                    finish();

                                }


                            });

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast.makeText(AmbulanceRequests.this, "FIRESTORE ERROR: COULD NOT SEND"+e.getMessage(), Toast.LENGTH_LONG).show();


                }
            });

        }else{


            storageReference.child("Ambulance_Requests").child(user_id+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {

                    uri=mainImageURI;

                    Map<String, Object> userMap = new HashMap<>();

                    userMap.put("name", name2);
                    userMap.put("phone_number", phn_num);
                    userMap.put("email", email);
                    userMap.put("location", geopoint);
                    userMap.put("incident", incident_type);
                    userMap.put("description", descriptionTxt);
                    userMap.put("user_id", user_id);
                    userMap.put("county", userCity);
                    userMap.put("time", formattedDate);
                    userMap.put("condition", "personal");
                    userMap.put("status", "new");
                    userMap.put("image" , uri.toString());
                    userMap.put("timestamp" , FieldValue.serverTimestamp());







                    firebaseFirestore.collection("Ambulance_Requests").document()
                            .set(userMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(AmbulanceRequests.this, "Ambulance request sent", Toast.LENGTH_LONG).show();


                                    sendNotification(user_id, descriptionTxt, name2);
                                    updateRequestCount();
                                    updateCountyCount(userCity);

                                    descriptionText.setText("");
                                    imageUrl.setText("");

                                    progressDialog.dismiss();

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    Toast.makeText(AmbulanceRequests.this, "FIRESTORE ERROR: COULD NOT SEND", Toast.LENGTH_LONG).show();

                                    finish();

                                }


                            });




                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast.makeText(AmbulanceRequests.this, "FIRESTORE ERROR at else: COULD NOT SEND"+e.getMessage(), Toast.LENGTH_LONG).show();


                }
            });






        }



    }

    private void updateCountyCount(final String userCity) {


        DocumentReference docRef = firebaseFirestore.collection("County_Request_Dispatch").document(userCity);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());

                        DocumentReference countyRef = firebaseFirestore.collection("County_Request_Dispatch").document(userCity);

// Atomically increment the county count  by 1.
                        countyRef.update("count", FieldValue.increment(1));



                    } else {
                        Log.d(TAG, "No such document");

                        Map<String ,Object> countMap = new HashMap<>();
                        countMap.put("count" , 1);

                        firebaseFirestore.collection("County_Request_Dispatch").document(userCity)
                                .set(countMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                Toast.makeText(AmbulanceRequests.this , "County count updated" , Toast.LENGTH_SHORT).show();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Toast.makeText(AmbulanceRequests.this , "Error on count updated"+e.getMessage() , Toast.LENGTH_SHORT).show();

                                Log.d(TAG, "Error on count updated " + e.getMessage());

                            }
                        });
                    }
                } else {
                    Log.d(TAG, "get failed at county count with ", task.getException());
                }
            }
        });


    }

    private void sendNotification(String user_id, String descriptionTxt, String name2) {


        String message2 = "Ambulance Request From " + name2 + "\n" + descriptionTxt;
        Map<String, Object> adminNotification = new HashMap<>();
        adminNotification.put("from", user_id);
        adminNotification.put("description", message2);
        adminNotification.put("status", "received");
        adminNotification.put("condition", "new");
        adminNotification.put("timestamp", FieldValue.serverTimestamp());

        firebaseFirestore.collection("Dispatcher_Notification").document().set(adminNotification)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Toast.makeText(AmbulanceRequests.this, "Admin Notified", Toast.LENGTH_SHORT).show();


                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(AmbulanceRequests.this, "Failed to notified admin" + e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });


    }

    private void sendNotificationElse(String user_id, String descriptionTxt, String name2) {


        String message2 = "Ambulance Request For " + name2 + "\n" + descriptionTxt;
        Map<String, Object> adminNotification = new HashMap<>();
        adminNotification.put("from", user_id);
        adminNotification.put("description", message2);
        adminNotification.put("status", "received");
        adminNotification.put("condition", "new");
        adminNotification.put("timestamp", FieldValue.serverTimestamp());

        firebaseFirestore.collection("Dispatcher_Notification").document().set(adminNotification)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Toast.makeText(AmbulanceRequests.this, "Admin Notified", Toast.LENGTH_SHORT).show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(AmbulanceRequests.this, "Failed to notified admin" + e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });


    }

    public void checkForSuspension() {

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
                        buttonOkay = (Button) myDialog.findViewById(R.id.okay_btn);


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

    private void BringImagePicker() {

        // start picker to get image for cropping and then use the image in cropping activity
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == Activity.RESULT_OK) {

//****replace image with new image******
                mainImageURI = result.getUri();
                otherImageUri = result.getUri();

                imageUrl.setText(mainImageURI.toString());

                otherImageUrl.setText(otherImageUri.toString());



                isChanged=true;


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error= result.getError();

            }
        }


    }


}





