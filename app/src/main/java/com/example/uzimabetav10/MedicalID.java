package com.example.uzimabetav10;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class MedicalID extends AppCompatActivity {

    private FloatingActionButton floatEdit;
    private TextView med_name,med_occup,med_city,med_allerg,med_hist,med_dob,med_age,med_bg,med_contact1,med_contact2;
    private ImageView editIdImage;
    private StorageReference storageReference;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private String user_id;
    private ProgressDialog progressDialog;
    private String bloodGrp, ageTxt;
    private Uri mainImageURI=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_id);

        //firebase setup
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        user_id = mAuth.getCurrentUser().getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        floatEdit= findViewById(R.id.floating_edit);
        progressDialog=new ProgressDialog(this);

        //widget setup
        med_name = findViewById(R.id.id_name);
        med_age = findViewById(R.id.id_age);
        med_dob = findViewById(R.id.txt_DoB);
        med_occup = findViewById(R.id.id_occup);
        med_city = findViewById(R.id.id_city);
        med_allerg = findViewById(R.id.id_allergies);
        med_hist = findViewById(R.id.id_hist);
        med_bg = findViewById(R.id.txt_blood);
        editIdImage = findViewById(R.id.id_image);
        med_contact1 = findViewById(R.id.em_cntct1);
        med_contact2 = findViewById(R.id.em_cntct2);

        //do a quick check to see if the user has a medical
        fetchFromDatabase();

        //toolbar setup
        Toolbar toolbar = findViewById(R.id.single_post_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Medical Id");

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MedicalID.this, MainActivity.class));
            }
        });


        floatEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MedicalID.this,EditMedId.class));
            }
        });
    }

    public void fetchFromDatabase(){

        progressDialog.setMessage("Loading details...");
        progressDialog.show();


//******checking on data within the database on whether it already exist then fetch the user details and place them in the required fields************



        DocumentReference docRef = firebaseFirestore.collection("User_Med_IDs").document(user_id);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    progressDialog.dismiss();
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String name2 = task.getResult().getString("name");
                        String occup2 = task.getResult().getString("occupation");
                        String allerg2= task.getResult().getString("allergies");
                        String city2= task.getResult().getString("city");
                        String dob2= task.getResult().getString("D_o_B");
                        String hist2= task.getResult().getString("historical_illness");
                        String image2= task.getResult().getString("image");
                        String bg= task.getResult().getString("blood_group");
                        String age= task.getResult().getString("age");
                        String contact1 = task.getResult().getString("emergency_contact1");
                        String contact2 = task.getResult().getString("emergency_contact2");




                        mainImageURI = Uri.parse(image2);



                        med_occup.setText(occup2);
                        med_allerg.setText(allerg2);
                        med_hist.setText(hist2);
                        med_name.setText(name2);
                        med_city.setText(city2);
                        med_dob.setText(dob2);
                        med_age.setText(age);
                        med_bg.setText(bg);
                        med_contact1.setText(contact1);
                        med_contact2.setText(contact2);



                        //******replacing the dummy image with real profile picture******

                        RequestOptions placeholderRequest = new RequestOptions();
                        placeholderRequest.placeholder(R.drawable.user_image);

                        Glide.with(MedicalID.this).setDefaultRequestOptions(placeholderRequest).load(image2).into(editIdImage);


//19-10-1996
                    } else {

                        Toast.makeText(MedicalID.this, "DATA DOES NOT EXISTS,PLEASE CREATE YOUR MEDICAL ID", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(MedicalID.this,EditMedId.class));



                    }
                } else {

                    String error = task.getException().getMessage();
                    Toast.makeText(MedicalID.this, "(FIRESTORE RETRIEVE ERROR):" + error, Toast.LENGTH_LONG).show();


                }



            }
        });

    }
}
