package com.example.uzimabetav10;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class EmergencyFeeds extends AppCompatActivity {

    private FloatingActionButton floatingActionButton , floatingCall;
    Dialog myDialog;
    Button buttonCancel;
    Button buttonYes;
    private String number;
    private static final int REQUEST_CALL =1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_feeds);

        number="+254759783805";


        floatingActionButton=findViewById(R.id.create_post);
        floatingCall=findViewById(R.id.button_call);

        myDialog=new Dialog(this);


        //Toolbar settings
        Toolbar toolbar = findViewById(R.id.main_interface_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Emergency feeds");

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EmergencyFeeds.this, MainActivity.class));
            }
        });

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(EmergencyFeeds.this, PostEmergency.class));

            }
        });

        //Handles call button
        floatingCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                myDialog.setContentView(R.layout.popup);
                buttonCancel=(Button) myDialog.findViewById(R.id.button_no);
                buttonYes=(Button) myDialog.findViewById(R.id.button_yes);



                buttonCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        myDialog.dismiss();
                    }
                });

                buttonYes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        makeCall();
                        myDialog.dismiss();

                    }
                });

                myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                myDialog.show();



            }
        });
    }

    public void makeCall(){

        if(ContextCompat.checkSelfPermission(EmergencyFeeds.this, Manifest.permission.CALL_PHONE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(EmergencyFeeds.this,new String[]{Manifest.permission.CALL_PHONE},REQUEST_CALL);

        }else{
            String dial= "tel:" + number;
            startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
        }

    }
}
