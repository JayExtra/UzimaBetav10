package com.example.uzimabetav10;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import android.widget.Toast;

import com.example.uzimabetav10.utils.EmergencyPosts;
import com.example.uzimabetav10.utils.EmergencyRecyclerAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class EmergencyFeeds extends AppCompatActivity {

    private FloatingActionButton floatingActionButton , floatingCall;
    Dialog myDialog;
    Button buttonCancel;
    Button buttonYes;
    private String number,user_id;
    private static final int REQUEST_CALL =1;

    private RecyclerView feedsRecycler;
    private EmergencyRecyclerAdapter emergencyRecyclerAdapter;
    private DocumentSnapshot lastVisible;
    private Boolean isFirstPageFirstLoad = true;

    private List<EmergencyPosts> emergency_list;

    //importing firebase
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_feeds);

        //firebase setup
        FirebaseApp.initializeApp(this);
        firebaseAuth = FirebaseAuth.getInstance();
        user_id = firebaseAuth.getCurrentUser().getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();

        number="+254759783805";

        //setup widgets
        floatingActionButton=findViewById(R.id.create_post);
        floatingCall=findViewById(R.id.button_call);


        myDialog=new Dialog(this);


        //Recycler View settings

        emergency_list = new ArrayList<>();
        feedsRecycler =findViewById(R.id.emergency_list_view);
        emergencyRecyclerAdapter = new EmergencyRecyclerAdapter(emergency_list);
        feedsRecycler.setLayoutManager(new LinearLayoutManager(this));
        feedsRecycler.setAdapter(emergencyRecyclerAdapter);



        //Toolbar settings
        Toolbar toolbar = findViewById(R.id.single_post_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Emergency feeds");

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);

        Intent retrieveIntent= getIntent();
        final String lat= retrieveIntent.getStringExtra("LATITUDE");
        final String log= retrieveIntent.getStringExtra("LONGITUDE");

        Toast.makeText(this,"Your Location:"+"\n"+"Latitude= "+lat+"\n"+"Longitude= "+log,Toast.LENGTH_SHORT).show();



        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EmergencyFeeds.this, MainActivity.class));
            }
        });

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent emergencyIntent= new Intent(EmergencyFeeds.this,PostEmergency.class);
                emergencyIntent.putExtra("LATITUDE",lat);
                emergencyIntent.putExtra("LONGITUDE",log);
                startActivity(emergencyIntent);

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

        feedsRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                Boolean reachedBottom =  !recyclerView.canScrollVertically(1);

                if (reachedBottom){

                    String desc = lastVisible.getString("title");
                    Toast.makeText(EmergencyFeeds.this,"WE have reaced bottom of" + desc,Toast.LENGTH_SHORT).show();

                    loadMorePosts();


                }
            }
        });






        //retrieve firebase posts

        Query firstQuery = firebaseFirestore.collection("Emergency_Feeds").orderBy("timestamp",Query.Direction.DESCENDING).limit(3);

       firstQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                if (isFirstPageFirstLoad) {


                    lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);

                }


                for(DocumentChange doc: queryDocumentSnapshots.getDocumentChanges()){

                    if(doc.getType() == DocumentChange.Type.ADDED){

                        String emergencyPostId = doc.getDocument().getId();

                        EmergencyPosts emergencyPosts = doc.getDocument().toObject(EmergencyPosts.class).withId(emergencyPostId);

                        if(isFirstPageFirstLoad) {

                            emergency_list.add(emergencyPosts);

                        }else{

                            emergency_list.add(0,emergencyPosts);

                        }

                        emergencyRecyclerAdapter.notifyDataSetChanged();


                    }

                }

                isFirstPageFirstLoad = false;

            }
        });
    }

    public void loadMorePosts(){

        //retrieve firebase posts

        Query nextQuery = firebaseFirestore.collection("Emergency_Feeds")
                .orderBy("timestamp",Query.Direction.DESCENDING)
                .startAfter(lastVisible)
                .limit(3);

        nextQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

            if(!queryDocumentSnapshots.isEmpty()){

                //start here
                lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);

                for(DocumentChange doc: queryDocumentSnapshots.getDocumentChanges()){

                    if(doc.getType() == DocumentChange.Type.ADDED){

                        String emergencyPostId = doc.getDocument().getId();

                        EmergencyPosts emergencyPosts = doc.getDocument().toObject(EmergencyPosts.class).withId(emergencyPostId);

                        emergency_list.add(emergencyPosts);

                        emergencyRecyclerAdapter.notifyDataSetChanged();


                    }

                }

            }

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
