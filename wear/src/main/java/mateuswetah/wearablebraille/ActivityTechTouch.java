package mateuswetah.wearablebraille;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
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
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.view.textservice.SpellCheckerSession;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import mateuswetah.wearablebraille.BrailleÉcran.ActivityAccessibleList;
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

public class ActivityTechTouch
        extends WearableTextEditorActivity {

    // View Components
    private DrawView drawView;

    // Touch Listeners
    TwoFingersDoubleTapDetector twoFingersListener;
    TwoFingersSwipeDetector twoFingersSwipeListener;
    private View.OnClickListener dotClickListener;
    private static final int DOUBLE_TAP_MAX_DIST = 40;

    // Test related
    int trialCount = 0;
    Util util;

    // Gesture detector for implementing Swipe gestures
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_braille_core_stub);

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
                        twoFingersListener.onTouchEvent(event);
                        if (!twoFingersSwipeListener.onTouchEvent(event) && !hasJustTwoFingerSwiped) {

                            int i;
                            boolean hasSimpleClicked = false;
                            if (event.getActionMasked() == MotionEvent.ACTION_UP) {
                                for (i = 0; i < brailleDots.ButtonDots.length; i++) {
                                    if (isViewContains(brailleDots.ButtonDots[i], event.getX(), event.getY())) {
                                        brailleDots.toggleDotVisibility(i);
                                        hasSimpleClicked = true;
                                        break;
                                    }
                                }
                            }
                            if (!hasSimpleClicked)
                                gestureDetector.onTouchEvent(event);

                        } else {
                            hasJustTwoFingerSwiped = true;
                            new Handler().postDelayed(
                                new Runnable() {
                                    public void run() {
                                        hasJustTwoFingerSwiped = false;
                                    }
                                }, 200);
                        }
                        return false;
                    }
                });
                drawView.setLongClickable(infoOnLongPress);

                drawView.setOnLongClickListener(new View.OnLongClickListener(){
                    @Override
                    public boolean onLongClick(View view) {
                        if (infoOnLongPress) {
                            Log.d("FULL MESSAGE OUTPUT: ", message);
                            if (message.length() > 0) {
                                tts.speak(getString(R.string.SendingFullSentence) + message, TextToSpeech.QUEUE_FLUSH, null, "Output info");
                            } else {
                                tts.speak(getString(R.string.EmptyMessage), TextToSpeech.QUEUE_FLUSH, null, "Output empty message info.");
                            }

                            vibrator.vibrate(300);

                            ArrayList activeButtons = new ArrayList();
                            for (int i = 0; i < brailleDots.ButtonDots.length; i++) {
                                if ((Boolean) brailleDots.ButtonDots[i].getTag())
                                    activeButtons.add(i);
                            }
                            String activeButtonsMessage = new String();
                            if (activeButtons.size() > 0) {
                                for (int i = 0; i < activeButtons.size(); i++) {
                                    activeButtonsMessage = activeButtonsMessage.concat(String.valueOf(activeButtons.get(i)));
                                    if (i <= activeButtons.size() - 3)
                                        activeButtonsMessage += ", ";
                                    else if (i > activeButtons.size() - 3 && i <= activeButtons.size() - 2)
                                        activeButtonsMessage += " e ";
                                    else
                                        activeButtonsMessage += ".";
                                }
                                Log.d("Extra message", activeButtonsMessage);
                                tts.speak(getString(R.string.ActivatedDots) + activeButtonsMessage, TextToSpeech.QUEUE_ADD, null, "Extra output info");
                            } else {
                                tts.speak(getString(R.string.NoActiveDots), TextToSpeech.QUEUE_FLUSH, null, "Output no active dots info.");
                            }
                        }
                        return true;
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
                    public boolean onDoubleTap(MotionEvent event) {
                        for (int i = 0; i < brailleDots.ButtonDots.length; i++) {
                            if (isViewContains(brailleDots.ButtonDots[i], event.getX(), event.getY()))
                                return super.onDoubleTapEvent(event);
                        }
                        confirmCharacter();
                        return super.onDoubleTap(event);
                    }
                });

                // Sets a special font for braille printing
                //Typeface typeFace = Typeface.createFromAsset(getAssets(),"font/visualbraille.ttf");
                //resultLetter.setTypeface(typeFace);

                // WatchMI tests related
                tv1 = (TextView) findViewById(R.id.tv1);
//                tv2 = (TextView) findViewById(R.id.tv2);
                tv3 = (TextView) findViewById(R.id.tv3);
//                if (!isStudy) {
                    tv1.setText("");
                    tv3.setText("");
//                } else {
//                    //InitTrials();
//                    //SetNextTrial();
//                    tv1.setText("Correct/Wrong");
//                    tv3.setText("Trial:" + trialCount);
//                }

                // Output latim letter
                resultLetter = (TextView) findViewById(R.id.resultLetter);

                // Instantiate braille buttons and dot listeners
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
                // Associate OnClick and OnLongClick listeners to ButtonDots.
                for (int i = 0; i < brailleDots.ButtonDots.length; i++) {
//                    brailleDots.ButtonDots[i].setOnClickListener(dotClickListener);
                }
            }
        });

        // Sets two finger swipe for deleting and accessing navigation mode
        twoFingersSwipeListener = new TwoFingersSwipeDetector() {
            @Override
            protected void onTwoFingersSwipeLeft() {
                if (message.length() > 0)
                    removeCharacter();
            }

            @Override
            protected void onTwoFingersSwipeRight() {
                enterNavigationMode();
            }
        };

        // Sets two finger double tap detector for exiting application
        twoFingersListener = new TwoFingersDoubleTapDetector() {
            @Override
            public void onTwoFingersDoubleTap() {
                Intent i = new Intent(getApplicationContext(), ActivitySelectTech.class);
                Bundle b = new Bundle();

                if (isStudy)
                    b.putBoolean("study", isStudy);
                Log.d("SCREEN ROTATED", String.valueOf(isScreenRotated));
                b.putBoolean("isScreenRotated", isScreenRotated);
                b.putBoolean("useWordReading", isUsingWordReading);
                b.putBoolean("useSpellCheck", isSpellChecking);
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
}