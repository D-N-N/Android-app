package com.dnn.vision;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

class EventReceiver extends BroadcastReceiver {


    EventReceiver()
    {
    }

    public void onReceive(final Context context, final Intent intent) {

        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                Intent i = new Intent(context, SplashScreenActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);

        }
    }
}
