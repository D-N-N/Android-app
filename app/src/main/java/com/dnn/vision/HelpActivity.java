package com.dnn.vision;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.dnn.vision.Settings.VibrationModule;
import com.dnn.vision.Utilities.Logger;

public class HelpActivity extends AppCompatActivity
{
    private static final Logger LOGGER = new Logger();
    private GestureDetector mDetector;
    private SpeechService speechService;
    protected VibrationModule vibrationModule;

    View.OnTouchListener touchListener = new View.OnTouchListener()
    {
        @Override
        public boolean onTouch(View v, MotionEvent event)
        {
            return mDetector.onTouchEvent(event);

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        mDetector = new GestureDetector(this, new HelpGestureListener());
        speechService = new SpeechService(this);

        View rootView = findViewById(R.id.help_root);
        rootView.setOnTouchListener(touchListener);

        setTitle("Help");
        ActionBar actionBar = this.getSupportActionBar();

        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        speechService.textToSpeech("Help is open.");
        speechService.textToSpeech("Tap once on the screen to listen to help.");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if (id == android.R.id.home)
        {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private class HelpGestureListener extends GestureDetector.SimpleOnGestureListener
    {

        @Override
        public boolean onDown(MotionEvent event)
        {
            LOGGER.d("onDown: ");

            return true;
        }

        @Override
        public void onLongPress(MotionEvent e)
        {
            LOGGER.i("onLongPress: ");
            Intent intent = new Intent(HelpActivity.this, MainActivity.class);
            startActivity(intent);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e)
        {
            String titleNavigation = getString(R.string.navigation_title);
            String helpNavigation = getString(R.string.navigation_text);
//            speechService.textToSpeech(titleNavigation);
//            speechService.textToSpeech(helpNavigation);
//
            String titleCurrency = getString(R.string.currency_detection_help_title);
            String textCurrency = getString(R.string.currency_detection_help_text);
//            speechService.textToSpeech(titleCurrency);
//            speechService.textToSpeech(textCurrency);

            String wholeMsg = String.format("%s\n%s\n\n%s\n%s",titleNavigation,helpNavigation,titleCurrency,textCurrency);
            speechService.textToSpeech(wholeMsg);
            return true;
        }


    }
}
