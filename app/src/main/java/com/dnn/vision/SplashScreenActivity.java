package com.dnn.vision;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.dnn.vision.Settings.VibrationModule;
import com.dnn.vision.R;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        SpeechService speechService = new SpeechService(this);
        speechService.textToSpeech("Application sleep");

        RelativeLayout layout = (RelativeLayout)findViewById(R.id.splash);
        layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Thread loading = new Thread() {
                    public void run() {
                        try {
                            VibrationModule VibrationModule = new VibrationModule(500,getApplicationContext());
                            VibrationModule.execute();
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
