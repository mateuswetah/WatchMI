package mateuswetah.wearablebraille;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.widget.TextView;
import android.widget.Toast;

import mateuswetah.wearablebraille.BrailleÉcran.BrailleDots;
import mateuswetah.wearablebraille.GestureDetectors.SerialTapDetector;
import mateuswetah.wearablebraille.GestureDetectors.Swipe4DirectionsDetector;
import mateuswetah.wearablebraille.GestureDetectors.TwoFingersDoubleTapDetector;

/**
 * Created by orpheus on 15/11/17.
 */

public class ActivityTechSerial extends WearableActivity{

    // View Components
    private BoxInsetLayout mContainerView;
    private BrailleDots brailleDots;
    private TextView tv1, tv2, tv3, resultLetter;
    private WearableActivity activity;

    // Touch Listeners
    TwoFingersDoubleTapDetector twoFingersListener;
    private GestureDetector gestureDetector;
    private View.OnClickListener dotClickListener;
    private SerialTapDetector serialTapDetector;

    // Feedback Tools
    private TextToSpeech tts;

    //Flags
    boolean started = false;
    boolean stopped = true;
    boolean isStudy = false;
    boolean isScreenRotated = false;
    boolean reset = false;
    boolean onResultExibition = false;

    // Serial Input Control
    int serialLine = 0;
    float windowWidth;

    // Test related
    int trialCount = 0;
    Util util;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_braille_core_stub);
        this.activity = this;

        // Sets TextToSpeech for Feedback
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                Log.d("TTS", "TextToSpeech Service Initialized");
                //tts.setLanguage(Locale.ENGLISH);
            }
        });

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
        }

        // Build and set view components
        WatchViewStub stub = (WatchViewStub) findViewById(R.id.stub);
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

                // Initialize Serial Line
                activity.findViewById(R.id.serial_line_1).setBackgroundResource(R.drawable.braille_ecran_button);
                activity.findViewById(R.id.serial_line_2).setBackground(null);
                activity.findViewById(R.id.serial_line_3).setBackground(null);

                // Swipe Gesture Detection
                gestureDetector = new GestureDetector(activity, new Swipe4DirectionsDetector() {
                    @Override
                    public void onTopSwipe() {
                        // Move to next Serial Line
                        incrementSerialLine();
                    }

                    @Override
                    public void onLeftSwipe() {
                    }

                    @Override
                    public void onBottomSwipe() {
                    }

                    @Override
                    public void onRightSwipe() {
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

                // Associate OnClick and OnLongClick listeners to ButtonDots.
                for (int i = 0; i < brailleDots.ButtonDots.length; i++) {
                    brailleDots.ButtonDots[i].setClickable(false);
                }
            }
        });

        // Sets two finger double tap detector
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

        // Obtain Windows dimensions for SerialTapDetector
        Point dimensions = new Point();
        getWindowManager().getDefaultDisplay().getSize(dimensions);
        windowWidth = dimensions.x;

        serialTapDetector = new SerialTapDetector(windowWidth) {

            @Override
            public void onSerialDoubleTap() {
                switch (serialLine) {
                    case 0:
                        brailleDots.setDotVisibility(0, true);
                        brailleDots.setDotVisibility(3, true);
                        break;
                    case 1:
                        brailleDots.setDotVisibility(1, true);
                        brailleDots.setDotVisibility(4, true);
                        break;
                    case 2:
                        brailleDots.setDotVisibility(2,true);
                        brailleDots.setDotVisibility(5, true);
                        break;
                }
                Log.d("SERIAL", "DOUBLE TAP");

                // Move to next Serial Line
                incrementSerialLine();
            }

            @Override
            public void onSerialSingleTap(boolean isRightSide) {

                if (isRightSide) {
                    switch (serialLine) {
                        case 0:
                            brailleDots.setDotVisibility(3, true);
                            break;
                        case 1:
                            brailleDots.setDotVisibility(4, true);
                            break;
                        case 2:
                            brailleDots.setDotVisibility(5,true);
                            break;
                    }
                    Log.d("SERIAL", "SINGLE TAP RIGHT");
                } else {
                    switch (serialLine) {
                        case 0:
                            brailleDots.setDotVisibility(0,true);
                            break;
                        case 1:
                            brailleDots.setDotVisibility(1, true);
                            break;
                        case 2:
                            brailleDots.setDotVisibility(2,true);
                            break;
                    }
                    Log.d("SERIAL", "SINGLE TAP LEFT");
                }
                // Move to next Serial Line
                incrementSerialLine();
            }
        };
    }

    void setTouchListener() {
        mContainerView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (!onResultExibition) {
                    twoFingersListener.onTouchEvent(event);
                    if (!gestureDetector.onTouchEvent(event))
                        serialTapDetector.onTouchEvent(event);
                }
                return true;
            }
        });
    }

    void incrementSerialLine() {

        serialLine = (serialLine + 1) % 3;

        switch (serialLine){
            case 0:
                activity.findViewById(R.id.serial_line_1).setBackground(null);
                activity.findViewById(R.id.serial_line_2).setBackground(null);
                activity.findViewById(R.id.serial_line_3).setBackground(null);

                //Processes Braille Character
                final String latinChar = brailleDots.checkCurrentCharacter(false, false, false, false);
                Log.d("CHAR OUTPUT: ", latinChar);
                resultLetter.setText(latinChar);
                tts.speak(latinChar, TextToSpeech.QUEUE_FLUSH, null, "Output");
                onResultExibition = true;

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        activity.findViewById(R.id.serial_line_1).setBackgroundResource(R.drawable.braille_ecran_button);
                        activity.findViewById(R.id.serial_line_2).setBackground(null);
                        activity.findViewById(R.id.serial_line_3).setBackground(null);
                        brailleDots.toggleAllDotsOff();
                        resultLetter.setText("");
                        onResultExibition = false;
                    }
                }, 1300);
                break;
            case 1:
                activity.findViewById(R.id.serial_line_2).setBackgroundResource(R.drawable.braille_ecran_button);
                activity.findViewById(R.id.serial_line_1).setBackground(null);
                activity.findViewById(R.id.serial_line_3).setBackground(null);
                break;
            case 2:
                activity.findViewById(R.id.serial_line_3).setBackgroundResource(R.drawable.braille_ecran_button);
                activity.findViewById(R.id.serial_line_2).setBackground(null);
                activity.findViewById(R.id.serial_line_1).setBackground(null);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.brailleDots.freeTTSService();
        tts.shutdown();
    }
}
