package mateuswetah.wearablebraille;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.view.textservice.SentenceSuggestionsInfo;
import android.view.textservice.SpellCheckerSession;
import android.view.textservice.SuggestionsInfo;
import android.view.textservice.TextInfo;
import android.view.textservice.TextServicesManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.concurrent.CompletionException;

import mateuswetah.wearablebraille.BrailleÉcran.BrailleDots;
import mateuswetah.wearablebraille.GestureDetectors.OneFingerDoubleTapDetector;
import mateuswetah.wearablebraille.GestureDetectors.Swipe4DirectionsDetector;
import mateuswetah.wearablebraille.GestureDetectors.TwoFingersDoubleTapDetector;

/**
 * Created by orpheus on 15/11/17.
 */

public class ActivityTechTouch
        extends WearableActivity
        implements GoogleApiClient.ConnectionCallbacks,
                    GoogleApiClient.OnConnectionFailedListener {

    // View Components
    private BoxInsetLayout mContainerView;
    private BrailleDots brailleDots;
    private TextView tv1, tv2, tv3, resultLetter;
    private WearableActivity activity;

    // Autocomplete text field
    private AutoCompleteTextView autoCompleteTextView;
    private static final String[] VOCABULARY = new String[] {
            "Belgium", "France", "Italy", "Germany", "Spain"
    };
    private ArrayAdapter<String> autoCompleteSpinnerAdapter;

    // Final Text
    private String message;

    // Touch Listeners
    TwoFingersDoubleTapDetector twoFingersListener;
    OneFingerDoubleTapDetector oneFingerListener;
    private View.OnClickListener dotClickListener;
    private View.OnLongClickListener dotLongClickListener;

    // Feedback Tools
    private TextToSpeech tts;

    //Flags
    boolean started = false;
    boolean stopped = true;
    boolean isStudy = false;
    boolean isScreenRotated = false;
    boolean reset = false;
    boolean isUsingWordReading = false;
    boolean isUsingAutoComplete = false;
    boolean isTTSInitialized = false;

    // Test related
    int trialCount = 0;
    Util util;

    // Gesture detector for implementing Swipe gestures
    private GestureDetector gestureDetector;

    //Spell Checker, testing...
    private SpellCheckerSession spellCheckerSession;
    private String[] suggestions;

    // Related to Sending the Message
    private static final String WEARABLE_MAIN = "WearableMain";
    private static Node mNode;
    private static GoogleApiClient mGoogleApiClient;
    private static final String SPELLCHECKER_WEAR_PATH = "/gesture-from-wear";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_braille_core_stub);
        this.activity = this;

        // Gets the googleApiClient for communicating with mobile pair
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mGoogleApiClient.connect();

        // Sets TextToSpeech for feedback
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    Log.d("TTS", "TextToSpeech Service Initialized");
                    isTTSInitialized = true;
                    tts.setLanguage(Locale.getDefault());
                }
            }
        });

        // Updates settings variables
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

            if (extras.getBoolean("useWordReading") == true)
                isUsingWordReading = true;
            else
                isUsingWordReading = false;

            if (extras.getBoolean("useAutoComplete") == true)
                isUsingAutoComplete = true;
            else
                isUsingAutoComplete = false;
        }

        //Initializes Autocomplete EditText
        Spinner spinner = (Spinner) findViewById(R.id.spinner1);
        autoCompleteSpinnerAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, VOCABULARY);
        autoCompleteTextView = (AutoCompleteTextView)
                findViewById(R.id.autoCompleteTextView);
        autoCompleteTextView.setText("");
        autoCompleteTextView.setClickable(false);
        autoCompleteTextView.setActivated(false);
        autoCompleteTextView.setTextIsSelectable(false);
        autoCompleteTextView.setCursorVisible(false);
        autoCompleteTextView.setAdapter(autoCompleteSpinnerAdapter);
        autoCompleteTextView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d("AUTOCOMPLETE", "Selecionei" + adapterView.getItemAtPosition(i).toString());
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d("AUTOCOMPLETE", "Cliquei" + adapterView.getItemAtPosition(i).toString());
            }
        });

        // Initializes message
        message = new String();

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

                // Sets a special font for braille printing
                //Typeface typeFace = Typeface.createFromAsset(getAssets(),"font/visualbraille.ttf");
                //resultLetter.setTypeface(typeFace);

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

                dotLongClickListener = new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {

                        int currentButton = v.getId();

                        switch (currentButton) {

                            case R.id.dotButton1:
                                brailleDots.toggleDotVisibility(0);
                                brailleDots.toggleDotVisibility(3);
                                break;
                            case R.id.dotButton4:
                                brailleDots.toggleDotVisibility(0);
                                brailleDots.toggleDotVisibility(3);
                                break;
                            case R.id.dotButton2:
                                brailleDots.toggleDotVisibility(1);
                                brailleDots.toggleDotVisibility(4);
                                break;
                            case R.id.dotButton5:
                                brailleDots.toggleDotVisibility(1);
                                brailleDots.toggleDotVisibility(4);
                                break;
                            case R.id.dotButton3:
                                brailleDots.toggleDotVisibility(2);
                                brailleDots.toggleDotVisibility(5);
                                break;
                            case R.id.dotButton6:
                                brailleDots.toggleDotVisibility(2);
                                brailleDots.toggleDotVisibility(5);
                                break;
                        }
                        return true;
                    }
                };

                // Associate OnClick and OnLongClick listeners to ButtonDots.
                for (int i = 0; i < brailleDots.ButtonDots.length; i++) {
                    brailleDots.ButtonDots[i].setOnClickListener(dotClickListener);
//                    brailleDots.ButtonDots[i].setOnLongClickListener(dotLongClickListener);
                }
            }
        });

        // Sets one finger double tap detector
        oneFingerListener = new OneFingerDoubleTapDetector() {
            @Override
            public void onOneFingerDoubleTap() {
                final String latinChar = brailleDots.checkCurrentCharacter(false, false, false, false);
                Log.d("CHAR OUTPUT: ", latinChar);

                resultLetter.setText(latinChar);
                message = message.concat(latinChar);

                if (isTTSInitialized) {
                    if (isUsingWordReading || latinChar.equals(" ")) {
                        // Breaks string into words to speak only last one
                        String[] words = message.split(" ");
                        tts.speak(words[words.length - 1], TextToSpeech.QUEUE_FLUSH, null, "Output");
                        Log.d("FULL MESSAGE OUTPUT: ", message);
                        Log.d("LAST MESSAGE OUTPUT: ", words[words.length - 1]);
                    }
                    else
                        tts.speak(latinChar, TextToSpeech.QUEUE_FLUSH, null, "Output");

                    // Updates AutoComplete EditText
                    if (isUsingAutoComplete) {
                        autoCompleteTextView.setText(message);
                        if ((String) autoCompleteTextView.getCompletionHint() != null)
                            Log.d("DICA", (String) autoCompleteTextView.getCompletionHint());
                    }

                    // Used by SpellChecker
                    fetchSuggestionsFromMobile(message);
                }

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        resultLetter.setText("");
                        brailleDots.toggleAllDotsOff();
                    }
                }, 1200);
            }
        };

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
                b.putBoolean("useWordReading", isUsingWordReading);
                i.putExtras(b);
                startActivity(i);
                finish();
            }
        };

    }

    void setTouchListener() {
        mContainerView.setLongClickable(true);
        mContainerView.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View view) {
                Log.d("FULL MESSAGE OUTPUT: ", message);
                tts.speak(getString(R.string.SendingFullSentence) + message, TextToSpeech.QUEUE_FLUSH, null, "Output");
                return true;
            }
        });
        mContainerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                twoFingersListener.onTouchEvent(event);
                oneFingerListener.onTouchEvent(event);
                gestureDetector.onTouchEvent(event);
                mContainerView.onTouchEvent(event);
                return true;
            }
        });
    }

    // WRIST TWIST GESTURES
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_NAVIGATE_NEXT:
                // Do something that advances a user View to the next item in an ordered list.
                Log.d("FLICK", "NEXT");
                return true;
            case KeyEvent.KEYCODE_NAVIGATE_PREVIOUS:
                // Do something that advances a user View to the previous item in an ordered list.
                Log.d("FLICK", "PREV");
                return true;
        }
        // If you did not handle it, let it be handled by the next possible element as deemed by the Activity.
        return super.onKeyDown(keyCode, event);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.brailleDots.freeTTSService();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }

    // SPELL CHECKING -----------------------------------------
    private void fetchSuggestionsFromMobile(String input){

        if (mNode != null && mGoogleApiClient != null) {

            byte[] message = new byte[0];
            try {
                message = input.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            Wearable.MessageApi.sendMessage(mGoogleApiClient, mNode.getId(),
                    SPELLCHECKER_WEAR_PATH, message)
                    .setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                            if (!sendMessageResult.getStatus().isSuccess()) {
                                Log.d(WEARABLE_MAIN, "Failed message:" + sendMessageResult.getStatus().getStatusCode());
                            } else {
                                Log.d(WEARABLE_MAIN, "Message succeeded");
                            }
                        }
                    });
        }

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient)
                .setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(NodeApi.GetConnectedNodesResult nodes) {
                        for (Node node : nodes.getNodes()) {
                            if (node != null && node.isNearby()) {
                                mNode = node;
                                Log.d(WEARABLE_MAIN, "Connected to " + mNode.getDisplayName());

                                String id = mNode.getId();
                                String name = mNode.getDisplayName();

                                Log.d("WEAR CONNECTION", "Connected peer name & ID: " + name + "|" + id);

                            }
                        }
                        if (mNode == null) {
                            Log.d("WEAR CONNECTION", "Not connected");
//                            Intent intent = new Intent(getBaseContext(), MobileConnectedConfirmationActivity.class);
//                            startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY));
                        }
                    }
                });
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("WEAR CONNECTION", "Connection suspended");
//        Intent intent = new Intent(getBaseContext(), MobileConnectedConfirmationActivity.class);
//        startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY));
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("WEAR CONNECTION", "Connection failed");
    }
}