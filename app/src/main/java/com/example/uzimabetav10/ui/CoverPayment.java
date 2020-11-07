package com.example.uzimabetav10.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.androidstudy.daraja.Daraja;
import com.androidstudy.daraja.DarajaListener;
import com.androidstudy.daraja.model.AccessToken;
import com.androidstudy.daraja.model.LNMExpress;
import com.androidstudy.daraja.model.LNMResult;
import com.androidstudy.daraja.util.Env;
import com.androidstudy.daraja.util.TransactionType;
import com.example.uzimabetav10.R;
import com.example.uzimabetav10.mpesa.MpesaListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.messaging.FirebaseMessaging;

import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CoverPayment extends AppCompatActivity  implements MpesaListener {

    private Spinner spinnerPaymentOption, spinnerPeriod;
    String periodTime = null;
    String pointsEarned = null;
    private TextView amountText , dteText , warningText , expiryDateTv;
    private CardView warningCard , infoCard;
    TextView dateText,transTxt, amntTxt , phnNum;
    Button okayButton;
    private EditText numText;
    private Button payNow;
    private ImageView cancelImg;
    String value;
    int value2;
    Dialog myDialog;
    private Button buttonProceed;
    Daraja daraja;
    public static MpesaListener mpesaListener;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    String user_id;

    String TAG = "CoverPayment:";


    String county , name , email , gender , image , contacts;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cover_payment);

        //instantiate firebase
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        user_id = mAuth.getCurrentUser().getUid();

        mpesaListener = this;

        //toolbar setup
        Toolbar toolbar = findViewById(R.id.single_post_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Payment");


        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CoverPayment.this, MainActivity.class));
            }
        });

        getUserDetails();
        //setExpiryDate();

        //Start mpesa daraja Service
        daraja = Daraja.with("MkkLhmysNy79BQy1zWIu11k4DaDG3Kxi",
                "pn5BCzc8T4XNRSkh",
                Env.SANDBOX, new DarajaListener<AccessToken>() {
                    @Override
                    public void onResult(@NonNull AccessToken accessToken) {

                    }

                    @Override
                    public void onError(String error) {

                    }
                });


        myDialog=new Dialog(this);


        amountText = findViewById(R.id.amount_text);
        dteText = (TextView)findViewById(R.id.date_textview);
        payNow = findViewById(R.id.pay_btn);
        numText = findViewById(R.id.phone_number_field);
        warningCard = findViewById(R.id.warning_card);
        infoCard = findViewById(R.id.info_card);
        expiryDateTv = findViewById(R.id.date_status_txt);
        warningText = findViewById(R.id.warning_text);




        payNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String phoneNumber = numText.getText().toString().trim();

                LNMExpress lnmExpress = new LNMExpress(
                        "174379", //Test credential but shortcode is mostly paybill number, email mpesa businnes fo clarification
                        "bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919",  //https://developer.safaricom.co.ke/test_credentials
                        TransactionType.CustomerPayBillOnline,  // TransactionType.CustomerPayBillOnline  <- Apply any of these two
                        "1",
                        phoneNumber,
                        "174379",
                        phoneNumber,
                        "https://us-central1-uzima-413c8.cloudfunctions.net/api/myCallBackUrl", // call back url send back payload info if the transactions went through. Its important inorder to update ui after user has paid, its essential but the service can work without it.
                        "001ABC",
                        "Goods Payment"
                ); //pass the required fields

                daraja.requestMPESAExpress(lnmExpress, new DarajaListener<LNMResult>() {
                    @Override
                    public void onResult(@NonNull LNMResult lnmResult) {



                        Toast.makeText(CoverPayment.this , "Response here:"+lnmResult.ResponseDescription , Toast.LENGTH_SHORT).show();

                        FirebaseMessaging.getInstance()
                                .subscribeToTopic(lnmResult.CheckoutRequestID.toString());

                    }

                    @Override
                    public void onError(String error) {

                        Toast.makeText(CoverPayment.this , "Error here:"+error , Toast.LENGTH_SHORT).show();
                        warningText.setText("Error:"+error);

                    }
                });

            }
        });




    }

    @Override
    protected void onStart() {
        super.onStart();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkMembership();
            }
        }, 2000);

    }

    @Override
    public void sendSuccesfull(@NotNull final String amount, @NotNull final String phone, @NotNull final String date, @NotNull final String receipt) {

        CoverPayment.this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(
                        CoverPayment.this, "Payment Succesfull\n" +
                                "Receipt:"+receipt+"\n" +
                                "Date:"+date+"\n" +
                                "Phone:"+phone+"\n" +
                                "Amount:"+amount, Toast.LENGTH_LONG
                ).show();

                myDialog.setContentView(R.layout.success_payment_popup);
                transTxt = (TextView) myDialog.findViewById(R.id.txt_trans_id);
                 dateText = (TextView) myDialog.findViewById(R.id.dte_txt);
                amntTxt = (TextView) myDialog.findViewById(R.id.txt_amount);
                phnNum = (TextView) myDialog.findViewById(R.id.txt_phone_number);
                okayButton = (Button) myDialog.findViewById(R.id.button_okay_payment);

                amntTxt.setText(amount);
                transTxt.setText(receipt);
                dateText.setText(date);
                phnNum.setText(phone);

                okayButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        myDialog.dismiss();

                        sendToPayDatabase(amount , receipt , date , phone);
                    }
                });


                myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                myDialog.show();



            }
        });






    }

    //create payment database

    private void sendToPayDatabase(final String amount, final String receipt, final String date, final String phone) {
        
        Map<String, Object> pay = new HashMap<>();
        pay.put("amount", amount);
        pay.put("phone", phone);
        pay.put("date", date);
        pay.put("receipt" , receipt);
        pay.put("user_id" , user_id);
        pay.put("status" , "new");
        pay.put("timestamp" , FieldValue.serverTimestamp());

        firebaseFirestore.collection("Payments").document()
                .set(pay)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Toast.makeText(CoverPayment.this , "Payment successfully added",Toast.LENGTH_SHORT).show();
                        
                        createMembership(amount , receipt , date , phone);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(CoverPayment.this , "Payment not added" + e,Toast.LENGTH_SHORT).show();

                    }
                });

    }


    //create or update membership database
    private void createMembership(final String amount, final String receipt, String date, String phone) {
        DocumentReference docRef = firebaseFirestore.collection("Members").document(user_id);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        DocumentReference memberRef = firebaseFirestore.collection("Members").document(user_id);

                        Date date = new Date();
                        Date exp_date = new DateTime(date).plusMonths(12).toDate();

// Set the "isCapital" field of the city 'DC'
                        memberRef
                                .update("status", "active",
                                        "condition" , "new",
                                        "expiry_date",exp_date,
                                        "amount_paid" , amount)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        updateCountyCount(amount);
                                        updateYearCount(amount);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        Toast.makeText(CoverPayment.this , "Failed to update Membership details" , Toast.LENGTH_SHORT).show();

                                    }
                                });

                    } else {

                        Date date = new Date();
                        Date exp_date = new DateTime(date).plusMonths(12).toDate();

                        Map<String, Object> city = new HashMap<>();
                        city.put("timestamp", FieldValue.serverTimestamp());
                        city.put("amount_paid", amount);
                        city.put("name", name);
                        city.put("image", image);
                        city.put("gender", gender);
                        city.put("county", county);
                        city.put("email", email);
                        city.put("contact", contacts);
                       city.put("expiry_date",exp_date );
                        city.put("status", "active");
                        city.put("condition", "new");

                        firebaseFirestore.collection("Members").document(user_id)
                                .set(city)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(CoverPayment.this , "Membership created, Welcome to the Uzima Team" , Toast.LENGTH_SHORT).show();
                                        updateCountyCount(amount);
                                        updateYearCount(amount);
                                        notifyDispatcher(amount , receipt);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(CoverPayment.this , "Membership creation Failed,Please contact the Main Offices " , Toast.LENGTH_SHORT).show();

                                    }
                                });




                    }
                } else {

                    Toast.makeText(CoverPayment.this , "Could not check " , Toast.LENGTH_SHORT).show();


                }
            }
        });
    }

    //notify dispatcher of payment

    private void notifyDispatcher(String amount, String receipt) {

        String message = "Payment received from"+name+" .Transaction ID"+receipt+" .Amount paid"+amount;

        Map<String, Object> not = new HashMap<>();
        not.put("condition" , "new");
        not.put("description",message);
        not.put("from", user_id);
        not.put("status" , "none");
        not.put("timestamp" , FieldValue.serverTimestamp());


        firebaseFirestore.collection("Dispatcher_Notification").document()
                .set(not)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: Dispatcher Informed");

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onSuccess: Dispatcher not Informed");
                    }
                });




    }

    //update grand total

    private void updateGrandTotal(String amount) {

        DocumentReference monthRef = firebaseFirestore.collection("Payment_Count").document("Grand_Total");

        int amnt = Integer.parseInt(amount);
// Atomically increment the population of the city by 50.
        monthRef.update("total", FieldValue.increment(amnt));


    }

    //update monthly gand total

    private void updateYearCount(String amount) {

        updateGrandTotal(amount);

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat month_date = new SimpleDateFormat("MMMM");
        String month_name = month_date.format(cal.getTime());

        DocumentReference monthRef = firebaseFirestore.collection("Payment_Count").document("Monthly_Total");

        int amnt = Integer.parseInt(amount);
// Atomically increment the population of the city by 50.
        monthRef.update(month_name, FieldValue.increment(amnt));



    }

    //update total per county

    private void updateCountyCount(String amount) {


        DocumentReference monthRef = firebaseFirestore.collection("Payment_Count/County/Counties").document(county);

        int amnt = Integer.parseInt(amount);
// Atomically increment the population of the city by 50.
        monthRef.update("total", FieldValue.increment(amnt));



    }

    //get user details on creation

    public void getUserDetails(){
        DocumentReference docRef = firebaseFirestore.collection("users").document(user_id);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        name = task.getResult().getString("name");
                        image = task.getResult().getString("image");
                        county = task.getResult().getString("county");
                        email = task.getResult().getString("email");
                        gender = task.getResult().getString("gender");
                        contacts = task.getResult().getString("phone");

                    } else {
                        Toast.makeText(CoverPayment.this, "No such user" , Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    @Override
    public void sendFailed(final String reason) {

        CoverPayment.this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(
                        CoverPayment.this, "Payment Failed\n" +
                                "Reason:"+reason
                        , Toast.LENGTH_LONG
                ).show();

            }
        });

    }

    private void checkMembership() {

        firebaseFirestore.collection("Members")
                .document(user_id)
                .addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                        if (e != null) {
                            Toast.makeText(CoverPayment.this, "Error while loading!", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, e.toString());
                            return;
                        }
                        if (documentSnapshot.exists()) {


                            String status = documentSnapshot.getString("status");
                            Date expDate = documentSnapshot.getTimestamp("expiry_date").toDate();
                            SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
                            String formattedDate = df.format(expDate);
                            String message = "Cover expiry date: " + formattedDate;


                            if(status.equals("active")){

                                warningCard.setVisibility(View.VISIBLE);
                                infoCard.setVisibility(View.VISIBLE);
                                expiryDateTv.setText(message);
                                payNow.setVisibility(View.GONE);

                            }else{

                                warningCard.setVisibility(View.VISIBLE);
                                warningText.setText("Your membership period has expired please renew below");
                                infoCard.setVisibility(View.GONE);
                                payNow.setVisibility(View.VISIBLE);
                            }


                        }else{

                            warningCard.setVisibility(View.GONE);

                        }
                    }
                });
    }


    public  void setExpiryDate(){

        Date date = Calendar.getInstance().getTime();
        Date exp_date = new DateTime(date).plusMonths(12).toDate();

        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
        String formattedDate = df.format(exp_date);
       dteText.setText(formattedDate);


    }







}

