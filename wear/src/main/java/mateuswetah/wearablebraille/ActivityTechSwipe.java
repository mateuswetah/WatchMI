package mateuswetah.wearablebraille;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
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
import mateuswetah.wearablebraille.BrailleÉcran.CharacterToSpeech;
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
    private DrawView drawView;

    // Touch Listeners
    TwoFingersDoubleTapDetector twoFingersListener;
    private GestureDetector gestureDetector;
    private View.OnClickListener dotClickListener;

    // Feedback Tools
    private CharacterToSpeech tts;

    //Flags
    boolean started = false;
    boolean stopped = true;
    boolean isStudy = false;
    boolean isScreenRotated = false;
    boolean speakWordAtSpace = false;
    boolean infoOnLongPress = false;
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
        tts = new CharacterToSpeech(this, new TextToSpeech.OnInitListener() {
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

            if (extras.getBoolean("speakWordAtSpace") == true)
                speakWordAtSpace = true;
            else
                speakWordAtSpace = false;


            if (extras.getBoolean("infoOnLongPress") == true)
                infoOnLongPress = true;
            else
                infoOnLongPress = false;
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

                drawView = (DrawView) findViewById(R.id.draw_view);
                drawView.setBackgroundColor(Color.TRANSPARENT);

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

                    @Override
                    public boolean onDoubleTap(MotionEvent event) {
                        int i;
                        for (i = 0; i < brailleDots.ButtonDots.length; i++) {
                            if (isViewContains(brailleDots.ButtonDots[i], event.getX(), event.getY()))
                                return super.onDoubleTapEvent(event);
                        }
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
                        return super.onDoubleTap(event);
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

    private boolean isViewContains(View view, float px, float py) {

        int[] topLeftPoint = new int[2];
        view.getLocationOnScreen(topLeftPoint);
        int x = topLeftPoint[0];
        int y = topLeftPoint[1];

        int w = view.getWidth();
        int h = view.getHeight();

        if (px >= 0 && py >= 0) {
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
                    if (px > x + w || py < y) {
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
        } else {
            return false;
        }
    }


    void setTouchListener() {
        drawView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                twoFingersListener.onTouchEvent(event);
                swiped = event.getActionMasked() == MotionEvent.ACTION_MOVE;
                gestureDetector.onTouchEvent(event);
                return false;
            }
        });
        drawView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (!swiped && infoOnLongPress) {

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
                return false;
            }
        });
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
