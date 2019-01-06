package mateuswetah.wearablebraille;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.annotation.RequiresApi;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import mateuswetah.wearablebraille.BrailleÉcran.BrailleDots;
import mateuswetah.wearablebraille.BrailleÉcran.CharacterToSpeech;
import mateuswetah.wearablebraille.BrailleÉcran.MyBoxInsetLayout;
import mateuswetah.wearablebraille.GestureDetectors.PerkinsTapDetector;
import mateuswetah.wearablebraille.GestureDetectors.Swipe4DirectionsDetector;
import mateuswetah.wearablebraille.GestureDetectors.TwoFingersDoubleTapDetector;

/**
 * Created by orpheus on 15/11/17.
 */

public class ActivityTechPerkins extends WearableActivity{

    // View Components
    private MyBoxInsetLayout mContainerView;
    private BrailleDots brailleDots;
    private TextView tv1, tv2, tv3, resultLetter;
    private WearableActivity activity;

    // Touch Listeners
    TwoFingersDoubleTapDetector twoFingersListener;
    private GestureDetector gestureDetector;
    private View.OnClickListener dotClickListener;
    private PerkinsTapDetector perkinsTapDetector;

    // Feedback Tools
    private CharacterToSpeech tts;

    // Final Text
    private String message;

    //Flags
    boolean started = false;
    boolean stopped = true;
    boolean isStudy = false;
    boolean isScreenRotated = false;
    boolean isUsingWordReading = false;
    boolean isSpellChecking = false;
    boolean speakWordAtSpace = false;
    boolean infoOnLongPress = false;
    boolean isPerformingLongClick = false;
    boolean reset = false;
    boolean isTTSInitialized = false;

    // Perkins Column Control
    boolean perkinsColumnLeft = true;
    float windowWidth;
    MediaPlayer mediaPlayer;
    PlaybackParams mediaParams;

