package com.dnn.vision.sensor;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.Toast;

import com.dnn.vision.SpeechService;

import static android.content.Context.SENSOR_SERVICE;

public class LightSensor implements SensorEventListener {
    private  SensorManager mSensorManager;
    private Sensor mLight;
    private Activity activity;
    public LightSensor(Activity mActivity){
        mSensorManager = (SensorManager)mActivity.getSystemService(SENSOR_SERVICE);
        mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        mSensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_NORMAL);
        activity = mActivity;
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        if( event.sensor.getType() == Sensor.TYPE_LIGHT)
        {
            Toast.makeText(activity.getApplicationContext(),event.values[0]+"",Toast.LENGTH_LONG).show();
            if(event.values[0] <= 5){
                SpeechService speechService = new SpeechService(activity);
                speechService.textToSpeech("Please double tap to flash on");
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
