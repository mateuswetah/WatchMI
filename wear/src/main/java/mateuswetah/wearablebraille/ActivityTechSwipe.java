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
import android.support.wearable.view.BoxInsetLayout;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.view.textservice.SpellCheckerSession;
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
import mateuswetah.wearablebraille.GestureDetectors.Swipe8DirectionsDetector;
import mateuswetah.wearablebraille.GestureDetectors.TwoFingersDoubleTapDetector;
import mateuswetah.wearablebraille.GestureDetectors.TwoFingersSwipeDetector;

/**
 * Created by orpheus on 15/11/17.
 */

public class ActivityTechSwipe
        extends WearableTextEditorActivity {

    // View Components
    private DrawView drawView;

    // Touch Listeners
    TwoFingersDoubleTapDetector twoFingersListener;
    private GestureDetector gestureDetector;
    private View.OnClickListener dotClickListener;
    private View.OnLongClickListener dotLongClickListener;
    TwoFingersSwipeDetector twoFingersSwipeListener;
    private static final int DOUBLE_TAP_MAX_DIST = 40;

    //Flags
    private boolean swiped = false;

    // Test related
    int trialCount = 0;
    Util util;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Build and set view components
        WatchViewStub stub = (WatchViewStub) findViewById(R.id.stub);
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

        // Sets two finger double tap detector
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


    void setTouchListener() {
        drawView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                twoFingersListener.onTouchEvent(event);
                swiped = event.getActionMasked() == MotionEvent.ACTION_MOVE;
                gestureDetector.onTouchEvent(event);
                return false;
            }
        });
        drawView.setLongClickable(infoOnLongPress);

        drawView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (!swiped && infoOnLongPress) {
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
    }

}
