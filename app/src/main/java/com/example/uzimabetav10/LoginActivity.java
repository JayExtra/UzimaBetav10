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

public class LoginActivity extends AppCompatActivity {

    private TextView regText;
    private Button buttonLogin;
    private EditText emailText, passwrdText;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //initialize Firebase app and get Firebase instance
        FirebaseApp.initializeApp(this);
        mAuth= FirebaseAuth.getInstance();

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
    }

        //Loging in method
    private void loginUser() {
        String email=emailText.getText().toString();
        String password=passwrdText.getText().toString();

        progressDialog.setMessage("Signing in...");
        progressDialog.show();

        //authentication process
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                progressDialog.dismiss();

                if(task.isSuccessful()){
                    //start choose account activity
                    finish();
                    startActivity(new Intent(getApplicationContext(),MainActivity.class));
                } else{

                    String errorMessage=task.getException().getMessage();
                    Toast.makeText(LoginActivity.this,"Error:"+errorMessage, Toast.LENGTH_SHORT).show();
                }

            }
        });




    }
}
