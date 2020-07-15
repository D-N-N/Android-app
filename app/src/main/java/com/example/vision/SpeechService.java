package com.example.vision;

import android.app.Activity;
import android.app.Application;
import android.widget.Toast;

import com.mapzen.speakerbox.Speakerbox;

public class SpeechService {

    private Activity activity;
    private Speakerbox speakerbox;

    public SpeechService(Activity activity) {

        this.activity = activity;
    }

    public void textToSpeech(String txt){

        try {
            speakerbox = new Speakerbox(activity.getApplication());
            speakerbox.play(txt);

        }catch (Exception e){
            Toast.makeText(activity.getApplicationContext(),e+"",Toast.LENGTH_LONG).show();

        }
    }
}
