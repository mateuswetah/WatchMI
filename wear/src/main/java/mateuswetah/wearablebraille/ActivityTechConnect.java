package mateuswetah.wearablebraille;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import mateuswetah.wearablebraille.BrailleÉcran.BrailleDots;

/**
 * Created by orpheus on 15/11/17.
 */

public class ActivityTechConnect extends WearableActivity {

    // View Components
    private BoxInsetLayout mContainerView;
    private BrailleDots brailleDots;
    private TextView tv1, tv2, tv3;
    private WearableActivity activity;

    // Connect the Dots components
    private boolean checkOutput = false;

    // Touch Listeners
    TwoFingersDoubleTapDetector twoFingersListener;
    private View.OnClickListener dotClickListener;

    // Vibrations generator for feedbacks
    private Vibrator vibrator;
    private TextToSpeech tts;

    //Flags
    boolean started = false;
    boolean stopped = true;
    boolean isStudy = false;
    boolean isScreenRotated = false;
    boolean reset = false;

    // Test related
    int trialCount = 0;
    Util util;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_braille_core_stub);
        this.activity = this;

        // Sets the Vibrator and TextToSpeech for feedback
        vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                Log.d("TTS", "TextToSpeech Service Initialized");
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
                setTouchListener();

                tv1 = (TextView) findViewById(R.id.tv1);
                tv2 = (TextView) findViewById(R.id.tv2);
                tv3 = (TextView) findViewById(R.id.tv3);

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
//                        tts.speak("1", TextToSpeech.QUEUE_FLUSH, "Mensagem do botão 1");
                                break;
                            case R.id.dotButton4:
                                vibrator.vibrate(new long[]{0,50,30,50,30,50,30,50},-1);
                                brailleDots.toggleDotVisibility(3);
//                        tts.speak("4", TextToSpeech.QUEUE_FLUSH, "Mensagem do botão 4");
                                break;
                            case R.id.dotButton2:
                                vibrator.vibrate(new long[]{0,50,30,50},-1);
                                brailleDots.toggleDotVisibility(1);
//                        tts.speak("2", TextToSpeech.QUEUE_FLUSH, "Mensagem do botão 2");
                                break;
                            case R.id.dotButton5:
                                vibrator.vibrate(new long[]{0,50,30,50,30,50,30,50,30,50},-1);
                                brailleDots.toggleDotVisibility(4);
//                        tts.speak("5", TextToSpeech.QUEUE_FLUSH, "Mensagem do botão 5");
                                break;
                            case R.id.dotButton3:
                                vibrator.vibrate(new long[]{0,50,30,50,30,50},-1);
                                brailleDots.toggleDotVisibility(2);
//                        tts.speak("3", TextToSpeech.QUEUE_FLUSH, "Mensagem do botão 3");
                                break;
                            case R.id.dotButton6:
                                vibrator.vibrate(new long[]{0,50,25,50,25,50,25,50,25,50},-1);
                                brailleDots.toggleDotVisibility(5);
