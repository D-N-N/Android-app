package com.dnn.vision;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Size;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.dnn.vision.Settings.VibrationModule;
import com.dnn.vision.Utilities.BorderedText;
import com.dnn.vision.Utilities.ImageUtils;
import com.dnn.vision.Utilities.Logger;
import com.dnn.vision.Utilities.MultiBoxTracker;
import com.dnn.vision.customview.OverlayView;
import com.dnn.vision.tflite.Classifier;
import com.dnn.vision.tflite.TFLiteClassifier;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends CameraActivity
{

    private static final Logger LOGGER = new Logger();

    // Configuration values for the prepackaged SSD model.
    private static final int TF_OD_API_INPUT_SIZE = 300;
    private static final boolean TF_OD_API_IS_QUANTIZED = false;
    private static final String TF_OD_API_MODEL_FILE = "detect.tflite";
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/labelmap.txt";
    private static final DetectorMode MODE = DetectorMode.TF_OD_API;
    // Minimum detection confidence to track a detection.
    private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.8f;
    private static final boolean MAINTAIN_ASPECT = false;
    private static final Size DESIRED_PREVIEW_SIZE = new Size(480, 640);
    private static final boolean SAVE_PREVIEW_BITMAP = false;
    private static final float TEXT_SIZE_DIP = 10;
    OverlayView trackingOverlay;
    private Integer sensorOrientation;

    private Classifier detector;

    private long lastProcessingTimeMs;
    private Bitmap rgbFrameBitmap = null;
    private Bitmap croppedBitmap = null;
    private Bitmap cropCopyBitmap = null;

    private boolean computingDetection = false;

    private long timestamp = 0;

    private Matrix frameToCropTransform;
    private Matrix cropToFrameTransform;

    private MultiBoxTracker tracker;

    private BorderedText borderedText;
    public int VolumeValue = 0;
    private SpeechService speechService;
    private GestureDetector mDetector;
    protected VibrationModule vibrationModule;

    View.OnTouchListener touchListener = new View.OnTouchListener()
    {
        @Override
        public boolean onTouch(View v, MotionEvent event)
        {
            // pass the events to the gesture detector
            // a return value of true means the detector is handling it
            // a return value of false means the detector didn't
            // recognize the event
            return mDetector.onTouchEvent(event);

        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        vibrationModule = new VibrationModule(500, getApplicationContext());
        speechService = new SpeechService(this);

        if (id == R.id.setting_settings)
        {
            vibrationModule.execute();
            speechService.textToSpeech(getString(R.string.open_settings));
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.help_settings)
        {
            vibrationModule.execute();
            speechService.textToSpeech(getString(R.string.open_help));
            Intent intent = new Intent(MainActivity.this, HelpActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mDetector = new GestureDetector(this, new MyGestureListener());
        View rootView = findViewById(R.id.root_main);
        rootView.setOnTouchListener(touchListener);

        //start background BackgroundService
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            MainActivity.this.startForegroundService(new Intent(MainActivity.this, BackgroundService.class));
//        } else {
        startService(new Intent(MainActivity.this, BackgroundService.class));
        //}


        //speech
        speechService = new SpeechService(this);
        speechService.textToSpeech(getString(R.string.app_start));


        /*start vibrate*/
        vibrationModule = new VibrationModule(500, getApplicationContext());
        vibrationModule.execute();

        SharedPreferences pref = getApplicationContext().getSharedPreferences("VolumeValue", 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();

        // set volume value from shared memory
        VolumeValue = pref.getInt("VolumeValue", 5);
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, VolumeValue, 0);


    }


//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event)
//    {
//
//        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
//        {
//
//
//            if (Tab_Index == 0)
//            {
//                tableLayout.getTabAt(1).select();
//                Tab_Index++;
//            } else
//            {
//                tableLayout.getTabAt(0).select();
//                Tab_Index--;
//            }
//            return true;
//        }
//
//        return super.onKeyDown(keyCode, event);
//    }

    @Override
    public void onPreviewSizeChosen(final Size size, final int rotation)
    {
        final float textSizePx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
        borderedText = new BorderedText(textSizePx);
        borderedText.setTypeface(Typeface.MONOSPACE);

        tracker = new MultiBoxTracker(this);

        int cropSize = TF_OD_API_INPUT_SIZE;

        try
        {
            detector =
                    TFLiteClassifier.create(
                            getAssets(),
                            TF_OD_API_MODEL_FILE,
                            TF_OD_API_LABELS_FILE,
                            TF_OD_API_INPUT_SIZE,
                            TF_OD_API_IS_QUANTIZED);
            cropSize = TF_OD_API_INPUT_SIZE;
        } catch (final IOException e)
        {
            e.printStackTrace();
            LOGGER.e(e, "Exception initializing classifier!");
            Toast toast = Toast.makeText(getApplicationContext(), "Classifier could not be initialized", Toast.LENGTH_SHORT);
            toast.show();
            finish();
        }

        previewWidth = size.getWidth();
        previewHeight = size.getHeight();

        sensorOrientation = rotation - getScreenOrientation();
        LOGGER.i("Camera orientation relative to screen canvas: %d", sensorOrientation);

        LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);
        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888);
        croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Bitmap.Config.ARGB_8888);

        frameToCropTransform = ImageUtils.getTransformationMatrix(previewWidth, previewHeight, cropSize, cropSize, sensorOrientation, MAINTAIN_ASPECT);

        cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);

        trackingOverlay = findViewById(R.id.tracking_overlay);
        trackingOverlay.addCallback(
                new OverlayView.DrawCallback()
                {
                    @Override
                    public void drawCallback(final Canvas canvas)
                    {
                        tracker.draw(canvas);
                        if (isDebug())
                        {
                            tracker.drawDebug(canvas);
                        }
                    }
                });

        tracker.setFrameConfiguration(previewWidth, previewHeight, sensorOrientation);
    }

    @Override
    protected void processImage()
    {
        ++timestamp;
        final long currTimestamp = timestamp;
        trackingOverlay.postInvalidate();

        // No mutex needed as this method is not reentrant.
        if (computingDetection)
        {
            readyForNextImage();
            return;
        }
        computingDetection = true;
        LOGGER.d("Preparing image " + currTimestamp + " for detection.");

        rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);

        readyForNextImage();

        final Canvas canvas = new Canvas(croppedBitmap);
        canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);
        // For examining the actual TF input.
        if (SAVE_PREVIEW_BITMAP)
        {
            ImageUtils.saveBitmap(croppedBitmap);
        }
        computingDetection = false;

