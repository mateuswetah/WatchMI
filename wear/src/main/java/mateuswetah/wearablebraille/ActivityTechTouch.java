package mateuswetah.wearablebraille;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;
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
import mateuswetah.wearablebraille.BrailleÉcran.MyBoxInsetLayout;
import mateuswetah.wearablebraille.GestureDetectors.OneFingerDoubleTapDetector;
import mateuswetah.wearablebraille.GestureDetectors.Swipe4DirectionsDetector;
import mateuswetah.wearablebraille.GestureDetectors.TwoFingersDoubleTapDetector;

/**
 * Created by orpheus on 15/11/17.
 */

public class ActivityTechTouch
        extends WearableActivity
        implements MessageApi.MessageListener, MessageClient.OnMessageReceivedListener {

    // View Components
    private MyBoxInsetLayout mContainerView;
    private BrailleDots brailleDots;
    private TextView tv1, tv2, tv3, resultLetter;
    private WearableActivity activity;

    // Autocomplete text field
    private AutoCompleteTextView autoCompleteTextView;
    private ArrayList<String> vocabulary = new ArrayList<String>();
    private ArrayAdapter<String> autoCompleteSpinnerAdapter;

    // Final Text
    private String message;

    // Touch Listeners
    TwoFingersDoubleTapDetector twoFingersListener;
    OneFingerDoubleTapDetector oneFingerListener;
    private View.OnClickListener dotClickListener;
    private View.OnLongClickListener dotLongClickListener;

    // Feedback Tools
    private ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME);
    private Vibrator vibrator = null;
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
    //Spell Checker
    private SpellCheckerSession spellCheckerSession;
    private ArrayList<String> suggestions;

    // Related to Sending the Message
    private List<Node> myNodes = new ArrayList<>();
    private static GoogleApiClient mGoogleApiClient;
    private static final String SPELLCHECKER_WEAR_PATH = "/message-to-spellchecker";
    private static final long CONNECTION_TIME_OUT_MS = 2500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_braille_core_stub);
        this.activity = this;

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
        vibrator = (Vibrator) this.activity.getSystemService(Context.VIBRATOR_SERVICE);

        // Initializes Google API
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApiIfAvailable(Wearable.API)
                .build();
        getNodes();

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
        vocabulary.add("Belgium");
        autoCompleteSpinnerAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, vocabulary);
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
                mContainerView = (MyBoxInsetLayout) findViewById(R.id.container);
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
                Log.d("FULL MESSAGE OUTPUT: ", message);

                resultLetter.setText(latinChar);
                message = message.concat(latinChar);

                if (isTTSInitialized) {
                    if (isUsingWordReading || latinChar.equals(" ")) {
                        // Breaks string into words to speak only last one
                        String[] words = message.split(" ");
                        tts.speak(words[words.length - 1], TextToSpeech.QUEUE_ADD, null, "Output");
                        Log.d("FULL MESSAGE OUTPUT: ", message);
                        Log.d("LAST MESSAGE OUTPUT: ", words[words.length - 1]);
                        // Used by SpellChecker
                        fetchSuggestionsFromMobile(words[words.length - 1]);
                    }
                    else {
                        speakComposedWord(latinChar);

                        // Automatically adds space after punctuation.
                        if (latinChar.equals(",") || latinChar.equals(".") || latinChar.equals(":")|| latinChar.equals("!") || latinChar.equals("?")){
                            brailleDots.toggleAllDotsOff();
                            onOneFingerDoubleTap();
                        }
                    }

                    // Updates AutoComplete EditText
                    if (isUsingAutoComplete) {
                        autoCompleteTextView.setText(message);
                        if ((String) autoCompleteTextView.getCompletionHint() != null)
                            Log.d("DICA", (String) autoCompleteTextView.getCompletionHint());
                    }
                }

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        resultLetter.setText("");
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
                b.putBoolean("useAutoComplete", isUsingAutoComplete);
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
                launchSuggestions();
                return true;
            case KeyEvent.KEYCODE_NAVIGATE_PREVIOUS:
                // Do something that advances a user View to the previous item in an ordered list.
                Log.d("FLICK", "PREV");
                launchSuggestions();
                return true;
        }
        // If you did not handle it, let it be handled by the next possible element as deemed by the Activity.
        return super.onKeyDown(keyCode, event);
    }

    private void launchSuggestions() {
        if (suggestions != null && suggestions.size() > 0) {
            Intent intent = new Intent(getApplicationContext(), ActivityAccessibleList.class);
            intent.putExtra("items", suggestions.toArray(new String[suggestions.size()]));
            intent.putExtra("introSpeakingSentence", getString(R.string.suggestions_intro));
            try {
                this.startActivityForResult(intent,1);
            } catch (ActivityNotFoundException e) {
                Log.d("ACTIVITY", "Can't find suggestions activity.");
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && data != null) {
            Log.d("SELECIONADO", data.getStringExtra("selectedItem"));
            applySuggestion(data.getStringExtra("selectedItem"));
        }
    }

    private void applySuggestion(String selectedSuggestion) {

        // Clears current suggestions list
        suggestions.clear();

        //Finds if there is any punctuation, as those are  ignored by spell checker
        String punctuation = message.substring(message.length() - 2);
        if (!(punctuation.equals(", ")) && !(punctuation.equals(". ")) && !(punctuation.equals("! ")) && !(punctuation.equals(": ")) && !(punctuation.equals("? ")) && !(punctuation.equals("; "))) {
            Log.d("PUNCTUATION", message.substring(message.length() - 2));
            punctuation = "";
        }

        // Rebuild list with fixed word
        String[] words = message.split(" ");
        words[words.length - 1] = selectedSuggestion;
        message = "";
        for (int i = 0; i < words.length; i++) {
            message = message + words[i];
            if (i < words.length - 1) {
                message = message + " ";
            }
        }
        message = message + punctuation;
        if (punctuation.equals("")) {
            message = message + " ";
        }
        Log.d("CORRECTED MESSAGE", message);

        // Speaks out selected suggestion
        if (isTTSInitialized) {
            tts.speak(getString(R.string.fixedMessage) + selectedSuggestion, TextToSpeech.QUEUE_FLUSH, null, "suggestion_option");
        }
    }

    // WORD SPEAKING
    private void speakComposedWord(String currentChar) {
        tts.speak(currentChar, TextToSpeech.QUEUE_FLUSH, null, "Output");

        if (!currentChar.equals("") &&
                !currentChar.equals("Nu") && !currentChar.equals("Ma") && !currentChar.equals("In") && !currentChar.equals("Cf") && !currentChar.equals("?!") &&
                !currentChar.equals("NU") && !currentChar.equals("MA") && !currentChar.equals("IN") && !currentChar.equals("CF")) {

            if (currentChar.equals(" ")) {
                tts.speak(getString(R.string.WhiteSpaceSpeech), TextToSpeech.QUEUE_FLUSH, null, "White Space Character Output");
            } else if (currentChar.equals("!"))
                tts.speak(getString(R.string.ExclamationSpeech), TextToSpeech.QUEUE_FLUSH, null, "Exclamation Sign Output");
            else if (currentChar.equals("?"))
                tts.speak(getString(R.string.InterrogationSpeech), TextToSpeech.QUEUE_FLUSH, null, "Interrogation Sign Output");
            else if (currentChar.equals("."))
                tts.speak(getString(R.string.PeriodSpeech), TextToSpeech.QUEUE_FLUSH, null, "Period Sign Output");
            else if (currentChar.equals("-"))
                tts.speak(getString(R.string.HyphenSpeech), TextToSpeech.QUEUE_FLUSH, null, "Hyphen Sign Output");
            else if (currentChar.equals(","))
                tts.speak(getString(R.string.CommaSpeech), TextToSpeech.QUEUE_FLUSH, null, "Comma Sign Output");
            else if (currentChar.equals(":"))
                tts.speak(getString(R.string.ColonSpeech), TextToSpeech.QUEUE_FLUSH, null, "Colon Sign Output");
            else if (currentChar.equals(";"))
                tts.speak(getString(R.string.SemiColonSpeech), TextToSpeech.QUEUE_FLUSH, null, "Semi Colon Sign Output");
            else if (currentChar.equals("-"))
                tts.speak(getString(R.string.HyphenSpeech), TextToSpeech.QUEUE_FLUSH, null, "Hyphen Sign Output");
            else if (currentChar.equals("@"))
                tts.speak(getString(R.string.AtSpeech), TextToSpeech.QUEUE_FLUSH, null, "AT Sign Output");
            else if (currentChar.equals("\\"))
                tts.speak(getString(R.string.BackSlashSpeech), TextToSpeech.QUEUE_FLUSH, null, "Back Slash Sign Output");
            else if (currentChar.equals("ç"))
                tts.speak(getString(R.string.CedillaCSpeech), TextToSpeech.QUEUE_FLUSH, null, "Cedille C Sign Output");
            else if (currentChar.equals("á"))
                tts.speak(getString(R.string.AcuteASpeech), TextToSpeech.QUEUE_FLUSH, null, "Acute A Sign Output");
            else if (currentChar.equals("é"))
                tts.speak(getString(R.string.AcuteESpeech), TextToSpeech.QUEUE_FLUSH, null, "Acute E Sign Output");
            else if (currentChar.equals("í"))
                tts.speak(getString(R.string.AcuteISpeech), TextToSpeech.QUEUE_FLUSH, null, "Acute I Sign Output");
            else if (currentChar.equals("ó"))
                tts.speak(getString(R.string.AcuteOSpeech), TextToSpeech.QUEUE_FLUSH, null, "Acute O Sign Output");
            else if (currentChar.equals("ú"))
                tts.speak(getString(R.string.AcuteUSpeech), TextToSpeech.QUEUE_FLUSH, null, "Acute U Sign Output");
            else if (currentChar.equals("â"))
                tts.speak(getString(R.string.CircumflexASpeech), TextToSpeech.QUEUE_FLUSH, null, "Circumflex A Sign Output");
            else if (currentChar.equals("ê"))
                tts.speak(getString(R.string.CircumflexESpeech), TextToSpeech.QUEUE_FLUSH, null, "Circumflex E Sign Output");
            else if (currentChar.equals("ô"))
                tts.speak(getString(R.string.CircumflexOSpeech), TextToSpeech.QUEUE_FLUSH, null, "Circumflex O Sign Output");
            else if (currentChar.equals("ã"))
                tts.speak(getString(R.string.TildeASpeech), TextToSpeech.QUEUE_FLUSH, null, "Tilde A Sign Output");
            else if (currentChar.equals("õ"))
                tts.speak(getString(R.string.TildeOSpeech), TextToSpeech.QUEUE_FLUSH, null, "Tilde O Sign Output");
            else if (currentChar.equals("à"))
                tts.speak(getString(R.string.CrasisASpeech), TextToSpeech.QUEUE_FLUSH, null, "Crasis A Sign Output");
            else {
                tts.speak((CharSequence) currentChar, TextToSpeech.QUEUE_FLUSH, null, "Audio Character Output");
            }

//            if (tmpCapsOn == true) {
//                tmpCapsOn = false;
//                tts.speak(getString(R.string.DeactivatingCapitalLetter), TextToSpeech.QUEUE_FLUSH, null, "Caps Deactivated Message");
//            } else if (tmpNumOn == true) {
//                tmpNumOn = false;
//                tts.speak(getString(R.string.DeactivatingNumbers), TextToSpeech.QUEUE_FLUSH, null, "Numbers Deactivated Message");
//            }

        }
    }

    // SPELL CHECKING -----------------------------------------
    private void fetchSuggestionsFromMobile(String input){

        if (myNodes != null && mGoogleApiClient != null) {

            byte[] message = new byte[0];
            try {
                message = input.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            final byte[] finalMessage = message;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for(Node n:myNodes) {
                        Log.d("GOOGLE API","Sending message to node: " + n.getDisplayName());
                        Wearable.MessageApi.sendMessage(mGoogleApiClient,n.getId(),SPELLCHECKER_WEAR_PATH, finalMessage);
                    }
                }
            }).run();
        }
    }

    /* Handling received suggestions. */
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        String messageEventString = new String(messageEvent.getData());
        messageEventString = messageEventString.substring(1, messageEventString.length() - 1);
        String[] suggestedStrings = messageEventString.split(", ");
        suggestions = new ArrayList<String>();

        for (String suggestion: suggestedStrings) {
            suggestions.add(suggestion);
            Log.d("ON SUGGESTIONS RECEIVED", suggestion);
//            this.autoCompleteSpinnerAdapter.add(suggestion);
        }
//        if (suggestions.size() > 0) {
//            this.autoCompleteSpinnerAdapter.notifyDataSetChanged();
//        }
        toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD);

    }

    private List<Node> getNodes(){
        new Thread(new Runnable() {

            @Override
            public void run() {
                Log.d("GOOGLE API","Getting Google API nodes...");

                mGoogleApiClient.blockingConnect(CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);

                NodeApi.GetConnectedNodesResult result = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                List<Node> nodes = result.getNodes();

                for(Node n:nodes){
                    Log.d("GOOGLE API","Adding Google API Node: "+n.getDisplayName());
                    myNodes.add(n);
                }

                Log.d("GOOGLE API","Getting nodes DONE!");
            }
        }).start();

        return null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Wearable.getMessageClient(this).addListener(this);
    }

    @Override
    protected void onPause() {
        Wearable.getMessageClient(this).removeListener(this);
        super.onPause();
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