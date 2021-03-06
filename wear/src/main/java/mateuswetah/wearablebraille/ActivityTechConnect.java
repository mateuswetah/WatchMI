package mateuswetah.wearablebraille;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
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
import android.view.ViewConfiguration;
import android.view.WindowInsets;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

import mateuswetah.wearablebraille.BrailleÉcran.BrailleDots;
import mateuswetah.wearablebraille.BrailleÉcran.CharacterToSpeech;
import mateuswetah.wearablebraille.BrailleÉcran.MyBoxInsetLayout;
import mateuswetah.wearablebraille.BrailleÉcran.WearableTextEditorActivity;
import mateuswetah.wearablebraille.GestureDetectors.OneFingerDoubleTapDetector;
import mateuswetah.wearablebraille.GestureDetectors.Swipe4DirectionsDetector;
import mateuswetah.wearablebraille.GestureDetectors.TwoFingersDoubleTapDetector;

/**
 * Created by orpheus on 15/11/17.
 */

public class ActivityTechConnect extends WearableTextEditorActivity {

    // Touch Listeners
    TwoFingersDoubleTapDetector twoFingersListener;
    GestureDetector doubleTapListener;

    //Flags
    private boolean checkOutput = false;
    private boolean isComposingLetter = false;
    private boolean preventLongPress = false;

    int lastTouchDownTime = 0;

    // Test related
    int trialCount = 0;
    Util util;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        // Double Tap listener, used for inserting space
        doubleTapListener = new GestureDetector(this.activity, new Swipe4DirectionsDetector() {
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
                        return super.onDoubleTap(event);
                }
                brailleDots.toggleAllDotsOff();
                confirmCharacter();
                checkOutput = false;
                isComposingLetter = false;

                return super.onDoubleTap(event);
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
                b.putBoolean("useWordReading", isUsingWordReading);
                b.putBoolean("useSpellCheck", isSpellChecking);
                i.putExtras(b);
                startActivity(i);
                finish();
            }
        };
    }

    void setTouchListener() {
        mContainerView.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View view) {

                if (!preventLongPress && !infoOnLongPress) {
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
                    return true;
                }
                return true;
            }
        });
        mContainerView.setLongClickable(true);
        mContainerView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
