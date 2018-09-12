package mateuswetah.wearablebraille;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.Locale;

import mateuswetah.wearablebraille.BrailleÉcran.BrailleDots;

/**
 * Created by orpheus on 15/11/17.
 */

public class ActivityTechTouch extends WearableActivity {

    // View Components
    private BoxInsetLayout mContainerView;
    private BrailleDots brailleDots;
    private TextView tv1, tv2, tv3, resultLetter;
    private WearableActivity activity;

    // Final Text
    private String message;

    // Touch Listeners
    TwoFingersDoubleTapDetector twoFingersListener;
    OneFingerDoubleTapDetector oneFingerListener;
    private View.OnClickListener dotClickListener;
    private View.OnLongClickListener dotLongClickListener;

    // Feedback Tools
    private TextToSpeech tts;

    //Flags
    boolean started = false;
    boolean stopped = true;
    boolean isStudy = false;
    boolean isScreenRotated = false;
    boolean reset = false;
    boolean isUsingWordReading = false;
    boolean isTTSInitialized = false;

    // Test related
    int trialCount = 0;
    Util util;

    // Gesture detector for implementing Swipe gestures
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_braille_core_stub);
        this.activity = this;

        // Sets TextToSpeech for feedback
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    Log.d("TTS", "TextToSpeech Service Initialized");
                    isTTSInitialized = true;
                    //tts.setLanguage(Locale.ENGLISH);
                }
            }
        });

        // Initializes message
        message = new String();

        // Checks if view is in test mode
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.getBoolean("study") == true) {
                isStudy = true;
                Toast.makeText(getApplicationContext(), "User study mode", Toast.LENGTH_SHORT).show();
            } else isStudy = false;

            if (extras.getBoolean("isScreenRotated")) {
                isScreenRotated = true;
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
            } else {
                isScreenRotated = false;
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }

            if (extras.getBoolean("useWordReading") == true)
                isUsingWordReading = true;
            else
                isUsingWordReading = false;
        }

        // Build and set view components
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mContainerView = (BoxInsetLayout) findViewById(R.id.container);

                mContainerView.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                    @Override
                    public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                        int mChinSize = insets.getSystemWindowInsetBottom();
                        v.onApplyWindowInsets(insets);
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
                });
                setTouchListener();

                tv1 = (TextView) findViewById(R.id.tv1);
                tv2 = (TextView) findViewById(R.id.tv2);
                tv3 = (TextView) findViewById(R.id.tv3);
                resultLetter = (TextView) findViewById(R.id.resultLetter);

                if (!isStudy) {
                    tv1.setText("");
                    tv3.setText("");
                } else {
                    //InitTrials();
                    //SetNextTrial();
                    tv1.setText("Correct/Wrong");
                    tv3.setText("Trial:" + trialCount);
                }

                // Instantiate braille buttons
                brailleDots = new BrailleDots(activity);

                dotClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        int currentButton = v.getId();

                        switch (currentButton) {

                            case R.id.dotButton1:
                                brailleDots.toggleDotVisibility(0);
                                break;
                            case R.id.dotButton4:
                                brailleDots.toggleDotVisibility(3);
                                break;
                            case R.id.dotButton2:
                                brailleDots.toggleDotVisibility(1);
                                break;
                            case R.id.dotButton5:
                                brailleDots.toggleDotVisibility(4);
                                break;
                            case R.id.dotButton3:
                                brailleDots.toggleDotVisibility(2);
                                break;
                            case R.id.dotButton6:
                                brailleDots.toggleDotVisibility(5);
                                break;
                        }
                    }
                };

                dotLongClickListener = new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {

                        int currentButton = v.getId();

                        switch (currentButton) {

                            case R.id.dotButton1:
                                brailleDots.toggleDotVisibility(0);
                                brailleDots.toggleDotVisibility(3);
                                break;
                            case R.id.dotButton4:
                                brailleDots.toggleDotVisibility(0);
                                brailleDots.toggleDotVisibility(3);
                                break;
                            case R.id.dotButton2:
                                brailleDots.toggleDotVisibility(1);
                                brailleDots.toggleDotVisibility(4);
                                break;
                            case R.id.dotButton5:
                                brailleDots.toggleDotVisibility(1);
                                brailleDots.toggleDotVisibility(4);
                                break;
                            case R.id.dotButton3:
                                brailleDots.toggleDotVisibility(2);
                                brailleDots.toggleDotVisibility(5);
                                break;
                            case R.id.dotButton6:
                                brailleDots.toggleDotVisibility(2);
                                brailleDots.toggleDotVisibility(5);
                                break;
                        }
                        return true;
                    }
                };

                // Associate OnClick and OnLongClick listeners to ButtonDots.
                for (int i = 0; i < brailleDots.ButtonDots.length; i++) {
                    brailleDots.ButtonDots[i].setOnClickListener(dotClickListener);
                    brailleDots.ButtonDots[i].setOnLongClickListener(dotLongClickListener);
                }
            }
        });

        // Sets one finger double tap detector
        oneFingerListener = new OneFingerDoubleTapDetector() {
            @Override
            public void onOneFingerDoubleTap() {
                final String latinChar = brailleDots.checkCurrentCharacter(false, false, false, false);
                Log.d("CHAR OUTPUT: ", latinChar);

                resultLetter.setText(latinChar);

                message = message.concat(latinChar);
                Log.d("MESSAGE OUTPUT: ", message);
                if (isTTSInitialized) {
                    if (isUsingWordReading || latinChar.equals(" "))
                        tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, "Output");
                    else
                        tts.speak(latinChar, TextToSpeech.QUEUE_FLUSH, null, "Output");
                }

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        resultLetter.setText("");
                        brailleDots.toggleAllDotsOff();
                    }
                }, 1200);
            }
        };

        // Sets two finger double tap detector
        twoFingersListener = new TwoFingersDoubleTapDetector() {
            @Override
            public void onTwoFingersDoubleTap() {
//                Toast.makeText(getApplicationContext(), "Exit by Two Fingers Double Tap", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(getApplicationContext(), ActivitySelectTech.class);
                Bundle b = new Bundle();

                if (isStudy)
                    b.putBoolean("study", isStudy);
                Log.d("SCREEN ROTATED", String.valueOf(isScreenRotated));
                b.putBoolean("isScreenRotated", isScreenRotated);
                b.putBoolean("useWordReading", isUsingWordReading);
                i.putExtras(b);
                startActivity(i);
                finish();
            }
        };
    }

    void setTouchListener() {
        mContainerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                twoFingersListener.onTouchEvent(event);
                oneFingerListener.onTouchEvent(event);
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_NAVIGATE_NEXT:
                // Do something that advances a user View to the next item in an ordered list.
                Log.d("FLICK Message", message);
                tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, "Output");

                return true;
            case KeyEvent.KEYCODE_NAVIGATE_PREVIOUS:
                // Do something that advances a user View to the previous item in an ordered list.
                Log.d("FLICK", "PREV");
                return true;
        }
        // If you did not handle it, let it be handled by the next possible element as deemed by the Activity.
        return super.onKeyDown(keyCode, event);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.brailleDots.freeTTSService();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }

}