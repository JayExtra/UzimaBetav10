package com.example.uzimabetav10.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.uzimabetav10.R;
import com.example.uzimabetav10.utils.DatePickerFragment;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class EditProfile extends AppCompatActivity  implements DatePickerDialog.OnDateSetListener, AdapterView.OnItemSelectedListener{

    private Button edit_picture, updateDetails, selectDate;
    private Uri mainImageURI=null;
    private ImageView setupImage;
    private EditText nameField,emailField,numberField,dateField,ageField;
    public boolean isChanged = false;
    private StorageReference storageReference;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private String user_id, genderSelected,countySelected;
    private ProgressDialog progressDialog;
    private Spinner genderSpinner,countySpinner;
    private EditText emergencyContact,locationText;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        //instantiate firebase
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();





        Toolbar toolbar = findViewById(R.id.single_post_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(" Edit your Profile");


        user_id =mAuth.getCurrentUser().getUid();

        //map widgets
        edit_picture = findViewById(R.id.button_picture);
        setupImage =findViewById(R.id.image_holder);
        updateDetails =findViewById(R.id.button_update);
        nameField=findViewById(R.id.name_fld);
        emailField=findViewById(R.id.email_fld);
        numberField=findViewById(R.id.numbr_fld);
        dateField=findViewById(R.id.date_field);
        ageField=findViewById(R.id.age_text);
        locationText=findViewById(R.id.location_txt);
        selectDate=findViewById(R.id.date_button);
        genderSpinner=findViewById(R.id.spinner_gender);
        countySpinner=findViewById(R.id.county_spinner);
        emergencyContact=findViewById(R.id.emergency_contact);



        progressDialog=new ProgressDialog(this);


        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EditProfile.this, Profile.class));
                finish();
            }
        });

        fetchFromDatabase();

        //implement the spinner adapters

        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(this,R.array.gender,android.R.layout.simple_spinner_item);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter( genderAdapter);
        genderSpinner.setOnItemSelectedListener(this);

        //implement the spinner adapters

        ArrayAdapter<CharSequence> countyAdapter = ArrayAdapter.createFromResource(this,R.array.County,android.R.layout.simple_spinner_item);
        countyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        countySpinner.setAdapter(  countyAdapter);
        countySpinner.setOnItemSelectedListener(this);



            //handles the change profile photo button
        edit_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    if (ContextCompat.checkSelfPermission(EditProfile.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                        Toast.makeText(EditProfile.this, "You do not have permission", Toast.LENGTH_SHORT).show();
                        ActivityCompat.requestPermissions(EditProfile.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

                    } else {

                        BringImagePicker();

                    }
                }else{
                    BringImagePicker();
                }


            }
        });

        updateDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendToDatabase();
            }
        });

        //date button
        selectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DatePickerFragment datePickerFragment = new DatePickerFragment();
                datePickerFragment.show(getSupportFragmentManager(),"date picker");

            }
        });


    }

    //writing values into database
    private void SendToDatabase() {

        final String name= nameField.getText().toString();
        final String email = emailField.getText().toString();
        final String phone= numberField.getText().toString();
        final String dOb= dateField.getText().toString();
        final String age= ageField.getText().toString();
        final String em_contact = emergencyContact.getText().toString();
        final String location = locationText.getText().toString();
        //final String status = "online";



        if(!TextUtils.isEmpty(name)&&!TextUtils.isEmpty(email)&&!TextUtils.isEmpty(phone)&&mainImageURI!=null
                && !TextUtils.isEmpty(dOb) && !TextUtils.isEmpty(age) && !TextUtils.isEmpty(em_contact)&&!TextUtils.isEmpty(location)){


            if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){

                emailField.setError("Please input correct email");

            }else{

                progressDialog.setMessage("Uploading details...");
                progressDialog.show();

                if (isChanged) {        ///checks first if the profile image is changed if it is then proceeds


//***sending image to fire base storage together with user details*********

                    user_id = mAuth.getCurrentUser().getUid();



                    StorageReference image_path = storageReference.child("profile_pictures").child(user_id + ".jpg");

                    image_path.putFile(mainImageURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {


                            if (task.isSuccessful()) {

                                //****call on method that will store the user data on the firestore database****

                                storeFirestore(task,name,email,phone,dOb,age,genderSelected,em_contact,countySelected,location);


                            } else {

                                String error = task.getException().getMessage();

                                Toast.makeText(EditProfile.this, "(IMAGE ERROR):" + error, Toast.LENGTH_LONG).show();

                                finish();

                            }


                        }
                    });


                }else {  //if profile image is not changed pass task as null therefore the user selects image again


                    storeFirestore(null, name,email,phone,dOb,age,genderSelected,em_contact,countySelected,location);
                    Toast.makeText(EditProfile.this, "FIRESTORE ERROR 1:", Toast.LENGTH_LONG).show();

                }




            }





        }else{

            Toast.makeText(EditProfile.this,"Please fill all the required fields. No field should be left empty ",Toast.LENGTH_LONG).show();


        }



    }

    //******adding user details  into firestore database ***********

    private void storeFirestore(@NonNull Task<UploadTask.TaskSnapshot>task, final String name,
                                final String email, final String phone,
                                final String dOb, final String age,final String genderSelected,
                                final String em_contact, final String countySelected,final String location) {

        //****hash map for storing user details in fire base cloud storage**************


        if (task!=null) {


            storageReference.child("profile_pictures").child(user_id+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri download_uri) {


                    Map<String, Object> userMap= new HashMap<>();

                    userMap.put("image",download_uri.toString());
                    userMap.put("name",name);
                    userMap.put("email",email);
                    userMap.put("phone",phone);
                    userMap.put("user_id",user_id);
                    userMap.put("date_of_birth",dOb);
                    userMap.put("user_age",age);
                    userMap.put("gender",genderSelected);
                    userMap.put("county",countySelected);
                    userMap.put("location",location);
                    userMap.put("emergency_contact",em_contact);





                    firebaseFirestore.collection("users").document(user_id)
                            .set(userMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(EditProfile.this,"Profile Updated",Toast.LENGTH_LONG).show();

                                   startActivity(new Intent(EditProfile.this,Profile.class));

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    Toast.makeText(EditProfile.this,"FIRESTORE ERROR: COULD NOT FETCH IMAGE",Toast.LENGTH_LONG).show();

                                   finish();

                                }


                            });

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                    Toast.makeText(EditProfile.this,"FIRESTORE ERROR 2",Toast.LENGTH_LONG).show();

                }
            });


        } else{

            storageReference.child("profile_images").child(user_id+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri download_uri) {
                    // Got the download URL for profile picture

                    download_uri=mainImageURI;

                    Map<String, Object> userMap= new HashMap<>();

                    userMap.put("image",download_uri.toString());
                    userMap.put("name",name);
                    userMap.put("email",email);
                    userMap.put("phone",phone);
                    userMap.put("user_id",user_id);
                    userMap.put("date_of_birth",dOb);
                    userMap.put("user_age",age);
                    userMap.put("gender",genderSelected);
                    userMap.put("county",countySelected);
                    userMap.put("location",location);
                    userMap.put("emergency_contact",em_contact);



                    firebaseFirestore.collection("users").document(user_id)
                            .set(userMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(EditProfile.this,"Profile Updated",Toast.LENGTH_LONG).show();

                                    startActivity(new Intent(EditProfile.this,Profile.class));


                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    Toast.makeText(EditProfile.this,"FIRESTORE ERROR 3",Toast.LENGTH_LONG).show();

                                    finish();

                                }


                            });




                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors

                    Toast.makeText(EditProfile.this,"FIRESTORE ERROR 4 ",Toast.LENGTH_LONG).show();

                }
            });


        }






    }

    private void fetchFromDatabase(){

        updateDetails.setEnabled(false);
        progressDialog.setMessage("Loading details...");
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
                        String name2 = task.getResult().getString("name");
                        String number = task.getResult().getString("phone");
                        String image2= task.getResult().getString("image");
                        String email2= task.getResult().getString("email");
                        String dOb2= task.getResult().getString("date_of_birth");
                        String age2= task.getResult().getString("user_age");
                        String location = task.getResult().getString("location");
                        String gender2= task.getResult().getString("gender");
                        String emergency_contact= task.getResult().getString("emergency_contact");





                        mainImageURI = Uri.parse(image2);



                        emailField.setText(email2);
                        nameField.setText(name2);
                        numberField.setText(number);
                        dateField.setText(dOb2);
                        ageField.setText(age2);
                        locationText.setText(location);
                        emergencyContact.setText(emergency_contact);


                        //******replacing the dummy image with real profile picture******

                        RequestOptions placeholderRequest = new RequestOptions();
                        placeholderRequest.placeholder(R.drawable.user_image);

                        Glide.with(EditProfile.this).setDefaultRequestOptions(placeholderRequest).load(image2).into(setupImage);


//19-10-1996
                    } else {

                        Toast.makeText(EditProfile.this, "DATA DOES NOT EXISTS,PLEASE REGISTER", Toast.LENGTH_LONG).show();



                    }
                } else {

                    String error = task.getException().getMessage();
                    Toast.makeText(EditProfile.this, "(FIRESTORE RETRIEVE ERROR):" + error, Toast.LENGTH_LONG).show();


                }


                updateDetails.setEnabled(true);

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
                setupImage.setImageURI(mainImageURI);

                isChanged=true;


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error= result.getError();

            }
        }


    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int date) {

        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR,year);
        c.set(Calendar.MONTH,month);
        c.set(Calendar.DAY_OF_MONTH,date);

        String currentDateString = DateFormat.getDateInstance(DateFormat.FULL).format(c.getTime());

        dateField.setText(currentDateString);

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
        Spinner spin = (Spinner)parent;
        Spinner spin2 = (Spinner)parent;

        if(spin.getId() == R.id.spinner_gender)
        {
            String txt = parent.getItemAtPosition(position).toString();
            genderSelected =txt;

        }

        if(spin2.getId() == R.id.county_spinner)
        {
            String txt2 = parent.getItemAtPosition(position).toString();
            countySelected =txt2;

        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

        Toast.makeText(EditProfile.this,"Please select your gender and county you are from",Toast.LENGTH_SHORT).show();


    }



    public void increaseByDate(){



    }


}
