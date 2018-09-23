package mateuswetah.wearablebraille;

import android.content.Intent;
import android.content.pm.ActivityInfo;
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
import mateuswetah.wearablebraille.GestureDetectors.Swipe8DirectionsDetector;
import mateuswetah.wearablebraille.GestureDetectors.TwoFingersDoubleTapDetector;

/**
 * Created by orpheus on 15/11/17.
 */

public class ActivityTechSwipe extends WearableActivity{

    // View Components
    private BoxInsetLayout mContainerView;
    private BrailleDots brailleDots;
    private TextView tv1, tv2, tv3, resultLetter;
    private WearableActivity activity;

    // Touch Listeners
    TwoFingersDoubleTapDetector twoFingersListener;
    private GestureDetector gestureDetector;
    private View.OnClickListener dotClickListener;

    // Feedback Tools
    private TextToSpeech tts;

    //Flags
    boolean started = false;
    boolean stopped = true;
    boolean isStudy = false;
    boolean isScreenRotated = false;
    boolean swiped = false;
    boolean reset = false;

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

                // Swipe Gesture Detection
                gestureDetector = new GestureDetector(activity, new Swipe8DirectionsDetector() {
                    @Override
                    public void onTopLeftSwipe() {
                        brailleDots.toggleDotVisibility(0);
                    }

                    @Override
                    public void onTopRightSwipe() {
                        brailleDots.toggleDotVisibility(3);
                    }

                    @Override
                    public void onMiddleLeftSwipe() {
                        brailleDots.toggleDotVisibility(1);
                    }

                    @Override
                    public void onMiddleRightSwipe() {
                        brailleDots.toggleDotVisibility(4);
                    }

                    @Override
                    public void onBottomLeftSwipe() {
                        brailleDots.toggleDotVisibility(2);
                    }

                    @Override
                    public void onBottomRightSwipe() {
                        brailleDots.toggleDotVisibility(5);
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
    }

    void setTouchListener() {
        mContainerView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                twoFingersListener.onTouchEvent(event);
                if (gestureDetector.onTouchEvent(event)) {
                    swiped = true;
                }
                swiped = false;
                return false;
            }
        });
        mContainerView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (!swiped) {

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

                }
                return true;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.brailleDots.freeTTSService();
        tts.shutdown();
    }
}
