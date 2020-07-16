package com.example.vision;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.security.Provider;

class Receiver extends BroadcastReceiver {


    private SharedPreferences prefAppStartKey;
    private  SharedPreferences.Editor editorAppStartKey;

    public void onReceive(final Context context, final Intent intent) {
        prefAppStartKey = context.getApplicationContext().getSharedPreferences("AppStartKey", 0); // 0 - for private mode
        editorAppStartKey = prefAppStartKey.edit();

//        Log.e("LOB", "Value "+prefAppStartKey.getInt("AppStartKey", 1)+"");

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {

            if(prefAppStartKey.getInt("AppStartKey", 0) == 1) {
                Intent i = new Intent(context, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }

        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            if(prefAppStartKey.getInt("AppStartKey", 0) == 0) {
                Intent i = new Intent(context, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }

        }
//        else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
//
//                Intent i = new Intent(context, MainActivity.class);
//                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                i.putExtra("powerOn",false);
//                context.startActivity(i);
//        }
    }
}
