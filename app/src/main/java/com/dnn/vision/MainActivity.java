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
import android.os.SystemClock;
import android.support.design.widget.TabLayout;
import android.os.Bundle;
import android.util.Size;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.dnn.vision.Fragment.CurrencyFragment;
import com.dnn.vision.Fragment.TextFragment;
import com.dnn.vision.Settings.VibrationModule;
import com.dnn.vision.R;
import com.dnn.vision.Utilities.BorderedText;
import com.dnn.vision.Utilities.ImageUtils;
import com.dnn.vision.customview.OverlayView;
import com.dnn.vision.tflite.Classifier;
import com.dnn.vision.tflite.TFLiteClassifier;
import com.dnn.vision.tracking.MultiBoxTracker;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.dnn.vision.Utilities.Logger;

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
    private static final Size DESIRED_PREVIEW_SIZE = new Size(1080, 2020);
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


    private TabLayout tableLayout;
    public int VolumeValue = 0;
    private SpeechService speechService;
    private CurrencyFragment currencyFragment;
    private TextFragment textFragment;

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
        VibrationModule VibrationModule = new VibrationModule(500, getApplicationContext());
        speechService = new SpeechService(this);

        if (id == R.id.setting_settings)
        {
            VibrationModule.execute();
            speechService.textToSpeech("Settings is open");
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.help_settings)
        {
            VibrationModule.execute();
            speechService.textToSpeech("Help is open");
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


//       tableLayout = findViewById(R.id.tab_layput);
//       ViewPager viewPager = findViewById(R.id.view_pager);
//       currencyFragment = new CurrencyFragment();
//       textFragment = new TextFragment();
//
//        ViewpagerAdapter viewpagerAdapter = new ViewpagerAdapter(getSupportFragmentManager());

        //start background BackgroundService
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            MainActivity.this.startForegroundService(new Intent(MainActivity.this, BackgroundService.class));
//        } else {
        startService(new Intent(MainActivity.this, BackgroundService.class));
        //}

//        viewpagerAdapter.addFragment(currencyFragment,"Currency");
//        viewpagerAdapter.addFragment(textFragment,"Text");

        //speech
        speechService = new SpeechService(this);
        speechService.textToSpeech("Application started");


        /*start vibrate*/
        VibrationModule VibrationModule = new VibrationModule(500, getApplicationContext());
        VibrationModule.execute();

        SharedPreferences pref = getApplicationContext().getSharedPreferences("VolumeValue", 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();

        // set volume value from shared memory
        VolumeValue = pref.getInt("VolumeValue", -1);
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, VolumeValue, 0);

//        viewPager.setAdapter(viewpagerAdapter);
//
//        tableLayout.setupWithViewPager(viewPager);
//
//        tableLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
//            @Override
//            public void onTabSelected(TabLayout.Tab tab) {
//                /*create vibrate*/
//                VibrationModule VibrationModule = new VibrationModule(500,getApplicationContext());
//                switch (tab.getPosition()){
//
//                    case 0:
//                        speechService.textToSpeech("Currency detection camera open");
//                        VibrationModule.execute();
//                        textFragment.onPause();
//                        currencyFragment.onStart();
//                        break;
//                    case 1:
//                        speechService.textToSpeech("Text detection camera open");
//                        /*start vibrate*/
//                        VibrationModule.execute();
//                        currencyFragment.onPause();
//                        textFragment.onStart();
//                        break;
//
//
//                }
//
//            }
//
//            @Override
//            public void onTabUnselected(TabLayout.Tab tab) {
//
//            }
//
//            @Override
//            public void onTabReselected(TabLayout.Tab tab) {
//
//            }
//        });


    }

//    class ViewpagerAdapter extends FragmentPagerAdapter
//    {
//
//
//        private ArrayList<Fragment> fragments;
//        private ArrayList<String> titles;
//
//        ViewpagerAdapter(FragmentManager fm)
//        {
//            super(fm);
//
//            this.fragments = new ArrayList<>();
//            this.titles = new ArrayList<>();
//        }
//
//        @Override
//        public Fragment getItem(int position)
//        {
//            return fragments.get(position);
//        }
//
//        @Override
//        public int getCount()
//        {
//            return fragments.size();
//        }
//
//        public void addFragment(Fragment fragment, String title)
//        {
//            fragments.add(fragment);
//            titles.add(title);
//        }
//
//        @Nullable
//        @Override
//        public CharSequence getPageTitle(int position)
//        {
//            return titles.get(position);
//        }
//    }

    int Tab_Index = 0;

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
        final float textSizePx =
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
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
            Toast toast =
                    Toast.makeText(
                            getApplicationContext(), "Classifier could not be initialized", Toast.LENGTH_SHORT);
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

        frameToCropTransform =
                ImageUtils.getTransformationMatrix(
                        previewWidth, previewHeight,
                        cropSize, cropSize,
                        sensorOrientation, MAINTAIN_ASPECT);

        cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);

        trackingOverlay = (OverlayView) findViewById(R.id.tracking_overlay);
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
        LOGGER.i("Preparing image " + currTimestamp + " for detection in bg thread.");

        rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);

        readyForNextImage();

        final Canvas canvas = new Canvas(croppedBitmap);
        canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);
        // For examining the actual TF input.
        if (SAVE_PREVIEW_BITMAP)
        {
            ImageUtils.saveBitmap(croppedBitmap);
        }

        runInBackground(
                new Runnable()
                {
                    @Override
                    public void run()
                    {
                        LOGGER.i("Running detection on image " + currTimestamp);
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

                        for (final Classifier.Recognition result : results)
                        {
                            final RectF location = result.getLocation();
                            if (location != null && result.getConfidence() >= minimumConfidence)
                            {
                                canvas.drawRect(location, paint);

                                cropToFrameTransform.mapRect(location);

                                result.setLocation(location);
                                mappedRecognitions.add(result);
                            }
                        }

                        tracker.trackResults(mappedRecognitions, currTimestamp);
                        trackingOverlay.postInvalidate();

                        computingDetection = false;

                    }
                });
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


}
