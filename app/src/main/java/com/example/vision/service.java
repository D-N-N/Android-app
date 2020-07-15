package com.example.vision;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.view.KeyEvent;
import android.widget.Toast;

public class service extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Service started by user.", Toast.LENGTH_LONG).show();

        final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_MEDIA_BUTTON);
        final BroadcastReceiver mReceiver = new Receiver();
        registerReceiver(mReceiver, filter);

        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();

        Intent broadcastIntent = new Intent(this, RestartService.class);
        sendBroadcast(broadcastIntent);

        Toast.makeText(this, "Service destroyed by user.", Toast.LENGTH_LONG).show();
    }


//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if(event.getAction() == KeyEvent.ACTION_DOWN) {
//
//            Intent dialogIntent = new Intent(this,MainActivity.class);
//            switch(keyCode) {
//                case KeyEvent.KEYCODE_HEADSETHOOK:
//                    dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(dialogIntent);
//                    return true;
//                case KeyEvent.KEYCODE_B:
//                    dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(dialogIntent);
//                    return true;
//
//                //etc.
//            }
//        }
//
//        return super.onKeyDown(keyCode, event);
//    }
}