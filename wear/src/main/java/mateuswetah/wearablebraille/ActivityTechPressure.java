package mateuswetah.wearablebraille;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.util.Pair;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import mateuswetah.wearablebraille.BrailleÉcran.BrailleDots;
import mateuswetah.wearablebraille.BrailleÉcran.CharacterToSpeech;
import mateuswetah.wearablebraille.GestureDetectors.Swipe4DirectionsDetector;
import mateuswetah.wearablebraille.GestureDetectors.TwoFingersDoubleTapDetector;

public class ActivityTechPressure extends WearableActivity implements SensorEventListener {


    // View Components
    private BoxInsetLayout mContainerView;
    private DrawView drawView;
    private BrailleDots brailleDots;
    private TextView tv1, tv2, tv3, resultLetter;
    private WearableActivity activity;

    // Touch Listeners
    TwoFingersDoubleTapDetector twoFingersListener;
    private View.OnClickListener dotClickListener;

    // Sensors
    private SensorManager mSensorManager;
    private Sensor mSensor;

    // TextToSpeech for Feedbacks
    private CharacterToSpeech tts;

    //Flags
    boolean started = false;
    boolean stopped = true;
    boolean isStudy = false;
    boolean isScreenRotated = false;
    boolean speakWordAtSpace = false;
    boolean reset = false;
    boolean isActivated = false;

    // Test related
    int trialCount = 0;
    Util util;

    // Pressure Touch variables
    private float[] mRotationMatrix = new float[16];
    float[] rotValues = new float[3];
    double yaw, pitch, roll;
    float x = 0, y = 0, z = 0;
    float startX, startY, startZ;
    float diffX, diffY, diffZ;

    // Gesture detector for implementing Swipe gestures
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_braille_core_stub);

        // Checks if view is in test mode
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.getBoolean("study") == true) {
                isStudy = true;
                Toast.makeText(getApplicationContext(), "User study mode", Toast.LENGTH_SHORT).show();
            } else isStudy = false;

            if (extras.getBoolean("isScreenRotated") == true) {
                isScreenRotated = true;
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
            } else {
                isScreenRotated = false;
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }

            if (extras.getBoolean("speakWordAtSpace") == true)
                speakWordAtSpace = true;
            else
                speakWordAtSpace = false;
        }

        this.activity = this;

        // Sets TextToSpeech for Feedback
        tts = new CharacterToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                Log.d("TTS", "TextToSpeech Service Initialized");
                //tts.setLanguage(Locale.ENGLISH);
            }
        });

        WatchViewStub stub = (WatchViewStub) findViewById(R.id.stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mContainerView = (BoxInsetLayout) findViewById(R.id.container);
                drawView = (DrawView) findViewById(R.id.draw_view);
                mContainerView.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                    @Override
                    public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                        int mChinSize = insets.getSystemWindowInsetBottom();
                        v.onApplyWindowInsets(insets);
                        drawView.setShape(mContainerView.isRound(), mChinSize);
                        drawView.setDrawModes(DrawView.DrawModes.PRESSURE);
                        drawView.setStudyModes(isStudy);
                        return insets;
                    }
                });

                // Swipe Gesture Detection
                gestureDetector = new GestureDetector(activity, new Swipe4DirectionsDetector() {

                    @Override
                    public void onTopSwipe() {
                    }

                    @Override
                    public void onLeftSwipe() {

                    }

                    @Override
                    public void onRightSwipe() {

                    }

                    @Override
                    public void onBottomSwipe() {

                    }

                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        final String latinChar = brailleDots.checkCurrentCharacter(false, false, false, false);
                        Log.d("CHAR OUTPUT: ", latinChar);
                        tts.speak(latinChar, TextToSpeech.QUEUE_FLUSH, null, "Output");
                        resultLetter.setText(latinChar);

                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                resultLetter.setText("");
                                brailleDots.toggleAllDotsOff();
                            }
                        }, 1200);
                        return super.onDoubleTap(e);
                    }
                });
                setTouchListener();

                tv1 = (TextView) findViewById(R.id.tv1);
                tv2 = (TextView) findViewById(R.id.tv2);
                tv3 = (TextView) findViewById(R.id.tv3);
                resultLetter = (TextView) findViewById(R.id.resultLetter);

                if (!isStudy) {
                    //tv1.setText("PRESS on any side of screen (near the edge) with varying pressure!");
                    //tv3.setText("double tap with 2 fingers to exit");
                } else {
                    InitTrials();
                    SetNextTrial();
                    tv1.setText("Correct/Wrong");
                    tv3.setText("Trial:" + trialCount);
                }

                brailleDots = new BrailleDots(activity);

                // Associate OnClick and OnLongClick listeners to ButtonDots.
                for (int i = 0; i < brailleDots.ButtonDots.length; i++) {
                    brailleDots.ButtonDots[i].setClickable(false);
                }

            }
        });

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
        if (mSensor == null) {
            Log.w("YEO", "Failed to attach to game rot vec.");
            Toast.makeText(getApplicationContext(), "No game rotation vector", Toast.LENGTH_SHORT).show();
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            if (mSensor == null) {
                Log.w("YEO", "Failed to attach to rot vec.");
                Toast.makeText(getApplicationContext(), "No rotation vector also", Toast.LENGTH_SHORT).show();
            }
        }
