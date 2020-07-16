package com.example.vision;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Build;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;

import com.example.vision.Settings.vibretor;

public class SettingsActivity extends AppCompatActivity {

    private AudioManager audioManager;
    private SharedPreferences prefVolume,prefAppStartKey;
    private  SharedPreferences.Editor editorVolume,editorAppStartKey;
    private vibretor vibretor;
    private SpeechService speechService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle("Settings");
        ActionBar actionBar = this.getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.planets_array, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        SeekBar seekBar = findViewById(R.id.seekbar);



        vibretor = new vibretor(500,getApplicationContext());

        prefVolume = getApplicationContext().getSharedPreferences("VolumeValue", 0); // 0 - for private mode
        editorVolume = prefVolume.edit();

        prefAppStartKey = getApplicationContext().getSharedPreferences("AppStartKey", 0); // 0 - for private mode
        editorAppStartKey = prefAppStartKey.edit();

        audioManager = (AudioManager) getSystemService(this.getApplicationContext().AUDIO_SERVICE);

        seekBar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        seekBar.setProgress(prefVolume.getInt("VolumeValue", -1));

        speechService = new SpeechService(this);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int newVolume, boolean b) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0);

                //speech
                speechService.textToSpeech("Set volume as "+newVolume);

                //set volume value into shared memory
                editorVolume.putInt("VolumeValue", newVolume);
                editorVolume.commit();
                vibretor.execute();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        spinner.setSelection(prefAppStartKey.getInt("AppStartKey", 0));// set selected previous value in spinner
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if(prefAppStartKey.getInt("AppStartKey", 0) != position) {
                    editorAppStartKey.putInt("AppStartKey", position);
                    editorAppStartKey.commit();
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                        SettingsActivity.this.startForegroundService(new Intent(SettingsActivity.this, service.class));
//                    } else {
                        startService(new Intent(SettingsActivity.this, service.class));
                    //}
                    if (position == 0) {
                        speechService.textToSpeech("start application when screen on");
                    } else if (position == 1) {
                        speechService.textToSpeech("start application when media connect");
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
