package com.example.uzimabetav10;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;

import java.util.HashMap;
import java.util.Map;

public class AmbulanceRequests extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private TextView numberText, descriptionText;
    private Spinner incidentSpinner;
    private Button sendButton, buttonCancel,buttonYes;
    String incident_type;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private String user_id;
    String lat , lng;
    Dialog myDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ambulance_requests);

        //firebase setup
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        user_id = mAuth.getCurrentUser().getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();

        //toolbar setup
        Toolbar toolbar = findViewById(R.id.single_post_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Ambulance Services");

        myDialog=new Dialog(this);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AmbulanceRequests.this, MainActivity.class));
            }
        });

        progressDialog=new ProgressDialog(this);


        //map widgets
        descriptionText = findViewById(R.id.description_text);
        incidentSpinner = findViewById(R.id.spinner_incident);
        sendButton = findViewById(R.id.send_button);

        //setupp spinner adapter

        ArrayAdapter<CharSequence> incidentAdapter = ArrayAdapter.createFromResource(this,R.array.incident,android.R.layout.simple_spinner_item);
        incidentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        incidentSpinner.setAdapter(incidentAdapter);
        incidentSpinner.setOnItemSelectedListener(this);



        //retrieve user coordinates

        Intent retrieveIntent= getIntent();
        lat= retrieveIntent.getStringExtra("LATITUDE");
        lng= retrieveIntent.getStringExtra("LONGITUDE");


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



        final double lat2=Double.parseDouble(lat);
        final double log2=Double.parseDouble(lng);

        final GeoPoint geopoint = new GeoPoint(lat2,log2);

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
                        userMap.put("time", FieldValue.serverTimestamp());


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
}
