package com.example.vision;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;


public class RestartService extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            context.startForegroundService(new Intent(context, service.class));
//        } else {
            context.startService(new Intent(context, service.class));
        //}

    }
}
