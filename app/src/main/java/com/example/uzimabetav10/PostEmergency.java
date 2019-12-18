package com.example.uzimabetav10;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.material.textfield.TextInputEditText;

public class PostEmergency extends AppCompatActivity {
    private TextInputEditText titleText,detailsText;
    private ImageView imageSelect;
    private Button sendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_emergency);

        //setup widgets
        titleText=findViewById(R.id.edit_title);
        detailsText=findViewById(R.id.edit_details);
        imageSelect=findViewById(R.id.image_select);
        sendButton=findViewById(R.id.button_send);




        //Toolbar settings
        Toolbar toolbar = findViewById(R.id.main_interface_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Post Emergency");

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PostEmergency.this, EmergencyFeeds.class));
            }
        });
    }
}