//                        tts.speak("6", TextToSpeech.QUEUE_FLUSH, "Mensagem do botão 6");
                                break;
                        }


                    }
                };

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
            public boolean onTouch(View v, MotionEvent event) {
                twoFingersListener.onTouchEvent(event);

                float x = event.getX();
                float y = event.getY();

                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        touch_start(x, y);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        touch_move(x, y);
                        break;
                    case MotionEvent.ACTION_UP:
                        touch_up();
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        touch_up();
                        break;
                }
                return true;
            }
        });
    }

    private void touch_start(float x, float y) {

        checkOutput = false;

        if (this.isViewContains(brailleDots.ButtonDots[0], x, y)) {
            //Log.d("BUTTON 0", "ENTER REGION!");
            if ((Boolean)(brailleDots.ButtonDots[0].getTag()) == false) {
                vibrator.vibrate(100);
                brailleDots.toggleDotVisibility(0);
                this.brailleDots.ButtonDots[0].callOnClick();
            }
        } else if (this.isViewContains(brailleDots.ButtonDots[1], x, y)) {
            //Log.d("BUTTON 1", "ENTER REGION!");
            if ((Boolean)(brailleDots.ButtonDots[1].getTag()) == false) {
                vibrator.vibrate(100);
                brailleDots.toggleDotVisibility(1);
                this.brailleDots.ButtonDots[1].callOnClick();
            }
        } else if (this.isViewContains(brailleDots.ButtonDots[2], x, y)) {
            //Log.d("BUTTON 2", "ENTER REGION!");
            if ((Boolean)(brailleDots.ButtonDots[2].getTag()) == false) {
                vibrator.vibrate(100);
                brailleDots.toggleDotVisibility(2);
                this.brailleDots.ButtonDots[2].callOnClick();
            }
        } else if (this.isViewContains(brailleDots.ButtonDots[3], x, y)) {
            //Log.d("BUTTON 3", "ENTER REGION!");
            if ((Boolean)(brailleDots.ButtonDots[3].getTag()) == false) {
                vibrator.vibrate(100);
                brailleDots.toggleDotVisibility(3);
                this.brailleDots.ButtonDots[3].callOnClick();
            }

        } else if (this.isViewContains(brailleDots.ButtonDots[4], x, y)) {
            //Log.d("BUTTON 4", "ENTER REGION!");
            if ((Boolean)(brailleDots.ButtonDots[4].getTag()) == false) {
                vibrator.vibrate(100);
                brailleDots.toggleDotVisibility(4);
                this.brailleDots.ButtonDots[4].callOnClick();
            }

        } else if (this.isViewContains(brailleDots.ButtonDots[5], x, y)) {
            //Log.d("BUTTON 5", "ENTER REGION!");
            if ((Boolean)(brailleDots.ButtonDots[5].getTag()) == false) {
                vibrator.vibrate(100);
                brailleDots.toggleDotVisibility(5);
                this.brailleDots.ButtonDots[5].callOnClick();
            }

        }

    }
    private void touch_move(float x, float y)
    {
        Log.d("MOVE", "MOVING");
        checkOutput = false;

        if (this.isViewContains(brailleDots.ButtonDots[0], x, y)) {
            //Log.d("BUTTON 0", "ENTER REGION!");
            if ((Boolean)(brailleDots.ButtonDots[0].getTag()) == false) {
                vibrator.vibrate(100);
                brailleDots.toggleDotVisibility(0);
                this.brailleDots.ButtonDots[0].callOnClick();
            }
        } else if (this.isViewContains(brailleDots.ButtonDots[1], x, y)) {
            //Log.d("BUTTON 1", "ENTER REGION!");
            if ((Boolean)(brailleDots.ButtonDots[1].getTag()) == false) {
                vibrator.vibrate(100);
                brailleDots.toggleDotVisibility(1);
                this.brailleDots.ButtonDots[1].callOnClick();
            }
        } else if (this.isViewContains(brailleDots.ButtonDots[2], x, y)) {
            //Log.d("BUTTON 2", "ENTER REGION!");
            if ((Boolean)(brailleDots.ButtonDots[2].getTag()) == false) {
                vibrator.vibrate(100);
                brailleDots.toggleDotVisibility(2);
                this.brailleDots.ButtonDots[2].callOnClick();
            }
        } else if (this.isViewContains(brailleDots.ButtonDots[3], x, y)) {
            //Log.d("BUTTON 3", "ENTER REGION!");
            if ((Boolean)(brailleDots.ButtonDots[3].getTag()) == false) {
                vibrator.vibrate(100);
                brailleDots.toggleDotVisibility(3);
                this.brailleDots.ButtonDots[3].callOnClick();
            }

        } else if (this.isViewContains(brailleDots.ButtonDots[4], x, y)) {
            //Log.d("BUTTON 4", "ENTER REGION!");
            if ((Boolean)(brailleDots.ButtonDots[4].getTag()) == false) {
                vibrator.vibrate(100);
                brailleDots.toggleDotVisibility(4);
                this.brailleDots.ButtonDots[4].callOnClick();
            }

        } else if (this.isViewContains(brailleDots.ButtonDots[5], x, y)) {
            //Log.d("BUTTON 5", "ENTER REGION!");
            if ((Boolean)(brailleDots.ButtonDots[5].getTag()) == false) {
                vibrator.vibrate(100);
                brailleDots.toggleDotVisibility(5);
                this.brailleDots.ButtonDots[5].callOnClick();
            }

        }
    }
    private void touch_up()
    {
        Log.d("MOVE", "DONE");
        checkOutput = true;

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                if (checkOutput) {
                    Log.d("CHAR OUTPUT: ", brailleDots.checkCurrentCharacter(false, false, false, false));
                    tts.speak(brailleDots.checkCurrentCharacter(false, false, false, false), TextToSpeech.QUEUE_FLUSH, null, "Output");
                    brailleDots.toggleAllDotsOff();
                    checkOutput = false;
                }

            }
        }, 1250);

        // commit the path to our offscreen
        //mCanvas.drawPath(points, mPaint);
        //mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SCREEN));
        //mPaint.setMaskFilter(null);
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