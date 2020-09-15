package com.example.uzimabetav10.ui;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.uzimabetav10.Config.Config;
import com.example.uzimabetav10.R;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONException;

import java.math.BigDecimal;

public class CoverPayment extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Spinner spinnerPaymentOption, spinnerPeriod;
    String periodTime = null;
    String pointsEarned = null;
    private TextView amountText, pointsText, amountTxt, transTxt, statusTxt;
    private Button payNow;
    private ImageView cancelImg;
    String value;
    int value2;
    Dialog myDialog;
    private Button buttonProceed;

    private static final int PAYPAL_REQUEST_CODE = 7171;

    private static PayPalConfiguration config = new PayPalConfiguration()
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            .clientId(Config.PAYPAL_CLIENT_ID);


    @Override
    protected void onDestroy() {

        stopService(new Intent(this, PayPalService.class));

        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cover_payment);

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

        //Start paypal Service

        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(intent);

        myDialog=new Dialog(this);


        spinnerPaymentOption = findViewById(R.id.payment_option_spinner);
        spinnerPeriod = findViewById(R.id.period_spinner);
        amountText = findViewById(R.id.amount_text);
        pointsText = findViewById(R.id.points_text);
        payNow = findViewById(R.id.pay_btn);

        ArrayAdapter<CharSequence> paymentAdapter = ArrayAdapter.createFromResource(this, R.array.payment, android.R.layout.simple_spinner_item);
        paymentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPaymentOption.setAdapter(paymentAdapter);


        ArrayAdapter<CharSequence> periodAdapter = ArrayAdapter.createFromResource(this, R.array.period, android.R.layout.simple_spinner_item);
        periodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPeriod.setAdapter(periodAdapter);
        spinnerPeriod.setOnItemSelectedListener(this);


        payNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processPayment();
            }
        });


    }

    private void processPayment() {

        PayPalPayment payPalPayment = new PayPalPayment(new BigDecimal(String.valueOf(value2)), "USD",
                "Pay to Uzima Emergency Team", PayPalPayment.PAYMENT_INTENT_SALE);

        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payPalPayment);
        startActivityForResult(intent, PAYPAL_REQUEST_CODE);


    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
        Spinner spin = (Spinner) parent;
        if (spin.getId() == R.id.period_spinner) {
            String txt = parent.getItemAtPosition(position).toString();
            periodTime = txt;

            Toast.makeText(CoverPayment.this, "Period selected is:" + periodTime, Toast.LENGTH_SHORT).show();
            calcuulateAmount();


        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }


    public void calcuulateAmount() {

        int periodNum = Integer.parseInt(periodTime);
        int amount = 100;

        value = String.valueOf(periodNum * amount);

        value2 = periodNum * amount;

        amountText.setText("Ksh." + value);

        pointsEarned = String.valueOf(periodNum * 20);

        pointsText.setText(pointsEarned);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PAYPAL_REQUEST_CODE) {

            if (resultCode == RESULT_OK) {

                PaymentConfirmation confirmation = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirmation != null) {
                    try {
                        String paymentDetails = confirmation.toJSONObject().toString(4);

                        myDialog.setContentView(R.layout.payment_popup);
                        buttonProceed=(Button) myDialog.findViewById(R.id.button_proceed);
                        transTxt=myDialog.findViewById(R.id.trans_id);
                        amountTxt=myDialog.findViewById(R.id.amount_txt);
                        statusTxt=myDialog.findViewById(R.id.status_txt);
                        cancelImg=myDialog.findViewById(R.id.cancel_img);


                        TextView dialogText = (TextView) myDialog.findViewById(R.id.dialog_text);

                        transTxt.setText(paymentDetails);




                        cancelImg.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                myDialog.dismiss();
                            }
                        });

                        buttonProceed.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                //sendToDatabase();
                                myDialog.dismiss();

                            }
                        });

                        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        myDialog.show();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }else if(resultCode == Activity.RESULT_CANCELED)
                Toast.makeText(this,"Cancel",Toast.LENGTH_SHORT).show();
        }

        else if(resultCode == PaymentActivity.RESULT_EXTRAS_INVALID)
            Toast.makeText(this,"Invalid", Toast.LENGTH_SHORT).show();

    }
}

