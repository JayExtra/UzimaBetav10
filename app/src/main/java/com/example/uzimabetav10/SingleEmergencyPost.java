package com.example.uzimabetav10;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.uzimabetav10.utils.Comments;
import com.example.uzimabetav10.utils.CommentsRecyclerAdapter;
import com.example.uzimabetav10.utils.EmergencyPosts;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nullable;

public class SingleEmergencyPost extends AppCompatActivity {

    private String emergency_post_id,user_id,postAdress;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private TextView descText,ttlText,postDate,postLocation,userName;
    private ImageView emergencyImage,sendImage;
    private Uri mainImageURI=null;
    private Geocoder geocoder;
    private Button navButton;
    private EditText commentField;
    private RecyclerView comment_list_all;
    private CommentsRecyclerAdapter commentsRecyclerAdapter;

    private List<Comments>commentsList;

    List<Address> adresses;
    Double lat, lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_emergency_post);

        //firebase setup
        FirebaseApp.initializeApp(this);
        firebaseAuth = FirebaseAuth.getInstance();
        user_id = firebaseAuth.getCurrentUser().getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();

        //toolbar setup
        Toolbar toolbar = findViewById(R.id.single_post_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SingleEmergencyPost.this, EmergencyFeeds.class));
            }
        });

        //retrieve post id
        emergency_post_id = getIntent().getStringExtra("DOCUMENT_ID");


        //setup widgets
        descText = findViewById(R.id.card_description2);
        emergencyImage = findViewById(R.id.imageView5);
        ttlText = findViewById(R.id.card_title2);
        postDate = findViewById(R.id.time_date2);
        postLocation = findViewById(R.id.loc_text2);
        navButton = findViewById(R.id.button_navigate);
        sendImage = findViewById(R.id.sendImage);
        commentField = findViewById(R.id.comment_field);
        comment_list_all = findViewById(R.id.comments_list);

        //Recycler view setup

        commentsList = new ArrayList<>();
        commentsRecyclerAdapter = new CommentsRecyclerAdapter(commentsList);
        comment_list_all.setHasFixedSize(true);
        comment_list_all.setLayoutManager(new LinearLayoutManager(this));
        comment_list_all.setAdapter(commentsRecyclerAdapter);


        //navigation button
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startNavigation();
            }
        });

        //retrieve comments from database
        firebaseFirestore.collection("Emergency_Posts/"+emergency_post_id+"/comments")
                .addSnapshotListener(SingleEmergencyPost.this,new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                if(!queryDocumentSnapshots.isEmpty()){

                    for(DocumentChange doc: queryDocumentSnapshots.getDocumentChanges()){

                        if(doc.getType() == DocumentChange.Type.ADDED){

                            String commentId = doc.getDocument().getId();
                            Comments comments = doc.getDocument().toObject(Comments.class);
                            commentsList.add(comments);
                            commentsRecyclerAdapter.notifyDataSetChanged();




                        }

                    }




                }

            }
        });


        //comment button

        sendImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String comment_message = commentField.getText().toString();
                if(!comment_message.isEmpty()){

                    Map<String, Object> commentMap = new HashMap<>();
                    commentMap.put("message",comment_message);
                    commentMap.put("user_id",user_id);
                    commentMap.put("timestamp", FieldValue.serverTimestamp());


                    firebaseFirestore.collection("Emergency_Posts/"+emergency_post_id+"/comments").add(commentMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if(!task.isSuccessful()){

                                Toast.makeText(SingleEmergencyPost.this,"Error posting comment:"+task.getException().getMessage(),Toast.LENGTH_SHORT).show();

                            }else{
                                commentField.setText("");
                            }
                        }
                    });

                }
            }
        });


        //retrieve post details
        DocumentReference docRef = firebaseFirestore.collection("Emergency_Posts").document(emergency_post_id);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {

                    DocumentSnapshot document = task.getResult();

                    if (document.exists()) {
                        String details = task.getResult().getString("details");
                        String image_uri = task.getResult().getString("house_image_uri");
                        GeoPoint location = task.getResult().getGeoPoint("location");
                        Timestamp post_date = task.getResult().getTimestamp("timestamp");
                        String title = task.getResult().getString("title");
                        String user_id = task.getResult().getString("user_id");
                        String name2 = task.getResult().getString("name");

                        //CONVERT TIMESTAMP TO DATE
                        //String dateString = DateFormat.format("MM/dd/yyyy", new Date(milliseconds)).toString();//gets the date in which the house was posted
                        Date date = post_date.toDate();
                        // the string representation of date according to the chosen pattern
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-mm-yyyy hh:mm:ss");
                        String strDate = dateFormat.format(date);

                        //setup geocoder

                        geocoder = new Geocoder(SingleEmergencyPost.this, Locale.getDefault());
                        lat = location.getLatitude();
                        lng = location.getLongitude();

                        try {

                            adresses = geocoder.getFromLocation(lat, lng, 1);
                            String address = adresses.get(0).getAddressLine(0);

                            String fulladdress = address + "";
                            postAdress = fulladdress;


                            //Toast.makeText(MainActivity.this, "Your Location:"+mainAdress, Toast.LENGTH_SHORT).show();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }





                        descText.setText(details);
                        ttlText.setText(title);
                        postDate.setText(strDate);
                        postLocation.setText(postAdress);






                        if (image_uri != null) {

                            mainImageURI = Uri.parse(image_uri);



                            //******replacing the dummy image with real profile picture******
                            RequestOptions placeholderRequest = new RequestOptions();
                            placeholderRequest.placeholder(R.drawable.grey_background);

                            Glide.with(SingleEmergencyPost.this).setDefaultRequestOptions(placeholderRequest).load(image_uri).into(emergencyImage);
                        } else {

                           emergencyImage.setImageResource(R.drawable.alaert_icon);

                        }


                    } else {


                        Toast.makeText(SingleEmergencyPost.this, "DATA DOES NOT EXISTS,PLEASE REGISTER", Toast.LENGTH_LONG).show();


                    }
                } else {


                    String error = task.getException().getMessage();
                    Toast.makeText(SingleEmergencyPost.this, "(FIRESTORE RETRIEVE ERROR):" + error, Toast.LENGTH_LONG).show();


                }


            }
        });



    }

    public void startNavigation(){

        //start navigation towards the emergency

        Uri gmmIntentUri = Uri.parse("google.navigation:q="+lat+","+lng);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(getPackageManager()) != null) {

            startActivity(mapIntent);

        }



    }
}

