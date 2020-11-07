package com.example.uzimabetav10;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.uzimabetav10.ui.CoverPayment;
import com.example.uzimabetav10.ui.EmergencyFeeds;
import com.example.uzimabetav10.ui.MainActivity;

public class ContactSupport extends AppCompatActivity {
    private EditText subjectText , messageText;
    private ImageView callImage;
    private Button sendEmailButton;
    String  number="+254759783805";
    private static final int REQUEST_CALL =1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_support);

        //toolbar setup
        Toolbar toolbar = findViewById(R.id.single_post_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Contact Support");


        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ContactSupport.this, MainActivity.class));
            }
        });

        callImage = findViewById(R.id.call_img);
        subjectText = findViewById(R.id.editTextSubject);
        messageText = findViewById(R.id.editTextMessage);
        sendEmailButton = findViewById(R.id.btn_send_email);


        sendEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMail();
            }
        });

        callImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ContextCompat.checkSelfPermission(ContactSupport.this, Manifest.permission.CALL_PHONE)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(ContactSupport.this,new String[]{Manifest.permission.CALL_PHONE},REQUEST_CALL);

                }else{
                    String dial= "tel:" + number;
                    startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
                }

            }
        });
    }

    private void sendMail() {

        String subject = subjectText.getText().toString();
        String message = messageText.getText().toString();

        if (message.isEmpty()) {

            messageText.setError("Field can't be empty");

        } else if(subject.isEmpty()){

            subjectText.setError("Field can't be empty");


        }else {

            String recepient = "uzima472@gmail.com";
            String[] recepients = recepient.split(",");


            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_EMAIL, recepients);
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
            intent.putExtra(Intent.EXTRA_TEXT, message);
            intent.setType("message/rfc822");
            startActivity(Intent.createChooser(intent, "Choose an email client"));

    }


    }
}
