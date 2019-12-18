package com.example.uzimabetav10;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private TextView loginText;
    private Button buttonRegister;
    private EditText emailText, passwrdText;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        FirebaseApp.initializeApp(this);
        mAuth= FirebaseAuth.getInstance();

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

                    finish();
                    startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                    Toast.makeText(RegisterActivity.this,"Registration Successful",Toast.LENGTH_SHORT).show();

                }else{

                    //when user registration fails
                    finish();

                    String errorMessage=task.getException().getMessage();
                    Toast.makeText(RegisterActivity.this,"Error:"+errorMessage, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}