//region Exit by two fingers double tap
        twoFingersListener = new TwoFingersDoubleTapDetector() {
            @Override
            public void onTwoFingersDoubleTap() {
//                Toast.makeText(getApplicationContext(), "Exit by Two Fingers Double Tap", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(getApplicationContext(), ActivitySelectTech.class);
                Bundle b = new Bundle();

                if (isStudy)
                    b.putBoolean("study", isStudy);

                b.putBoolean("isScreenRotated", isScreenRotated);
                i.putExtras(b);
                startActivity(i);
                finish();
            }
        };
//endregion
        util = new Util();
    }

    void setTouchListener() {
        mContainerView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                twoFingersListener.onTouchEvent(event);
                gestureDetector.onTouchEvent(event);

                float x = event.getX();
                float y = event.getY();

                int maskedAction = event.getActionMasked();
                switch (maskedAction) {
                    case MotionEvent.ACTION_DOWN:
                        started = true;
                        stopped = false;
                        touchPos = util.DetermineTouchPos(x, y, mContainerView.isRound());
                        if (touchPos == currentTrialPos)
                            drawView.strokeOrange.setColor(Color.argb(150, 0, 255, 0));
                        else drawView.strokeOrange.setColor(Color.argb(150, 255, 165, 0));
                        return true;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        return true;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        started = false;
                        stopped = true;
                        touchPos = 0;
                        drawView.strokeOrange.setColor(Color.argb(150, 255, 165, 0));
                        return true;
                }
                return false;
            }
        });
    }
//region Not important, onResume(), onPause(), onAccuracyChanged()
    @Override
    public void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
