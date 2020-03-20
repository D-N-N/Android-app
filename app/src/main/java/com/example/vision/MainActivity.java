package com.example.vision;

import android.content.Intent;
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
import android.widget.TableLayout;
import android.widget.Toast;

import com.example.vision.Fragment.CurrencyFragment;
import com.example.vision.Fragment.TextFragment;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private TabLayout tableLayout;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.setting_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }else if(id == R.id.help_settings){

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
       final CurrencyFragment currencyFragment = new CurrencyFragment();
       final TextFragment textFragment = new TextFragment();

        ViewpagerAdapter viewpagerAdapter = new ViewpagerAdapter(getSupportFragmentManager());

        viewpagerAdapter.addFragment(currencyFragment,"Currency");
        viewpagerAdapter.addFragment(textFragment,"Text");


        viewPager.setAdapter(viewpagerAdapter);

        tableLayout.setupWithViewPager(viewPager);

        tableLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Toast.makeText(getApplicationContext(),tab.getPosition()+"",Toast.LENGTH_LONG).show();
                switch (tab.getPosition()){

                    case 0:

                        textFragment.onPause();
                        currencyFragment.onStart();
                        break;
                    case 1:
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

//        String text = "hello world";
//        SpeechService sp = new SpeechService(this);
//        sp.textToSpeech(text);



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
