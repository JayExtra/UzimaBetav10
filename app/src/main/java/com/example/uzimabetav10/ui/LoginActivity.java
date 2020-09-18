package com.example.uzimabetav10.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private TextView regText , forgotText;
    private Button buttonLogin , buttonYes , buttonNo;
    private EditText emailText;
    private EditText passwrdText;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirebaseFirestore;

    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //initialize Firebase app and get Firebase instance
        FirebaseApp.initializeApp(this);
        mAuth= FirebaseAuth.getInstance();
        mFirebaseFirestore = FirebaseFirestore.getInstance();

        //check first if current user exists , if does starts main interface
        if(mAuth.getCurrentUser() !=null){

            finish();
            startActivity(new Intent(getApplicationContext(),MainActivity.class));

        }

        //starts the progress dialog
        progressDialog=new ProgressDialog(this);

        //mapping various buttons in the activities
        emailText=findViewById(R.id.email_field);
        passwrdText=findViewById(R.id.password_field);
        regText=findViewById(R.id.register_text);
        buttonLogin=findViewById(R.id.button_login);
        forgotText = findViewById(R.id.forgot_txt);

        //event for going to register activity
        regText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent launchReg= new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(launchReg);
            }
        });

            //starts the logging in process
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                loginUser();
            }
        });

        forgotText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startForgotPassword();
            }
        });
    }

    private void startForgotPassword() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final View submitPopup = getLayoutInflater().inflate(R.layout.reset_password_pop,null);
        final EditText emailReset = (EditText) submitPopup.findViewById(R.id.email_reset);
        buttonYes=(Button) submitPopup.findViewById(R.id.button_send_email);
        buttonYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String userEmail = emailReset.getText().toString();

                if(TextUtils.isEmpty(userEmail)){
                    emailReset.setError("Please provide your email");
                }else{

                    mAuth.sendPasswordResetEmail(userEmail).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            dialog.dismiss();

                            Toast.makeText(LoginActivity.this ,
                                    "Please check your email inbox for a password reset link",
                                    Toast.LENGTH_LONG).show();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(LoginActivity.this
                                    , "Failed to send a password reset link:" +e.getMessage()
                                    ,Toast.LENGTH_LONG).show();


                        }
                    });

                }





            }
        });

        buttonNo=(Button) submitPopup.findViewById(R.id.button_deny);
        buttonNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dialog.dismiss();

            }
        });


        dialogBuilder.setView(submitPopup);
        dialog = dialogBuilder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();



    }

    //Loging in method
    private void loginUser() {
        String email=emailText.getText().toString();
        String password=passwrdText.getText().toString();


        if(TextUtils.isEmpty(email)){
            emailText.setError("please add your email");
        }else if(TextUtils.isEmpty(password)){
            passwrdText.setError("please insert your password");

        }else{

            progressDialog.setMessage("Signing in...");
            progressDialog.show();

            //authentication process
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    progressDialog.dismiss();

                    if(task.isSuccessful()){
                        //get user tokens and update the database then log in user
                        String token_id = FirebaseInstanceId.getInstance().getToken();
                        String current_id = mAuth.getCurrentUser().getUid();

                        Map<String, Object> tokenMap = new HashMap<>();
                        tokenMap.put("token_id" , token_id);

                        mFirebaseFirestore.collection("users").document(current_id)
                                .update(tokenMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        startActivity(new Intent(getApplicationContext(),MainActivity.class));
                                        finish();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Toast.makeText(LoginActivity.this, "Error on Token update :.." + e.getMessage(),Toast.LENGTH_SHORT).show();

                            }
                        });
                    } else{

                        String errorMessage=task.getException().getMessage();
                        Toast.makeText(LoginActivity.this,"Error:"+errorMessage, Toast.LENGTH_SHORT).show();
                    }

                }
            });



        }








    }
}
