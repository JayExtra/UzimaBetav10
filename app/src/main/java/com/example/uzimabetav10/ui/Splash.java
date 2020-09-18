package com.example.uzimabetav10.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;

import com.example.uzimabetav10.R;

public class Splash extends AppCompatActivity {

    private ProgressBar mProgressBar;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        mProgressBar = findViewById(R.id.progressBar);

        mProgressBar.setProgressTintList(ColorStateList.valueOf(R.color.colorProgress));

        mProgressBar.setVisibility(View.VISIBLE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mProgressBar.setVisibility(View.GONE);

                Intent intent = new Intent(Splash.this , LoginActivity.class);
                startActivity(intent);

                finish();

            }
        }, 5000);
    }
}
