package com.example.uzimabetav10;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Profile extends AppCompatActivity {
    private Button buttonEdit;
    private TextView nameTxt,emailTxt,numberTxt,useridTxt,genderTxt,dobTxt,countyTxt,locationTxt,ageTxt;
    private ImageView setupImage2;
    private FirebaseAuth firebaseAuth;
    private Uri mainImageURI=null;
    private String user_id;
    private ProgressBar progressBarProfile;
    private FirebaseFirestore firebaseFirestore;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.single_post_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Profile");

        FirebaseApp.initializeApp(Profile.this);
        firebaseAuth = FirebaseAuth.getInstance();
        user_id = firebaseAuth.getCurrentUser().getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();

        progressDialog=new ProgressDialog(this);




        //map widgets
        buttonEdit=findViewById(R.id.edit_profile_btn);
        nameTxt=findViewById(R.id.name_txt);
        emailTxt=findViewById(R.id.email_txt);
        numberTxt=findViewById(R.id.number_txt);
        useridTxt=findViewById(R.id.user_id_txt);
        setupImage2=findViewById(R.id.image_user);
        genderTxt=findViewById(R.id.gender_txt);
        dobTxt=findViewById(R.id.dob_txt);
        countyTxt=findViewById(R.id.county_txt);
        locationTxt=findViewById(R.id.location_txt);
        ageTxt=findViewById(R.id.age_txt);





        retrieveProfile();



        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Profile.this, EditProfile.class));
            }
        });

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Profile.this, MainActivity.class));
            }
        });


    }

    public void retrieveProfile(){

        //retrieve user date from the database

        progressDialog.setMessage("Loading profile...");
        progressDialog.show();

        DocumentReference docRef = firebaseFirestore.collection("users").document(user_id);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {

                    progressDialog.dismiss();

                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String name2 = task.getResult().getString("name");
                        String number = task.getResult().getString("phone");
                        String user_id = task.getResult().getString("user_id");
                        String email2 = task.getResult().getString("email");
                        String image2= task.getResult().getString("image");
                        String age= task.getResult().getString("user_age");
                        String dob= task.getResult().getString("date_of_birth");
                        String gender= task.getResult().getString("gender");
                        String county= task.getResult().getString("county");
                        String location= task.getResult().getString("location");


                        mainImageURI = Uri.parse(image2);



                        emailTxt.setText(email2);
                        nameTxt.setText(name2);
                        numberTxt.setText(number);
                        useridTxt.setText(user_id);
                        ageTxt.setText(age);
                        dobTxt.setText(dob);
                        genderTxt.setText(gender);
                        countyTxt.setText(county);
                        locationTxt.setText(location);




                        //******replacing the dummy image with real profile picture******
                        RequestOptions placeholderRequest = new RequestOptions();
                        placeholderRequest.placeholder(R.drawable.user_image);

                        Glide.with(Profile.this).setDefaultRequestOptions(placeholderRequest).load(image2).into(setupImage2);





                    } else {


                        Toast.makeText(Profile.this, "DATA DOES NOT EXISTS,PLEASE REGISTER", Toast.LENGTH_LONG).show();


                    }
                } else {


                    String error = task.getException().getMessage();
                    Toast.makeText(Profile.this, "(FIRESTORE RETRIEVE ERROR):" + error, Toast.LENGTH_LONG).show();


                }


            }
        });





    }


}
