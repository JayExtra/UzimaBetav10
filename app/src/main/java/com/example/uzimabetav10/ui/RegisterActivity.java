package com.example.uzimabetav10.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.uzimabetav10.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private TextView loginText;
    private Button buttonRegister;
    private EditText emailText, passwrdText;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirebaseFirestore;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        FirebaseApp.initializeApp(this);
        mAuth= FirebaseAuth.getInstance();
        mFirebaseFirestore = FirebaseFirestore.getInstance();

        //finding the different views
        progressDialog=new ProgressDialog(this);
        loginText=findViewById(R.id.login_text);
        buttonRegister=findViewById(R.id.button_register);
        emailText=findViewById(R.id.email_field);
        passwrdText=findViewById(R.id.password_field);

        loginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent launchLogin= new Intent(RegisterActivity.this,LoginActivity.class);
                startActivity(launchLogin);
                finish();
            }
        });

        //registration button
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startRegistration();
            }
        });
    }

    private void startRegistration() {
        //get values in the field first
        String email=emailText.getText().toString();
        String password=passwrdText.getText().toString();



        if(TextUtils.isEmpty(email)){
            emailText.setError("please add your email");
        }else if(TextUtils.isEmpty(password)){
            passwrdText.setError("please insert your password");

        }else{


            //to display progress dialog
            progressDialog.setMessage("Registering user...");
            progressDialog.show();


            //fire base authentication process

            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    progressDialog.dismiss();

                    if(task.isSuccessful()){
                        //user is successfully registered start main domicile activity and log in

                        Toast.makeText(RegisterActivity.this,"Registration Successful",Toast.LENGTH_SHORT).show();

                        String token_id = FirebaseInstanceId.getInstance().getToken();
                        String current_id = mAuth.getCurrentUser().getUid();

                        Map<String, Object> tokenMap = new HashMap<>();
                        tokenMap.put("token_id" , token_id);

                        mFirebaseFirestore.collection("users").document(current_id).update(tokenMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        sendVerificationEmail();

                                        finish();
                                        startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                                        Toast.makeText(RegisterActivity.this,"Registration Successful",Toast.LENGTH_SHORT).show();


                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Toast.makeText(RegisterActivity.this, "Error :.." + e.getMessage(),Toast.LENGTH_SHORT).show();

                            }
                        });

                       /* finish();
                        startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                        finish();*/

                    }else{

                        //when user registration fails
                        String errorMessage=task.getException().getMessage();
                        Toast.makeText(RegisterActivity.this,"Error:"+errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }
            });




        }


    }

    private void sendVerificationEmail() {


        FirebaseUser user = mAuth.getCurrentUser();
        user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                Toast.makeText(RegisterActivity.this,"Verification link Has been sent to your email",Toast.LENGTH_LONG).show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(RegisterActivity.this,"Could not send verification Email:"+e.getMessage(),Toast.LENGTH_LONG).show();

            }
        });



    }
}

