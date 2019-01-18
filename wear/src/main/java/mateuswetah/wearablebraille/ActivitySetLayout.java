package mateuswetah.wearablebraille;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import mateuswetah.wearablebraille.BrailleÉcran.BrailleDots;
import mateuswetah.wearablebraille.BrailleÉcran.CharacterToSpeech;
import mateuswetah.wearablebraille.BrailleÉcran.MyBoxInsetLayout;
import mateuswetah.wearablebraille.BrailleÉcran.WearableTextEditorActivity;
import mateuswetah.wearablebraille.GestureDetectors.Swipe4DirectionsDetector;
import mateuswetah.wearablebraille.GestureDetectors.TwoFingersDoubleTapDetector;
import mateuswetah.wearablebraille.GestureDetectors.TwoFingersSwipeDetector;

/**
 * Created by orpheus on 15/11/17.
 */

public class ActivitySetLayout
        extends WearableActivity {

    // View Components
    private MyBoxInsetLayout mContainerView;
    private BrailleDots brailleDots;
    private DrawView drawView;
    private WearableActivity activity;
    private TextView resultLetter, tv1;

    // Touch Listeners
    Util util;

    // Final layout order
    String layoutOrder = "";

    // Feedback Tools
    ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME);
    Vibrator vibrator = null;
    CharacterToSpeech tts;
    boolean isTTSInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_braille_core_stub);
        this.activity = this;

        // Initializes util, to get touch area relation with buttons
        util = new Util();

        // Build and set view components
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mContainerView = (MyBoxInsetLayout) findViewById(R.id.container);
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

                drawView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent event) {
                        int i;
                        for (i = 0; i < brailleDots.ButtonDots.length; i++) {

                            // Checks if clicked region is a corner
                            if (isViewContains(brailleDots.ButtonDots[i], event.getX(), event.getY())) {
                                brailleDots.setDotVisibility(i, true);

                                // Checks if corner is already being used before adding it
                                if (layoutOrder.indexOf(String.valueOf(i + 1)) < 0) {
                                    layoutOrder = layoutOrder + String.valueOf(i + 1);
                                    tv1.setText(layoutOrder);

                                    vibrator.vibrate(200);
                                    if (isTTSInitialized) {
                                        switch (i) {
                                            case 0:
                                                tts.speak(getString(R.string.topLeftCorner), TextToSpeech.QUEUE_ADD, null, "topLeftCornerSpeech");
                                                break;
                                            case 1:
                                                tts.speak(getString(R.string.middleLeftCorner), TextToSpeech.QUEUE_ADD, null, "middleLeftCornerSpeech");
                                                break;
                                            case 2:
                                                tts.speak(getString(R.string.bottomLeftCorner), TextToSpeech.QUEUE_ADD, null, "bottomLeftCornerSpeech");
                                                break;
                                            case 3:
                                                tts.speak(getString(R.string.topRightCorner), TextToSpeech.QUEUE_ADD, null, "topRightCornerSpeech");
                                                break;
                                            case 4:
                                                tts.speak(getString(R.string.middleRightCorner), TextToSpeech.QUEUE_ADD, null, "middleRightCornerSpeech");
                                                break;
                                            case 5:
                                                tts.speak(getString(R.string.bottomRightCorner), TextToSpeech.QUEUE_ADD, null, "bottomRightCornerSpeech");
                                                break;
                                        }
                                    }

                                    switch (layoutOrder.length()) {
                                        case 1:
                                            if (isTTSInitialized) { tts.speak(getString(R.string.setDot2Instruction), TextToSpeech.QUEUE_ADD, null, "setDot2InstructionSpeech"); }
                                            break;
                                        case 2:
                                            if (isTTSInitialized) { tts.speak(getString(R.string.setDot3Instruction), TextToSpeech.QUEUE_ADD, null, "setDot3InstructionSpeech"); }
                                            break;
                                        case 3:
                                            if (isTTSInitialized) { tts.speak(getString(R.string.setDot4Instruction), TextToSpeech.QUEUE_ADD, null, "setDot4InstructionSpeech"); }
                                            break;
                                        case 4:
                                            if (isTTSInitialized) { tts.speak(getString(R.string.setDot5Instruction), TextToSpeech.QUEUE_ADD, null, "setDot5InstructionSpeech"); }
                                            break;
                                        case 5:
                                            if (isTTSInitialized) { tts.speak(getString(R.string.setDot6Instruction), TextToSpeech.QUEUE_ADD, null, "setDot6InstructionSpeech"); }
                                            break;
                                    }

                                    // Checks if this is the last corner, to finish layout setting
                                    if (layoutOrder.length() == brailleDots.ButtonDots.length) {
                                        brailleDots.setLayoutOrder(layoutOrder);
                                        layoutOrder = "";
                                        brailleDots.toggleOnlyDotImagesOff();
                                        brailleDots.freeTTSService();

                                        if (isTTSInitialized) {
                                            tts.speak(getString(R.string.layoutSettingFinishedSpeech), TextToSpeech.QUEUE_ADD, null, "layoutSettingFinishedSpeech");
                                        }
                                    }

                                    break;

                                } else {
                                    vibrator.vibrate(400);

                                    if (isTTSInitialized) {
                                        tts.speak(getString(R.string.alreadyDefinedCornerSpeech), TextToSpeech.QUEUE_ADD, null, "alreadyDefinedCornerSpeech");
                                    }
                                    break;
                                }

                            } else {
                                if (i == brailleDots.ButtonDots.length - 1) {
//                                    resultLetter.setText("");
                                    vibrator.vibrate(600);

                                    if (isTTSInitialized) {
                                        tts.speak(getString(R.string.regionWithoutDotsSpeech), TextToSpeech.QUEUE_ADD, null, "noRegionSpeech");
                                    }
                                }
                            }
                        }
                        return false;
                    }
                });

                // Instantiate braille buttons and dot listeners
                brailleDots = new BrailleDots(activity);

                // Output latim letter
                resultLetter = (TextView) findViewById(R.id.resultLetter);
                tv1 = (TextView) findViewById(R.id.tv1);
                tv1.setText(brailleDots.getLayoutOrder());

            }
        });

        // Sets TextToSpeech for feedback
        tts = new CharacterToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    Log.d("TTS", "TextToSpeech Service Initialized");
                    isTTSInitialized = true;
                    Log.d("TTS Voices", tts.getVoices().toString());
                    tts.setLanguage(Locale.getDefault());
                    tts.setOnUtteranceCompletedListener(new TextToSpeech.OnUtteranceCompletedListener() {
                        @Override
                        public void onUtteranceCompleted(String s) {

                            if (s.equals("layoutSettingFinishedSpeech")) {
                                Intent intent = new Intent(getApplicationContext(), ActivitySelectTech.class);
                                Bundle b = new Bundle();
                                intent.putExtras(b);
                                startActivity(intent);
                                finish();
                            }
                        }
                    });

                    tts.speak(getString(R.string.setLayoutIntro), TextToSpeech.QUEUE_ADD, null, "setLayoutIntroSpeech");
                    tts.speak(getString(R.string.setDot1Instruction), TextToSpeech.QUEUE_ADD, null, "setDot1InstructionSpeech");
                }
            }
        });
        vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
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