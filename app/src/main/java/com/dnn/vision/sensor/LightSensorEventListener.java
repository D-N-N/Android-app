package com.dnn.vision.sensor;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;


import com.dnn.vision.R;
import com.dnn.vision.SpeechService;

import static android.content.Context.SENSOR_SERVICE;

public class LightSensorEventListener implements SensorEventListener
{
    private Activity activity;
    private boolean isWarningsEnabled = true;
    private HandlerThread backgroundThread;
    private Handler backgroundHandler;

    public boolean getWarningStatus()
    {
        return isWarningsEnabled;
    }

    public void setWarningsStatus(boolean warningsStatus)
    {
        this.isWarningsEnabled = warningsStatus;
    }


    public LightSensorEventListener(Activity mActivity)
    {
        SensorManager mSensorManager = (SensorManager) mActivity.getSystemService(SENSOR_SERVICE);
        Sensor mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        startBackgroundThread();
        mSensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_NORMAL,backgroundHandler);
        activity = mActivity;
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        if (event.sensor.getType() == Sensor.TYPE_LIGHT)
        {

            if (event.values[0] <= 5 && isWarningsEnabled)
            {
                try
                {
                    Thread.sleep(3000);
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                SpeechService speechService = new SpeechService(activity);
                speechService.textToSpeech(activity.getString(R.string.flash_on_command));
                isWarningsEnabled = false;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {

    }


    private void startBackgroundThread()
    {
        backgroundThread = new HandlerThread("ImageListener");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }
}
