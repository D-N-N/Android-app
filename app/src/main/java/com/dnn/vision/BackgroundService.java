package com.dnn.vision;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.dnn.vision.Utilities.Logger;

public class BackgroundService extends Service
{
    BroadcastReceiver mReceiver;
    IntentFilter filter;
    private static final Logger LOGGER = new Logger();

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {

        LOGGER.d("Background service started");
        return START_STICKY;
    }

    @Override
    public void onCreate()
    {
        filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_MEDIA_BUTTON);
        mReceiver = new EventReceiver();
        registerReceiver(mReceiver, filter);
        super.onCreate();
    }

    @Override
    public void onDestroy()
    {

        unregisterReceiver(mReceiver);
        super.onDestroy();

        Intent broadcastIntent = new Intent(this, RestartService.class);
        this.sendBroadcast(broadcastIntent);

        LOGGER.d("Background service destroyed");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent)
    {
        Intent intent = new Intent(this, RestartService.class);
        sendBroadcast(intent);
        super.onTaskRemoved(rootIntent);
    }


}