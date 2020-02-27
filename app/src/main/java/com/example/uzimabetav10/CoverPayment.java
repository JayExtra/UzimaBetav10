package com.example.uzimabetav10;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class CoverPayment extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Spinner spinnerPaymentOption , spinnerPeriod;
    String periodTime = null;
    String pointsEarned = null;
    private TextView amountText,pointsText;

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


        spinnerPaymentOption = findViewById(R.id.payment_option_spinner);
        spinnerPeriod = findViewById(R.id.period_spinner);
        amountText = findViewById(R.id.amount_text);
        pointsText = findViewById(R.id.points_text);

        ArrayAdapter<CharSequence> paymentAdapter = ArrayAdapter.createFromResource(this,R.array.payment,android.R.layout.simple_spinner_item);
        paymentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPaymentOption.setAdapter(paymentAdapter);



        ArrayAdapter<CharSequence> periodAdapter = ArrayAdapter.createFromResource(this,R.array.period,android.R.layout.simple_spinner_item);
        periodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPeriod.setAdapter(periodAdapter);
        spinnerPeriod.setOnItemSelectedListener(this);












    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
        Spinner spin = (Spinner)parent;
        if(spin.getId() == R.id.period_spinner)
        {
            String txt = parent.getItemAtPosition(position).toString();
            periodTime=txt;

            Toast.makeText(CoverPayment.this,"Period selected is:"+periodTime,Toast.LENGTH_SHORT).show();
            calcuulateAmount();


        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }


    public void calcuulateAmount(){

        int periodNum = Integer.parseInt(periodTime);
        int amount = 2500;

        String value = String.valueOf(periodNum*amount);

        amountText.setText("Ksh."+value);

        pointsEarned = String.valueOf(periodNum*20);

        pointsText.setText(pointsEarned);










    }
}
