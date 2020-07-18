package com.dnn.vision.Settings;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

public class VibrationModule
{
    private int Seconds;
    Context mContext;
    public VibrationModule(int seconds, Context mContext){
        this.Seconds = seconds;
        this.mContext = mContext;
    }
    public void execute(){
        Vibrator v = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(this.Seconds, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            v.vibrate(this.Seconds);
        }
    }
}
