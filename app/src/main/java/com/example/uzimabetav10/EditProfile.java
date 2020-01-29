package com.example.uzimabetav10;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

public class EditProfile extends AppCompatActivity {

    private Button edit_picture, updateDetails;
    private Uri mainImageURI=null;
    private ImageView setupImage;
    private EditText nameField,emailField,numberField;
    public boolean isChanged = false;
    private StorageReference storageReference;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private String user_id;
    private ProgressDialog progressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        //instantiate firebase
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();



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

        progressDialog=new ProgressDialog(this);


        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EditProfile.this, Profile.class));
            }
        });

        fetchFromDatabase();

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


    }

    //writing values into database
    private void SendToDatabase() {

        final String name= nameField.getText().toString();
        final String email = emailField.getText().toString();
        final String phone= numberField.getText().toString();

        if(!TextUtils.isEmpty(name)&&!TextUtils.isEmpty(email)&&!TextUtils.isEmpty(phone)&&mainImageURI!=null){

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

                            storeFirestore(task,name,email,phone);


                        } else {

                            String error = task.getException().getMessage();

                            Toast.makeText(EditProfile.this, "(IMAGE ERROR):" + error, Toast.LENGTH_LONG).show();

                           finish();

                        }


                    }
                });


            }else {  //if profile image is not changed pass task as null therefore the user selects image again


                storeFirestore(null, name,email,phone);
                Toast.makeText(EditProfile.this, "FIRESTORE ERROR 1:", Toast.LENGTH_LONG).show();

            }


        }

    }

    //******adding user details  into firestore database ***********

    private void storeFirestore(@NonNull Task<UploadTask.TaskSnapshot>task, final String name, final String email, final String phone) {

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




                        mainImageURI = Uri.parse(image2);



                        emailField.setText(email2);
                        nameField.setText(name2);
                        numberField.setText(number);


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
}
