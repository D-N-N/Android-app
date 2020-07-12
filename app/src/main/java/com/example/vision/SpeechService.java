package com.example.vision;

import android.app.Activity;
import com.mapzen.speakerbox.Speakerbox;

public class SpeechService {

    private Activity activity;
    private Speakerbox speakerbox;

    public SpeechService(Activity activity) {

        this.activity = activity;
    }

    public void textToSpeech(String txt){

        speakerbox = new Speakerbox(activity.getApplication());
        speakerbox.play(txt);
    }
}
