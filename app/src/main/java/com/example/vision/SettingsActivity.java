package com.example.vision;

import android.content.SharedPreferences;
import android.media.AudioManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;

import com.example.vision.Settings.vibretor;

public class SettingsActivity extends AppCompatActivity {

    private AudioManager audioManager;
    private SharedPreferences pref;
    private  SharedPreferences.Editor editor;
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

        pref = getApplicationContext().getSharedPreferences("VolumeValue", 0); // 0 - for private mode
        editor = pref.edit();

        audioManager = (AudioManager) getSystemService(this.getApplicationContext().AUDIO_SERVICE);

        seekBar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        seekBar.setProgress(pref.getInt("VolumeValue", -1));

        speechService = new SpeechService(this);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int newVolume, boolean b) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0);

                //speech
                speechService.textToSpeech("Set volume as "+newVolume);

                //set volume value into shared memory
                editor.putInt("VolumeValue", newVolume);
                editor.commit();
                vibretor.execute();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
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