    // Test related
    int trialCount = 0;
    Util util;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_braille_core_stub);
        this.activity = this;

        // Sets TextToSpeech Feedback
        tts = new CharacterToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    Log.d("TTS", "TextToSpeech Service Initialized");
                    isTTSInitialized = true;
                    tts.setLanguage(Locale.getDefault());
                }
            }
        });

        // Sets up media player feedback
        mediaParams = new PlaybackParams();
        mediaParams.setPitch(0.5f);

        // Checks if view is in test mode
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.getBoolean("study") == true) {
                isStudy = true;
                Toast.makeText(getApplicationContext(), "User study mode", Toast.LENGTH_SHORT).show();
            } else isStudy = false;

            if (extras.getBoolean("isScreenRotated") == true) {
                isScreenRotated = true;
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            } else {
                isScreenRotated = false;
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }

            if (extras.getBoolean("useWordReading") == true)
                isUsingWordReading = true;
            else
                isUsingWordReading = false;

            if (extras.getBoolean("useSpellCheck") == true)
                isSpellChecking = true;
            else
                isSpellChecking = false;

            if (extras.getBoolean("speakWordAtSpace") == true)
                speakWordAtSpace = true;
            else
                speakWordAtSpace = false;

            if (extras.getBoolean("infoOnLongPress") == true)
                infoOnLongPress = true;
            else
                infoOnLongPress = false;
        }

        // Initializes message
        message = new String();

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

                // Initialize Perkins Column
                activity.findViewById(R.id.perkins_column_1).setBackgroundResource(R.drawable.braille_ecran_button);
                activity.findViewById(R.id.perkins_column_2).setBackground(null);

                // Swipe Gesture Detection
                gestureDetector = new GestureDetector(activity, new Swipe4DirectionsDetector() {
                    @Override
                    public void onTopSwipe() {
                        if (isScreenRotated) {
                            if (perkinsColumnLeft) {
                                brailleDots.setDotVisibility(0, true);
                                brailleDots.setDotVisibility(1, true);
                                brailleDots.setDotVisibility(2, true);
                            } else {
                                brailleDots.setDotVisibility(3, true);
                                brailleDots.setDotVisibility(4, true);
                                brailleDots.setDotVisibility(5, true);
                            }
                        } else {
                            if (perkinsColumnLeft) {
                                brailleDots.setDotVisibility(0, false);
                                brailleDots.setDotVisibility(1, false);
                                brailleDots.setDotVisibility(2, false);
                            } else {
                                brailleDots.setDotVisibility(3, false);
                                brailleDots.setDotVisibility(4, false);
                                brailleDots.setDotVisibility(5, false);
                            }
                        }

                        // Move to next Serial Line
                        switchPerkinsColumn();
                    }

                    @Override
                    public void onLeftSwipe() {
                    }

                    @Override
                    public void onBottomSwipe() {

                        if (isScreenRotated) {
                            if (perkinsColumnLeft) {
                                brailleDots.setDotVisibility(0,false);
                                brailleDots.setDotVisibility(1,false);
                                brailleDots.setDotVisibility(2,false);
                            } else {
                                brailleDots.setDotVisibility(3,false);
                                brailleDots.setDotVisibility(4,false);
                                brailleDots.setDotVisibility(5,false);
                            }
                        } else {
                            if (perkinsColumnLeft) {
                                brailleDots.setDotVisibility(0, true);
                                brailleDots.setDotVisibility(1, true);
                                brailleDots.setDotVisibility(2, true);
                            } else {
                                brailleDots.setDotVisibility(3, true);
                                brailleDots.setDotVisibility(4, true);
                                brailleDots.setDotVisibility(5, true);
                            }
                        }

                        // Move to next Serial Line
                        switchPerkinsColumn();

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
                b.putBoolean("useWordReading", isUsingWordReading);
                b.putBoolean("useSpellCheck", isSpellChecking);
                i.putExtras(b);
                startActivity(i);
                finish();
            }
        };

        Point dimensions = new Point();
        getWindowManager().getDefaultDisplay().getSize(dimensions);
        windowWidth = dimensions.x;

        perkinsTapDetector = new PerkinsTapDetector(windowWidth, isScreenRotated) {

            @Override
            public void onPerkinsDoubleTap(int line) {
                if (perkinsColumnLeft && !isPerformingLongClick) {
                    switch (line) {
                        case 1:
                            brailleDots.setDotVisibility(0, true);
                            brailleDots.setDotVisibility(1, true);
                            break;
                        case 2:
                            brailleDots.setDotVisibility(0, true);
                            brailleDots.setDotVisibility(2, true);
                            break;
                        case 3:
                            brailleDots.setDotVisibility(1, true);
                            brailleDots.setDotVisibility(2, true);
                            break;
                    }
                    Log.d("PERKINS", "DOUBLE TAP LEFT");
                } else if (!perkinsColumnLeft && !isPerformingLongClick) {
                    switch (line) {
                        case 1:
                            brailleDots.setDotVisibility(3, true);
                            brailleDots.setDotVisibility(4, true);
                            break;
                        case 2:
                            brailleDots.setDotVisibility(3, true);
                            brailleDots.setDotVisibility(5, true);
                            break;
                        case 3:
                            brailleDots.setDotVisibility(4, true);
                            brailleDots.setDotVisibility(5, true);
                            break;
                    }
                    Log.d("PERKINS", "DOUBLE TAP RIGHT");
                }


                // Move to next Serial Line
                if (!isPerformingLongClick)
                    switchPerkinsColumn();
            }

            @Override
            public void onPerkinsSingleTap(int line) {
                if (!isPerformingLongClick) {
                    switch (line) {
                        case 1:
                            if (perkinsColumnLeft) {
                                brailleDots.setDotVisibility(0,true);
                            } else {
                                brailleDots.setDotVisibility(3,true);
                            }
                            Log.d("PERKINS", "SINGLE TAP TOP");
                            break;

                        case 2:
                            if (perkinsColumnLeft) {
                                brailleDots.setDotVisibility(1,true);
                            } else {
                                brailleDots.setDotVisibility(4,true);
                            }
                            Log.d("PERKINS", "SINGLE TAP MIDDLE");
                            break;

                        case 3:
                            if (perkinsColumnLeft) {
                                brailleDots.setDotVisibility(2,true);
                            } else {
                                brailleDots.setDotVisibility(5,true);
                            }
                            Log.d("PERKINS", "SINGLE TAP BOTTOM");
                            break;
                    }
                    // Move to next Serial Line
                    switchPerkinsColumn();
                }
            }

        };
    }

    void setTouchListener() {
        if (infoOnLongPress)
            mContainerView.setLongClickable(true);

        mContainerView.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View view) {
                if (infoOnLongPress) {
                    Log.d("FULL MESSAGE OUTPUT: ", message);
                    tts.speak(getString(R.string.SendingFullSentence) + message, TextToSpeech.QUEUE_FLUSH, null, "Output");
                }
                return true;
            }
        });
        mContainerView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                mContainerView.onTouchEvent(event);
                twoFingersListener.onTouchEvent(event);
                if (!gestureDetector.onTouchEvent(event))
                    perkinsTapDetector.onTouchEvent(event);

                return true;
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    void switchPerkinsColumn() {

        perkinsColumnLeft = !perkinsColumnLeft;

        if (perkinsColumnLeft) {

            activity.findViewById(R.id.perkins_column_1).setBackgroundResource(R.drawable.braille_ecran_button);
            activity.findViewById(R.id.perkins_column_2).setBackground(null);

            //Processes Braille Character
            final String latinChar = brailleDots.checkCurrentCharacter(false, false, false, false);
            Log.d("CHAR OUTPUT: ", latinChar);

            resultLetter.setText(latinChar);
            message = message.concat(latinChar);

            if (isTTSInitialized) {
                if (isUsingWordReading || (speakWordAtSpace && latinChar.equals(" "))) {
                    // Breaks string into words to speak only last one
                    String[] words = message.split(" ");
                    tts.speak(words[words.length - 1], TextToSpeech.QUEUE_FLUSH, null, "Output");
                    Log.d("FULL MESSAGE OUTPUT: ", message);
                    Log.d("LAST MESSAGE OUTPUT: ", words[words.length - 1]);
                }
                else
                    tts.speak(latinChar, TextToSpeech.QUEUE_FLUSH, null, "Output");
            }

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    brailleDots.toggleAllDotsOff();
                    resultLetter.setText("");
                }
            }, 1500);

        } else {
            activity.findViewById(R.id.perkins_column_2).setBackgroundResource(R.drawable.braille_ecran_button);
            activity.findViewById(R.id.perkins_column_1).setBackground(null);
            activity.findViewById(R.id.perkins_column_2).setBackgroundResource(R.drawable.braille_ecran_button);
        }

        // Audio feedback for scroll list down navigation
        mediaPlayer = MediaPlayer.create(activity, R.raw.focus_actionable);
        mediaPlayer.setVolume(1.0f,1.0f);

        if (perkinsColumnLeft)
            mediaParams.setPitch(1.0f);
        else
            mediaParams.setPitch(0.5f);

        mediaPlayer.setPlaybackParams(mediaParams);
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.release();
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