//                mContainerView.onTouchEvent(event);
                doubleTapListener.onTouchEvent(event);
                twoFingersListener.onTouchEvent(event);

                float x = event.getX();
                float y = event.getY();
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        vibrator.vibrate(40);
                        isComposingLetter = true;
                        preventLongPress = false;
                        touch_start(x, y);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        isComposingLetter = true;
                        preventLongPress = true;
                        touch_move(x, y);
                        break;
                    case MotionEvent.ACTION_UP:
                        preventLongPress = false;
                        if (isComposingLetter)
                            touch_up(false);
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        preventLongPress = false;
                        if (isComposingLetter)
                            touch_up(false);
                        break;
                    default: preventLongPress = false;
                }
                return true;
            }
        });
    }

    private void touch_start(float x, float y) {

        checkOutput = false;

        if (this.isViewContains(brailleDots.ButtonDots[0], x, y)) {
            if ((Boolean)(brailleDots.ButtonDots[0].getTag()) == false) {
                brailleDots.toggleDotVisibility(0);
                this.brailleDots.ButtonDots[0].callOnClick();
            }
        } else if (this.isViewContains(brailleDots.ButtonDots[1], x, y)) {
            //Log.d("BUTTON 1", "ENTER REGION!");
            if ((Boolean)(brailleDots.ButtonDots[1].getTag()) == false) {
                brailleDots.toggleDotVisibility(1);
                this.brailleDots.ButtonDots[1].callOnClick();
            }
        } else if (this.isViewContains(brailleDots.ButtonDots[2], x, y)) {
            //Log.d("BUTTON 2", "ENTER REGION!");
            if ((Boolean)(brailleDots.ButtonDots[2].getTag()) == false) {
                brailleDots.toggleDotVisibility(2);
                this.brailleDots.ButtonDots[2].callOnClick();
            }
        } else if (this.isViewContains(brailleDots.ButtonDots[3], x, y)) {
            //Log.d("BUTTON 3", "ENTER REGION!");
            if ((Boolean)(brailleDots.ButtonDots[3].getTag()) == false) {
                brailleDots.toggleDotVisibility(3);
                this.brailleDots.ButtonDots[3].callOnClick();
            }

        } else if (this.isViewContains(brailleDots.ButtonDots[4], x, y)) {
            //Log.d("BUTTON 4", "ENTER REGION!");
            if ((Boolean)(brailleDots.ButtonDots[4].getTag()) == false) {
                brailleDots.toggleDotVisibility(4);
                this.brailleDots.ButtonDots[4].callOnClick();
            }

        } else if (this.isViewContains(brailleDots.ButtonDots[5], x, y)) {
            //Log.d("BUTTON 5", "ENTER REGION!");
            if ((Boolean)(brailleDots.ButtonDots[5].getTag()) == false) {
                brailleDots.toggleDotVisibility(5);
                this.brailleDots.ButtonDots[5].callOnClick();
            }
        } else {
            isComposingLetter = false;
        }

    }
    private void touch_move(float x, float y)
    {
        //Log.d("MOVE", "MOVING");
        checkOutput = false;

        if (this.isViewContains(brailleDots.ButtonDots[0], x, y)) {
            //Log.d("BUTTON 0", "ENTER REGION!");
            if ((Boolean)(brailleDots.ButtonDots[0].getTag()) == false) {
                brailleDots.toggleDotVisibility(0);
                this.brailleDots.ButtonDots[0].callOnClick();
            }
        } else if (this.isViewContains(brailleDots.ButtonDots[1], x, y)) {
            //Log.d("BUTTON 1", "ENTER REGION!");
            if ((Boolean)(brailleDots.ButtonDots[1].getTag()) == false) {
                brailleDots.toggleDotVisibility(1);
                this.brailleDots.ButtonDots[1].callOnClick();
            }
        } else if (this.isViewContains(brailleDots.ButtonDots[2], x, y)) {
            //Log.d("BUTTON 2", "ENTER REGION!");
            if ((Boolean)(brailleDots.ButtonDots[2].getTag()) == false) {
                brailleDots.toggleDotVisibility(2);
                this.brailleDots.ButtonDots[2].callOnClick();
            }
        } else if (this.isViewContains(brailleDots.ButtonDots[3], x, y)) {
            //Log.d("BUTTON 3", "ENTER REGION!");
            if ((Boolean)(brailleDots.ButtonDots[3].getTag()) == false) {
                brailleDots.toggleDotVisibility(3);
                this.brailleDots.ButtonDots[3].callOnClick();
            }

        } else if (this.isViewContains(brailleDots.ButtonDots[4], x, y)) {
            //Log.d("BUTTON 4", "ENTER REGION!");
            if ((Boolean)(brailleDots.ButtonDots[4].getTag()) == false) {
                brailleDots.toggleDotVisibility(4);
                this.brailleDots.ButtonDots[4].callOnClick();
            }

        } else if (this.isViewContains(brailleDots.ButtonDots[5], x, y)) {
            //Log.d("BUTTON 5", "ENTER REGION!");
            if ((Boolean)(brailleDots.ButtonDots[5].getTag()) == false) {
                brailleDots.toggleDotVisibility(5);
                this.brailleDots.ButtonDots[5].callOnClick();
            }
        } else {
            isComposingLetter = false;
        }
    }
    private void touch_up(boolean skipTimeout) {
        checkOutput = true;

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                if (checkOutput) {
                    confirmCharacter();
                    checkOutput = false;
                    isComposingLetter = false;
                }
            }
        }, skipTimeout ? 0 : 1250);

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
}