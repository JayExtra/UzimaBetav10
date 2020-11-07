package com.example.uzimabetav10.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.uzimabetav10.R;
import com.example.uzimabetav10.utils.Payment;
import com.example.uzimabetav10.utils.PaymentRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class Wallet extends AppCompatActivity {

    Dialog myDialog;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;
    private String user_id;
    private Button buttonOkay;
    private TextView totalTrans;


    private RecyclerView transactionsRecyclerView;
    private DocumentSnapshot lastVisible;
    private Boolean isFirstPageFirstLoad = true;
    private List<Payment> paymentsList;
    private PaymentRecyclerAdapter mPaymentRecyclerAdapter;
    private ProgressBar mProgressBar;


    String TAG = "Wallet:";
    int sum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        //check if account is suspended



        //initialize Firebase app and get Firebase instance
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        user_id = mAuth.getCurrentUser().getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();

        checkForSuspension();
        //toolbar setup
        Toolbar toolbar = findViewById(R.id.single_post_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("My Wallet");

        myDialog=new Dialog(this);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Wallet.this, MainActivity.class));
                finish();
            }
        });

        totalTrans = findViewById(R.id.total_trans_tv);
        transactionsRecyclerView = findViewById(R.id.payments_history_rv);
        mProgressBar = findViewById(R.id.load_payments);


        //Recycler view setup

        paymentsList = new ArrayList<>();
        mPaymentRecyclerAdapter = new PaymentRecyclerAdapter(paymentsList);
        transactionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        transactionsRecyclerView.setAdapter(mPaymentRecyclerAdapter);


        //Req location
        mProgressBar.setVisibility(View.VISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loadFirstPosts();
            }
        }, 2000);


        transactionsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                Boolean reachedBottom =  !recyclerView.canScrollVertically(1);

                if (reachedBottom){

                    //String desc = lastVisible.getString("title");
                    // Toast.makeText(PcrReports.this,"WE have reaced bottom of" + desc,Toast.LENGTH_SHORT).show();

                    loadMorePosts();


                }
            }
        });

        }

    public void loadFirstPosts(){

        mProgressBar.setVisibility(View.INVISIBLE);
        transactionsRecyclerView.setVisibility(View.VISIBLE);



        //retrieve firebase posts


        Query firstQuery = firebaseFirestore.collection("Payments")
                .whereEqualTo("user_id" , user_id)
                .limit(3);
        //.orderBy("arrival_time",Query.Direction.DESCENDING)
        // .limit(3);

        firstQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {

                if (isFirstPageFirstLoad) {


                    lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);

                }


                for(DocumentChange doc: queryDocumentSnapshots.getDocumentChanges()){

                    if(doc.getType() == DocumentChange.Type.ADDED){



                        Payment payment = doc.getDocument().toObject(Payment.class);

                        if(isFirstPageFirstLoad) {

                            paymentsList.add(payment);

                        }else{

                            paymentsList.add(0,payment);

                        }

                        mPaymentRecyclerAdapter.notifyDataSetChanged();


                    }

                }

                isFirstPageFirstLoad = false;

            }
        });
    }

    public void loadMorePosts(){

        //retrieve firebase posts

        Query nextQuery = firebaseFirestore.collection("Payments")
                .whereEqualTo("user_id" , user_id)
                //.orderBy("arrival_time",Query.Direction.DESCENDING)
                .startAfter(lastVisible)
                .limit(3);

        nextQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {

                if(!queryDocumentSnapshots.isEmpty()){

                    //start here
                    lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);

                    for(DocumentChange doc: queryDocumentSnapshots.getDocumentChanges()){

                        if(doc.getType() == DocumentChange.Type.ADDED){


                            Payment payment = doc.getDocument().toObject(Payment.class);
                            paymentsList.add(payment);

                            mPaymentRecyclerAdapter.notifyDataSetChanged();


                        }

                    }

                }

            }
        });



    }

    public void checkForSuspension(){

        DocumentReference docRef = firebaseFirestore.collection("Suspended_Accounts").document(user_id);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    // progressDialog.dismiss();
                    DocumentSnapshot document = task.getResult();
                    assert document != null;
                    if (document.exists()) {
                        String name2 = task.getResult().getString("name");

                        myDialog.setContentView(R.layout.popup_suspended);
                        buttonOkay=(Button) myDialog.findViewById(R.id.okay_btn);



                        buttonOkay.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                finish();
                                System.exit(0);
                            }
                        });



                        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        myDialog.show();




                        //Toast.makeText(MainActivity.this, "Welcome back " + name2, Toast.LENGTH_LONG).show();


//19-10-1996
                    } else {

                        //progressDialog.dismiss();

                        Toast.makeText(Wallet.this, "DATA DOES NOT EXISTS,PLEASE CREATE YOUR PROFILE", Toast.LENGTH_LONG).show();
                        //startActivity(new Intent(Wallet.this, EditProfile.class));


                    }
                } else {

                    String error = task.getException().getMessage();
                    Toast.makeText(Wallet.this, "(FIRESTORE RETRIEVE ERROR):" + error, Toast.LENGTH_LONG).show();


                }


            }
        });





    }

    @Override
    protected void onStart() {
        super.onStart();

                getTotalTransacted();

    }

    public void getTotalTransacted(){

        firebaseFirestore.collection("Payments")
                .whereEqualTo("user_id", user_id)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        List<Integer> amount = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            if (doc.get("user_id") != null) {

                                amount.add(Integer.parseInt( doc.getString("amount")));

                            }
                        }
                        Log.d(TAG, "Current amount are: " + amount);



                        for(int i = 0; i < amount.size(); i++)
                            sum += amount.get(i);


                        totalTrans.setText("Ksh. "+Integer.toString(sum));



                    }
                });







    }
}
