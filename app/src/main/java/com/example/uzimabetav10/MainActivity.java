package com.example.uzimabetav10;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;


public class MainActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    private CardView emergencyCard;
    private ImageView slideshow;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialize Firebase app and get Firebase instance
        FirebaseApp.initializeApp(this);
        mAuth= FirebaseAuth.getInstance();

        //map widgets
        emergencyCard=findViewById(R.id.emergency_card);
        slideshow =findViewById(R.id.slide_img);

        String currentuser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        
        Toast.makeText(this,"user id is:"+currentuser,Toast.LENGTH_SHORT).show();

        Toolbar toolbar = findViewById(R.id.main_interface_toolbar);
        setSupportActionBar(toolbar);


        //starts the progress dialog
        progressDialog=new ProgressDialog(this);

        emergencyCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, EmergencyFeeds.class));
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.profile:
                startActivity(new Intent(MainActivity.this, Profile.class));
                return true;

            case R.id.logout:
                progressDialog.setMessage("Signing out...");
                progressDialog.show();

                mAuth.signOut();
                finish();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                return true;

            case R.id.contact_sup:
                Toast.makeText(this,"contact was selected",Toast.LENGTH_SHORT).show();
                return true;

                default:

                    return super.onOptionsItemSelected(item);
        }

    }
}
