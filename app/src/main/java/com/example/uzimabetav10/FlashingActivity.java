package com.example.uzimabetav10;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.ImageView;

public class FlashingActivity extends AppCompatActivity {

    private ImageButton backBtn;
    private ImageView cntrImg;


     MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashing);


        backBtn = findViewById(R.id.back_btn);
        cntrImg = findViewById(R.id.cnter_img);


        //start process
        startLights();
        startSiren();




        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mediaPlayer.stop();


                startActivity(new Intent(FlashingActivity.this,MainActivity.class));



            }
        });

    }

    public void startSiren(){

        mediaPlayer = MediaPlayer.create(this, R.raw.warning1);
        mediaPlayer.start();
        mediaPlayer.setLooping(true);

    }

    @SuppressLint("WrongConstant")
    public void startLights(){

        ObjectAnimator anim = ObjectAnimator.ofInt(cntrImg,"BackgroundColor", Color.RED,Color.BLUE,Color.WHITE);
        anim.setDuration(120);
        anim.setEvaluator(new ArgbEvaluator());
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        anim.start();


    }
}