//endregion
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) { return; }

        if (event.sensor.getType() == Sensor.TYPE_GAME_ROTATION_VECTOR || event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(mRotationMatrix, event.values);
            SensorManager.getOrientation(mRotationMatrix, rotValues);
            yaw = rotValues[0];
            pitch = rotValues[1];
            roll = -rotValues[2];
            int inclination = (int) Math.round(Math.toDegrees(Math.acos(mRotationMatrix[10])));
            if (inclination > 90) {
                // faced down, when inclination near 90 (such as 85 or 95), roll is very sensitive
                // because it goes from 0 to pi suddenly
                pitch = -pitch;
            }
            x = -(float)roll;
            y = -(float)pitch;
            z = (float)yaw;

            if (started) {
                startX = x;
                startY = y;
                startZ = z;
                started = false; // only need get value once when touched down, don't constantly update
            }

            if (!stopped) {
                diffX = x - startX;
                diffY = y - startY;
                diffZ = z - startZ;

                if (isStudy)
                if (trialCount <= Constants.NUM_TRIALS)
                    Checking(diffX * Constants.MAGIC_XY, diffY * Constants.MAGIC_XY);

                drawView.setXYZ(diffX, diffY, diffZ);

                if (this.isViewContains(brailleDots.ButtonDots[0], diffX*Constants.MAGIC_XY + drawView.halfScreen, diffY*Constants.MAGIC_XY + drawView.halfScreen)) {
                    Log.d("BUTTON 0", "ENTER REGION!");
                    if (this.isActivated == false) {
                        brailleDots.toggleDotVisibility(0);
                        this.brailleDots.ButtonDots[0].callOnClick();
                        this.isActivated = true;
                    }
                } else if (this.isViewContains(brailleDots.ButtonDots[1], diffX*Constants.MAGIC_XY + drawView.halfScreen, diffY*Constants.MAGIC_XY + drawView.halfScreen)) {
                    Log.d("BUTTON 1", "ENTER REGION!");
                    if (this.isActivated == false) {
                        brailleDots.toggleDotVisibility(1);
                        this.brailleDots.ButtonDots[1].callOnClick();
                        this.isActivated = true;
                    }
                } else if (this.isViewContains(brailleDots.ButtonDots[2], diffX*Constants.MAGIC_XY + drawView.halfScreen, diffY*Constants.MAGIC_XY + drawView.halfScreen)) {
                    Log.d("BUTTON 2", "ENTER REGION!");
                    if (this.isActivated == false) {
                        brailleDots.toggleDotVisibility(2);
                        this.brailleDots.ButtonDots[2].callOnClick();
                        this.isActivated = true;
                    }
                } else if (this.isViewContains(brailleDots.ButtonDots[3], diffX*Constants.MAGIC_XY + drawView.halfScreen, diffY*Constants.MAGIC_XY + drawView.halfScreen)) {
                    Log.d("BUTTON 3", "ENTER REGION!");
                    if (this.isActivated == false) {
                        brailleDots.toggleDotVisibility(3);
                        this.brailleDots.ButtonDots[3].callOnClick();
                        this.isActivated = true;
                    }
                } else if (this.isViewContains(brailleDots.ButtonDots[4], diffX*Constants.MAGIC_XY + drawView.halfScreen, diffY*Constants.MAGIC_XY + drawView.halfScreen)) {
                    Log.d("BUTTON 4", "ENTER REGION!");
                    if (this.isActivated == false) {
                        brailleDots.toggleDotVisibility(4);
                        this.brailleDots.ButtonDots[4].callOnClick();
                        this.isActivated = true;
                    }
                } else if (this.isViewContains(brailleDots.ButtonDots[5], diffX*Constants.MAGIC_XY + drawView.halfScreen, diffY*Constants.MAGIC_XY + drawView.halfScreen)) {
                    Log.d("BUTTON 5", "ENTER REGION!");
                    if (this.isActivated == false) {
                        brailleDots.toggleDotVisibility(5);
                        this.brailleDots.ButtonDots[5].callOnClick();
                        this.isActivated = true;
                    }
                } else {
                    this.isActivated = false;
                }

                if (reset){
                    diffX = diffY = diffZ = 0;
                    drawView.setXYZ(0, 0, 0);
                    stopped = true;
                    reset = false;
                }
            }else {
                diffX = diffY = diffZ = 0;
            }
            if (drawView != null)
            drawView.invalidate();
        }
    }
    List<Pair<Integer,Integer>> trials = new ArrayList<>();
    void InitTrials(){
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 3; j++) {
                Pair<Integer, Integer> simplePair = new Pair<>(i, j);
                trials.add(simplePair);
            }
        }
        long seed = System.nanoTime();
        Collections.shuffle(trials, new Random(seed));
    }
    void SetNextTrial(){
        if (trialCount >= Constants.NUM_TRIALS) {
            Toast.makeText(getApplicationContext(), "THANKS " + trialCount, Toast.LENGTH_SHORT).show();
            tv2.setText("THANKS, GOODBYE");
        } else {
            currentTrialPos = trials.get(trialCount).first;
            currentTrialLvl = trials.get(trialCount).second;
            drawView.setTrial(currentTrialPos, currentTrialLvl);
            trialCount++;
        }
    }

    int touchPos = 0;
    int pos = 0, lvl = 0;
    int currentTrialPos = 0, currentTrialLvl = 0;
    int previousPos = 0, previousLvl = 0;
    long currentTime, startTime, askTime = 0;
    void Checking(float x, float y){
        double angle = (Math.toDegrees(Math.atan2(y, x)) + 360 + 90) % 360;
        pos = (int)((angle + 22.5)/ 45) + 1;
        if (pos == 9) pos = 1; // quick hack
        lvl = util.DetermineLevel(x, y, 40, 200);

        if (pos == currentTrialPos && lvl == currentTrialLvl && pos == touchPos)
            drawView.paintHighlightTask.setColor(Color.argb(150, 0, 255, 0));
        else drawView.paintHighlightTask.setColor(Color.argb(150, 255, 0, 0));

        if (lvl != 0){ // started?
            if (lvl != previousLvl || pos != previousPos){ // enter new box
                startTime = System.currentTimeMillis();
                previousLvl = lvl;
                previousPos = pos;
            } else {
                currentTime = System.currentTimeMillis();
                if (currentTime - startTime >= Constants.TIME_OUT) {
                    if (pos == currentTrialPos && lvl == currentTrialLvl && pos == touchPos){
                        tv1.setText("CORRECT");
                        SetNextTrial();
                    } else {
                        tv1.setText("WRONG");
                    }

                    drawView.paintHighlightTask.setColor(Color.argb(150, 255, 0, 0));
                    drawView.strokeOrange.setColor(Color.argb(150, 255, 165, 0));

                    pos = lvl = previousPos = previousLvl = touchPos = 0;
                    reset = true;
                    tv3.setText(trialCount + "/" + Constants.NUM_TRIALS + " Pos:" + currentTrialPos + "Lvl:" + currentTrialLvl);
                    askTime = System.currentTimeMillis();
                }
            }
        } else { // level = 0
            previousPos = previousLvl = 0;
        }
    }

    float alphaL = 0.2f;
    float alphaH = 0.2f;
    // simple low-pass filter
    float lowPass(float current, float last) {
        return alphaL * current + (1.0f - alphaL) * last;
    }

    // simple high-pass filter
    float highPass(float current, float last, float filtered) {
        return alphaH * (filtered + current - last);
    }

    private boolean isViewContains(View view, float px, float py) {

        int[] topLeftPoint = new int[2];
        view.getLocationOnScreen(topLeftPoint);
        int x = topLeftPoint[0];
        int y = topLeftPoint[1];

        int w = view.getWidth();
        int h = view.getHeight();

        switch (view.getId()) {

            case R.id.dotButton1:
                if (px > x + w || py > y + h) {
                    return false;
                }
                break;
            case R.id.dotButton4:
                if (px < x || py > y + h) {
                    return false;
                }
                break;
            case R.id.dotButton2:
                if (px > x + w || py < y || py > y + h) {
                    return false;
                }
                break;
            case R.id.dotButton5:
                if (px < x || py < y || py > y + h) {
                    return false;
                }
                break;
            case R.id.dotButton3:
                if (px > x + w || py < y ) {
                    return false;
                }
                break;
            case R.id.dotButton6:
                if (px < x || py < y) {
                    return false;
                }
                break;
        }

        return true;
    }

    @Override
    protected void onDestroy() {
        this.brailleDots.freeTTSService();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
