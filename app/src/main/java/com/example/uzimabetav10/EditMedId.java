package com.example.uzimabetav10;

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
import com.example.uzimabetav10.utils.DatePickerFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
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

public class EditMedId extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, AdapterView.OnItemSelectedListener {

    private FloatingActionButton floatingEditID;
    private Button dateButton;
    private Spinner ageSpinner,bloodSpinner;
    private String bloodGrp, ageTxt;
    private EditText med_name,med_occup,med_city,med_allerg,med_hist,med_dob, med_cont1,med_cont2;
    private ImageView editIdImage;
    private StorageReference storageReference;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private String user_id;

    private ProgressDialog progressDialog;

    private Uri mainImageURI=null;
    public boolean isChanged = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_med_id);

        //firebase setup
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        user_id = mAuth.getCurrentUser().getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();


        progressDialog=new ProgressDialog(this);

        //toolbar setup
        Toolbar toolbar = findViewById(R.id.edit_medicalId_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Edit your medical id");

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EditMedId.this, MainActivity.class));
            }
        });


        //setup widgets
        dateButton = findViewById(R.id.button_date);
        ageSpinner = findViewById(R.id.age_spinner);
        bloodSpinner = findViewById(R.id.bloodgroup_spinner);
        med_name = findViewById(R.id.id_name);
        med_occup = findViewById(R.id.id_occup);
        med_city = findViewById(R.id.id_city);
        med_allerg = findViewById(R.id.id_allergies);
        med_hist = findViewById(R.id.id_hist);
        floatingEditID = findViewById(R.id.id_float_send);
        editIdImage = findViewById(R.id.id_image);
        med_dob = findViewById(R.id.id_dob);
        med_cont1 = findViewById(R.id.em_cont1);
        med_cont2 = findViewById(R.id.em_cont2);





        //implement the spinner adapters

        ArrayAdapter<CharSequence> ageAdapter = ArrayAdapter.createFromResource(this,R.array.age,android.R.layout.simple_spinner_item);
        ageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ageSpinner.setAdapter(ageAdapter);
        ageSpinner.setOnItemSelectedListener(this);

        ArrayAdapter<CharSequence> bloodAdapter = ArrayAdapter.createFromResource(this,R.array.blood,android.R.layout.simple_spinner_item);
        ageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bloodSpinner.setAdapter(bloodAdapter);
        bloodSpinner.setOnItemSelectedListener(this);


        //do a quick check to see if the user has a medical
        fetchFromDatabase();







        //sending button
        floatingEditID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendToIdDatabase();
            }
        });

        //image button to bring image picker

        editIdImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    if (ContextCompat.checkSelfPermission(EditMedId.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                        Toast.makeText(EditMedId.this, "You do not have permission", Toast.LENGTH_SHORT).show();
                        ActivityCompat.requestPermissions(EditMedId.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

                    } else {

                        BringImagePicker();

                    }
                }else{
                    BringImagePicker();
                }
            }
        });



        //date button
        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DatePickerFragment datePickerFragment = new DatePickerFragment();
                datePickerFragment.show(getSupportFragmentManager(),"date picker");

            }
        });


    }


    //responsible for setting the date
    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int date) {

        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR,year);
        c.set(Calendar.MONTH,month);
        c.set(Calendar.DAY_OF_MONTH,date);

        String currentDateString = DateFormat.getDateInstance(DateFormat.FULL).format(c.getTime());

        EditText dateText = (EditText) findViewById(R.id.id_dob);

        dateText.setText(currentDateString);




    }



    public void sendToIdDatabase(){


        final String name= med_name.getText().toString();
        final String occup = med_occup.getText().toString();
        final String allergies= med_allerg.getText().toString();
        final String hist= med_hist.getText().toString();
        final String city = med_city.getText().toString();
        final String dob= med_dob.getText().toString();
        final String cont1 = med_cont1.getText().toString();
        final String cont2 = med_cont2.getText().toString();

        if(!TextUtils.isEmpty(name)&&!TextUtils.isEmpty(occup)&&!TextUtils.isEmpty(allergies)&&mainImageURI!=null &&!TextUtils.isEmpty(hist)&&!TextUtils.isEmpty(city)
                &&!TextUtils.isEmpty(dob)){

            progressDialog.setMessage("Uploading details...");
            progressDialog.show();

            if (isChanged) {        ///checks first if the profile image is changed if it is then proceeds


//***sending image to fire base storage together with user details*********


                user_id = mAuth.getCurrentUser().getUid();


                StorageReference image_path = storageReference.child("medId_pictures").child(user_id + ".jpg");

                image_path.putFile(mainImageURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {


                        if (task.isSuccessful()) {

                            //****call on method that will store the user data on the firestore database****

                            storeFirestore(task,name,occup,allergies,hist,city,dob,bloodGrp,ageTxt,cont1,cont2);


                        } else {

                            String error = task.getException().getMessage();

                            Toast.makeText(EditMedId.this, "(IMAGE ERROR):" + error, Toast.LENGTH_LONG).show();

                            finish();

                        }


                    }
                });


            }else {  //if profile image is not changed pass task as null therefore the user selects image again


                storeFirestore(null, name,occup,allergies,hist,city,dob,bloodGrp,ageTxt,cont1,cont2);
                Toast.makeText(EditMedId.this, "FIRESTORE ERROR 1:", Toast.LENGTH_LONG).show();

            }


        }


    }


    //handling the selected items within the spinners
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {

        //String txt = parent.getItemAtPosition(position).toString();
        //Toast.makeText(EditMedId.this,"Item selected is :" + txt,Toast.LENGTH_SHORT).show();

        Spinner spin = (Spinner)parent;
        Spinner spin2 = (Spinner)parent;
        if(spin.getId() == R.id.bloodgroup_spinner)
        {
            String txt = parent.getItemAtPosition(position).toString();
            bloodGrp =txt;

        }
        if(spin2.getId() == R.id.age_spinner)
        {
            String txt = parent.getItemAtPosition(position).toString();
            ageTxt = txt;

        }



    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

        Toast.makeText(EditMedId.this,"Please select blood group",Toast.LENGTH_SHORT).show();

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
                editIdImage.setImageURI(mainImageURI);

                isChanged=true;


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error= result.getError();

                Toast.makeText(EditMedId.this,""+error,Toast.LENGTH_SHORT).show();

            }
        }


    }

    //******adding user details  into firestore database ***********

    private void storeFirestore(@NonNull Task<UploadTask.TaskSnapshot>task, final String name, final String occup, final String allergies,
    final String hist, final String city, final String dob,final String bloodGrp,final String ageTxt, final String conta1,final String conta2) {

        //****hash map for storing user details in fire base cloud storage**************


        if (task!=null) {


            storageReference.child("medId_pictures").child(user_id+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri download_uri) {


                    Map<String, Object> userMap= new HashMap<>();

                    userMap.put("image",download_uri.toString());
                    userMap.put("name",name);
                    userMap.put("occupation",occup);
                    userMap.put("allergies",allergies);
                    userMap.put("historical_illness",hist);
                    userMap.put("city",city);
                    userMap.put("D_o_B",dob);
                    userMap.put("blood_group",bloodGrp);
                    userMap.put("age",ageTxt);
                    userMap.put("user_id",user_id);
                    userMap.put("emergency_contact1",conta1);
                    userMap.put("emergency_contact2",conta2);




                    firebaseFirestore.collection("User_Med_IDs").document(user_id)
                            .set(userMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(EditMedId.this,"Medical Id Updated",Toast.LENGTH_LONG).show();

                                    startActivity(new Intent(EditMedId.this,MedicalID.class));

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    Toast.makeText(EditMedId.this,"FIRESTORE ERROR: COULD NOT FETCH IMAGE",Toast.LENGTH_LONG).show();

                                    finish();

                                }


                            });

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                    Toast.makeText(EditMedId.this,"FIRESTORE ERROR 2",Toast.LENGTH_LONG).show();

                }
            });


        } else{

            storageReference.child("medId_pictures").child(user_id+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri download_uri) {
                    // Got the download URL for profile picture

                    download_uri=mainImageURI;

                    Map<String, Object> userMap= new HashMap<>();

                    userMap.put("image",download_uri.toString());
                    userMap.put("name",name);
                    userMap.put("occupation",occup);
                    userMap.put("allergies",allergies);
                    userMap.put("historical_illness",hist);
                    userMap.put("city",city);
                    userMap.put("D_o_B",dob);
                    userMap.put("blood_group",bloodGrp);
                    userMap.put("age",ageTxt);
                    userMap.put("user_id",user_id);
                    userMap.put("emergency_contact1",conta1);
                    userMap.put("emergency_contact2",conta2);


                    firebaseFirestore.collection("User_Med_IDs").document(user_id)
                            .set(userMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(EditMedId.this,"Medical id  Updated",Toast.LENGTH_LONG).show();

                                    startActivity(new Intent(EditMedId.this,MedicalID.class));


                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    Toast.makeText(EditMedId.this,"FIRESTORE ERROR 3",Toast.LENGTH_LONG).show();

                                    finish();

                                }


                            });




                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors

                    Toast.makeText(EditMedId.this,"FIRESTORE ERROR 4 ",Toast.LENGTH_LONG).show();

                }
            });


        }






    }

    private void fetchFromDatabase(){

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
                        String contact1 = task.getResult().getString("emergency_contact1");
                        String contact2 = task.getResult().getString("emergency_contact2");




                        mainImageURI = Uri.parse(image2);



                        med_occup.setText(occup2);
                        med_allerg.setText(allerg2);
                        med_hist.setText(hist2);
                        med_name.setText(name2);
                        med_city.setText(city2);
                        med_dob.setText(dob2);
                        med_cont1.setText(contact1);
                        med_cont2.setText(contact2);


                        //******replacing the dummy image with real profile picture******

                        RequestOptions placeholderRequest = new RequestOptions();
                        placeholderRequest.placeholder(R.drawable.user_image);

                        Glide.with(EditMedId.this).setDefaultRequestOptions(placeholderRequest).load(image2).into(editIdImage);


//19-10-1996
                    } else {

                        Toast.makeText(EditMedId.this, "DATA DOES NOT EXISTS,PLEASE CREATE YOUR MEDICAL ID", Toast.LENGTH_LONG).show();



                    }
                } else {

                    String error = task.getException().getMessage();
                    Toast.makeText(EditMedId.this, "(FIRESTORE RETRIEVE ERROR):" + error, Toast.LENGTH_LONG).show();


                }



            }
        });


    }


}
