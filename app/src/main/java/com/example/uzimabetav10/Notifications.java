package com.example.uzimabetav10;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.uzimabetav10.Messaging.NotificationsConstructor;
import com.example.uzimabetav10.Messaging.NotificationsRecyclerAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class Notifications extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private String user_id;
    private FloatingActionButton mFloatClear;


    //setup adapter elements
    private List<NotificationsConstructor> notificationsList;
    private RecyclerView notificationsRecyclerView;
    private NotificationsRecyclerAdapter notificationsRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        //firebase setup

        FirebaseApp.initializeApp(Notifications.this);
        firebaseAuth = FirebaseAuth.getInstance();
        user_id = firebaseAuth.getCurrentUser().getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();

        //fetch the notification details
        String dataMessage = getIntent().getStringExtra("message");
        String dataFrom = getIntent().getStringExtra("from_user_id");
        String dataTo = getIntent().getStringExtra("receiver_id");



        //setup the toolbar
        Toolbar toolbar = findViewById(R.id.single_post_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Notifications");

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Notifications.this, MainActivity.class));
                finish();
            }
        });

        //setup app widgets


        mFloatClear = findViewById(R.id.float_clear_all);



        //setup the list
        notificationsRecyclerView = findViewById(R.id.notifications_recycler_view);

        notificationsRecyclerView.setHasFixedSize(true);


        //setup the adapter
        notificationsList = new ArrayList<>();
        notificationsRecyclerAdapter = new NotificationsRecyclerAdapter(notificationsList);
        notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        notificationsRecyclerView.setAdapter(notificationsRecyclerAdapter);




        //fetch documents from firestore and set into the recycler adapter

        firebaseFirestore.collection("users").document(user_id).collection("Notifications")
                .addSnapshotListener(Notifications.this,new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                        if(!queryDocumentSnapshots.isEmpty()){

                            for(DocumentChange doc: queryDocumentSnapshots.getDocumentChanges()){

                                if(doc.getType() == DocumentChange.Type.ADDED){


                                    String dispatchId = doc.getDocument().getId();
                                    NotificationsConstructor notifications = doc.getDocument().toObject(NotificationsConstructor.class);
                                    notificationsList.add(notifications);
                                    notificationsRecyclerAdapter.notifyDataSetChanged();


                                }

                            }


                        }else{

                            Toast.makeText(Notifications.this, "The current user does not have any notification",Toast.LENGTH_SHORT).show();
                        }

                    }
                });


    }
}
