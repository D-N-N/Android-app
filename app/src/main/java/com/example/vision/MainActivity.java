package com.example.vision;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.vision.Fragment.CurrencyFragment;
import com.example.vision.Fragment.TextFragment;
import com.example.vision.Settings.vibretor;

import java.security.Provider;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private TabLayout tableLayout;
    public int VolumeValue = 0;
    private SpeechService speechService;
    private CurrencyFragment currencyFragment;
    private TextFragment textFragment;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        vibretor vibretor = new vibretor(500,getApplicationContext());
        speechService = new SpeechService(this);

        if (id == R.id.setting_settings) {
            vibretor.execute();
            speechService.textToSpeech("Settings is open");
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }else if(id == R.id.help_settings){
            vibretor.execute();
            speechService.textToSpeech("Help is open");
            Intent intent = new Intent(MainActivity.this, HelpActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       tableLayout = findViewById(R.id.tab_layput);
       ViewPager viewPager = findViewById(R.id.view_pager);
       currencyFragment = new CurrencyFragment();
       textFragment = new TextFragment();

        ViewpagerAdapter viewpagerAdapter = new ViewpagerAdapter(getSupportFragmentManager());

        //start background service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            MainActivity.this.startForegroundService(new Intent(MainActivity.this, service.class));
        } else {
            startService(new Intent(MainActivity.this, service.class));
        }

        viewpagerAdapter.addFragment(currencyFragment,"Currency");
        viewpagerAdapter.addFragment(textFragment,"Text");

        //speech
        speechService = new SpeechService(this);
        speechService.textToSpeech("Application started");


        /*start vibrate*/
        vibretor vibretor = new vibretor(500,getApplicationContext());
        vibretor.execute();

        SharedPreferences pref = getApplicationContext().getSharedPreferences("VolumeValue", 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();

        // set volume value from shared memory
        VolumeValue = pref.getInt("VolumeValue", -1);
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, VolumeValue, 0);

        viewPager.setAdapter(viewpagerAdapter);

        tableLayout.setupWithViewPager(viewPager);

        tableLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                /*create vibrate*/
                vibretor vibretor = new vibretor(500,getApplicationContext());
                switch (tab.getPosition()){

                    case 0:
                        speechService.textToSpeech("Currency detection camera open");
                        vibretor.execute();
                        textFragment.onPause();
                        currencyFragment.onStart();
                        break;
                    case 1:
                        speechService.textToSpeech("Text detection camera open");
                        /*start vibrate*/
                        vibretor.execute();
                        currencyFragment.onPause();
                        textFragment.onStart();
                        break;


                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


    }

    class ViewpagerAdapter extends FragmentPagerAdapter {


        private ArrayList<Fragment> fragments;
        private ArrayList<String> titles;

        ViewpagerAdapter(FragmentManager fm){
            super(fm);

            this.fragments = new ArrayList<>();
            this.titles = new ArrayList<>();
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
        public void addFragment(Fragment fragment,String title){
            fragments.add(fragment);
            titles.add(title);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }

    int Tab_Index = 0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){


            if(Tab_Index == 0) {
                tableLayout.getTabAt(1).select();
                Tab_Index++;
            }else {
                tableLayout.getTabAt(0).select();
                Tab_Index--;
            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}
