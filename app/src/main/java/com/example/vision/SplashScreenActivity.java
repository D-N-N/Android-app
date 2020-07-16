package com.example.vision;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import com.example.vision.Settings.vibretor;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        SpeechService speechService = new SpeechService(this);
        speechService.textToSpeech("Application sleep");

        android.support.constraint.ConstraintLayout layout = (android.support.constraint.ConstraintLayout)findViewById(R.id.splash);
        layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Thread loading = new Thread() {
                    public void run() {
                        try {
                            vibretor vibretor = new vibretor(500,getApplicationContext());
                            vibretor.execute();
                            Intent main = new Intent(SplashScreenActivity.this,MainActivity.class);
                            startActivity(main);
                            finish();


                        }

                        catch (Exception e) {
                            e.printStackTrace();
                        }

                        finally {
                            finish();
                        }
                    }
                };

                loading.start();
                return false;
            }
        });
    }
}