//        runInBackground(
//                new Runnable()
//                {
//                    @Override
//                    public void run()
//                    {
//                        LOGGER.i("Running detection on image " + currTimestamp);
//                        final long startTime = SystemClock.uptimeMillis();
//                        final List<Classifier.Recognition> results = detector.recognizeImage(croppedBitmap);
//                        lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;
//
//                        cropCopyBitmap = Bitmap.createBitmap(croppedBitmap);
//                        final Canvas canvas = new Canvas(cropCopyBitmap);
//                        final Paint paint = new Paint();
//                        paint.setColor(Color.RED);
//                        paint.setStyle(Paint.Style.STROKE);
//                        paint.setStrokeWidth(2.0f);
//
//                        float minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
//                        switch (MODE)
//                        {
//                            case TF_OD_API:
//                                minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
//                                break;
//                        }
//
//                        final List<Classifier.Recognition> mappedRecognitions =
//                                new LinkedList<Classifier.Recognition>();
//
//                        double maxConfidence = 0;
//                        Classifier.Recognition optimalPrediction = null;
//                        for (final Classifier.Recognition result : results)
//                        {
//
//                            double confidence = result.getConfidence();
//                            if (result.getLocation() != null && confidence >= minimumConfidence && confidence > maxConfidence)
//                            {
//                                optimalPrediction = result;
//
//                            }
//                        }
//                        if (optimalPrediction != null)
//                        {
//                            final RectF location = optimalPrediction.getLocation();
//                            canvas.drawRect(location, paint);
//                            cropToFrameTransform.mapRect(location);
//                            optimalPrediction.setLocation(location);
//                            mappedRecognitions.add(optimalPrediction);
//                            speechService.textToSpeech(getFinalCurrencyClass(optimalPrediction.getTitle()));
//                        }
//                        tracker.trackResults(mappedRecognitions, currTimestamp);
//                        trackingOverlay.postInvalidate();
//
//                        computingDetection = false;
//
//                    }
//                });
    }

    @Override
    protected int getLayoutId()
    {
        return R.layout.tracking;
    }

    @Override
    protected Size getDesiredPreviewFrameSize()
    {
        return DESIRED_PREVIEW_SIZE;
    }


    // Which detection model to use: by default uses Tensorflow Object Detection API frozen
    // checkpoints.
    private enum DetectorMode
    {
        TF_OD_API;
    }

    private String getFinalCurrencyClass(String result)
    {
        return result.substring(0, result.length() - 1) + " Rupees";
    }

    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener
    {

        @Override
        public boolean onDown(MotionEvent event)
        {
            LOGGER.d("onDown: ");

            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e)
        {
            vibrationModule.execute();
            final long startTime = SystemClock.uptimeMillis();
            final List<Classifier.Recognition> results = detector.recognizeImage(croppedBitmap);
            lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;

            cropCopyBitmap = Bitmap.createBitmap(croppedBitmap);
            final Canvas canvas = new Canvas(cropCopyBitmap);
            final Paint paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2.0f);

            float minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
            switch (MODE)
            {
                case TF_OD_API:
                    minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
                    break;
            }

            final List<Classifier.Recognition> mappedRecognitions =
                    new LinkedList<Classifier.Recognition>();

            double maxConfidence = 0;
            Classifier.Recognition optimalPrediction = null;
            for (final Classifier.Recognition result : results)
            {

                double confidence = result.getConfidence();
                if (result.getLocation() != null && confidence >= minimumConfidence && confidence > maxConfidence)
                {
                    optimalPrediction = result;

                }
            }
            if (optimalPrediction != null)
            {
                final RectF location = optimalPrediction.getLocation();
                canvas.drawRect(location, paint);
                cropToFrameTransform.mapRect(location);
                optimalPrediction.setLocation(location);
                mappedRecognitions.add(optimalPrediction);
                speechService.textToSpeech(getFinalCurrencyClass(optimalPrediction.getTitle()));
            }
            else
            {
                speechService.textToSpeech(getString(R.string.detection_failure_message));
            }
//            tracker.trackResults(mappedRecognitions, currTimestamp);
            trackingOverlay.postInvalidate();

//            computingDetection = false;
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e)
        {
            LOGGER.i("onLongPress: ");
        }

        @Override
        public boolean onDoubleTap(MotionEvent e)
        {
            LOGGER.d("Tapped");
            if(hasFlash)
            {
                flasher.toggleFlash();
            }
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY)
        {
            LOGGER.i("onScroll: ");
            return true;
        }

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2,
                               float velocityX, float velocityY)
        {
            vibrationModule.execute();
            speechService.textToSpeech(getString(R.string.open_settings));
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
    }
}


