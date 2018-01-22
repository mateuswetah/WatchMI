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
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.widget.TextView;
import android.widget.Toast;

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

    // Touch Listeners
    TwoFingersDoubleTapDetector twoFingersListener;
    private View.OnClickListener dotClickListener;

    // Feedback Tools
    private Vibrator vibrator;
    private TextToSpeech tts;
    private ToneGenerator toneGenerator;

    //Flags
    boolean started = false;
    boolean stopped = true;
    boolean isStudy = false;
    boolean isScreenRotated = false;
    boolean reset = false;
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

        // Sets the Vibrator, TextToSpeech and ToneGenerator for Feedback
        vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
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
        toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME);

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
                        final String latinChar = brailleDots.checkCurrentCharacter(false, false, false, false);
                        Log.d("CHAR OUTPUT: ", latinChar);
                        if (isTTSInitialized){
                            tts.speak(latinChar, TextToSpeech.QUEUE_FLUSH, null, "Output");
                        }
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
                                vibrator.vibrate(new long[]{0,50}, -1);
                                toneGenerator.startTone(ToneGenerator.TONE_DTMF_1, 100);
//                            tts.speak("1", TextToSpeech.QUEUE_FLUSH, "Mensagem do botão 1");
                                break;
                            case R.id.dotButton4:
                                vibrator.vibrate(new long[]{0,50,30,50,30,50,30,50},-1);
                                brailleDots.toggleDotVisibility(3);
                                toneGenerator.startTone(ToneGenerator.TONE_DTMF_4, 100);
//                        tts.speak("4", TextToSpeech.QUEUE_FLUSH, "Mensagem do botão 4");
                                break;
                            case R.id.dotButton2:
                                vibrator.vibrate(new long[]{0,50,30,50},-1);
                                brailleDots.toggleDotVisibility(1);
                                toneGenerator.startTone(ToneGenerator.TONE_DTMF_2, 100);
//                        tts.speak("2", TextToSpeech.QUEUE_FLUSH, "Mensagem do botão 2");
                                break;
                            case R.id.dotButton5:
                                vibrator.vibrate(new long[]{0,50,30,50,30,50,30,50,30,50},-1);
                                brailleDots.toggleDotVisibility(4);
                                toneGenerator.startTone(ToneGenerator.TONE_DTMF_5, 100);
//                        tts.speak("5", TextToSpeech.QUEUE_FLUSH, "Mensagem do botão 5");
                                break;
                            case R.id.dotButton3:
                                vibrator.vibrate(new long[]{0,50,30,50,30,50},-1);
                                brailleDots.toggleDotVisibility(2);
                                toneGenerator.startTone(ToneGenerator.TONE_DTMF_3, 100);
//                        tts.speak("3", TextToSpeech.QUEUE_FLUSH, "Mensagem do botão 3");
                                break;
                            case R.id.dotButton6:
                                vibrator.vibrate(new long[]{0,50,25,50,25,50,25,50,25,50},-1);
                                brailleDots.toggleDotVisibility(5);
                                toneGenerator.startTone(ToneGenerator.TONE_DTMF_6, 100);
//                        tts.speak("6", TextToSpeech.QUEUE_FLUSH, "Mensagem do botão 6");
                                break;
                        }


                    }
                };

                // Associate OnClick and OnLongClick listeners to ButtonDots.
                for (int i = 0; i < brailleDots.ButtonDots.length; i++) {
                    brailleDots.ButtonDots[i].setOnClickListener(dotClickListener);
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
                Log.d("SCREEN ROTATED", String.valueOf(isScreenRotated));
                b.putBoolean("isScreenRotated", isScreenRotated);
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
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }
}