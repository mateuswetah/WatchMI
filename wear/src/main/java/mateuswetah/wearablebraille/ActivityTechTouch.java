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
import mateuswetah.wearablebraille.GestureDetectors.Swipe4DirectionsDetector;
import mateuswetah.wearablebraille.GestureDetectors.TwoFingersDoubleTapDetector;
import mateuswetah.wearablebraille.GestureDetectors.TwoFingersSwipeDetector;

/**
 * Created by orpheus on 15/11/17.
 */

public class ActivityTechTouch
        extends WearableActivity
        implements MessageApi.MessageListener,
            MessageClient.OnMessageReceivedListener {

    // View Components
    private MyBoxInsetLayout mContainerView;
    private BrailleDots brailleDots;
    private TextView tv1, tv2, tv3, resultLetter;
    private WearableActivity activity;
    private DrawView drawView;

    // Final Text
    private String message;
    private int cursorPosition;

    // Touch Listeners
    TwoFingersDoubleTapDetector twoFingersListener;
    TwoFingersSwipeDetector twoFingersSwipeListener;
    private View.OnClickListener dotClickListener;
    private View.OnLongClickListener dotLongClickListener;
    private static final int DOUBLE_TAP_MAX_DIST = 40;

    // Feedback Tools
    private ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME);
    private Vibrator vibrator = null;
    private CharacterToSpeech tts;

    //Flags
    boolean started = false;
    boolean stopped = true;
    boolean isStudy = false;
    boolean isScreenRotated = false;
    boolean reset = false;
    boolean isUsingWordReading = false;
    boolean isSpellChecking = false;
    boolean speakWordAtSpace = false;
    boolean infoOnLongPress = false;
    boolean spaceAfterPunctuation = false;
    boolean isTTSInitialized = false;
    boolean hasJustTwoFingerSwiped = false;

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
        tts = new CharacterToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    Log.d("TTS", "TextToSpeech Service Initialized");
                    isTTSInitialized = true;
                    Log.d("TTS Voices", tts.getVoices().toString());
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

            if (extras.getBoolean("spaceAfterPunctuation") == true)
                spaceAfterPunctuation = true;
            else
                spaceAfterPunctuation = false;
        }

        // Initializes util, to get touch area relation with buttons
        util = new Util();

        // Initializes message and cursor position
        message = new String();
        cursorPosition = 0;

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

                            int position = util.DetermineTouchPos(event.getX(), event.getY(), false);
                            switch (position) {
                                case 2:
                                    if (event.getActionMasked() == MotionEvent.ACTION_UP)
                                        brailleDots.ButtonDots[3].callOnClick();
                                    break;
                                case 3:
                                    if (event.getActionMasked() == MotionEvent.ACTION_UP)
                                        brailleDots.ButtonDots[4].callOnClick();
                                    break;
                                case 4:
                                    if (event.getActionMasked() == MotionEvent.ACTION_UP)
                                        brailleDots.ButtonDots[5].callOnClick();
                                    break;
                                case 6:
                                    if (event.getActionMasked() == MotionEvent.ACTION_UP)
                                        brailleDots.ButtonDots[2].callOnClick();
                                    break;
                                case 7:
                                    if (event.getActionMasked() == MotionEvent.ACTION_UP)
                                        brailleDots.ButtonDots[1].callOnClick();
                                    break;
                                case 8:
                                    if (event.getActionMasked() == MotionEvent.ACTION_UP)
                                        brailleDots.ButtonDots[0].callOnClick();
                                    break;
                                default:
                                    gestureDetector.onTouchEvent(event);
                            }
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
                if (infoOnLongPress)
                    drawView.setLongClickable(true);

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

    public void confirmCharacter() {
        final String latinChar = brailleDots.checkCurrentCharacter(false, false, false, false);
        Log.d("CHAR OUTPUT: ", latinChar);

        resultLetter.setText(latinChar);
        if (message.length() > 1 && cursorPosition < message.length() - 1) {
            message = (message.substring(0, cursorPosition + 1).concat(latinChar)).concat(message.substring(cursorPosition + 1));
        } else {
            message = message.concat(latinChar);
        }

        Log.d("MESSAGE OUTPUT: ", "message:" + message);

        cursorPosition++;

        if (isTTSInitialized) {
            if (isUsingWordReading || (speakWordAtSpace && latinChar.equals(" "))) {
                // Breaks string into words to speak only last one
                String[] words = message.substring(0,cursorPosition).split(" ");
                if (words.length > 0) {
                    tts.speak(words[words.length - 1], TextToSpeech.QUEUE_ADD, null, "Output");
                    Log.d("FULL MESSAGE OUTPUT: ", message);
                    Log.d("LAST MESSAGE OUTPUT: ", words[words.length - 1]);
                    // Used by SpellChecker
                    fetchSuggestionsFromMobile(words[words.length - 1]);
                }
            } else {
                speakComposedWord(latinChar);

                // Automatically adds space after punctuation.
                if (spaceAfterPunctuation && (latinChar.equals(",") || latinChar.equals(".") || latinChar.equals(":")|| latinChar.equals("!") || latinChar.equals("?"))){
                    brailleDots.toggleAllDotsOff();
                    confirmCharacter();
                }
            }
        }

        // Clears result letter on screen
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                resultLetter.setText("");
            }
        }, 1200);

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

    // WRIST TWIST GESTURES, CALL SUGGESTION LIST
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_NAVIGATE_NEXT:
                if (isSpellChecking) {
                    Log.d("WRIST FLICK", "SPELL CHECKING...");
                    launchSuggestions();
                } else {
                    Log.d("WRIST FLICK", "SPELL CHECK DISABLED ON WATCH");
                }
                return true;
            case KeyEvent.KEYCODE_NAVIGATE_PREVIOUS:
                Log.d("WRIST FLICK", "PREV");
                if (isSpellChecking) {
                    Log.d("WRIST FLICK", "SPELL CHECKING...");
                    launchSuggestions();
                } else {
                    Log.d("WRIST FLICK", "SPELL CHECK DISABLED ON WATCH");
                }
                return true;
        }
        // If you did not handle it, let it be handled by the next possible element as deemed by the Activity.
        return super.onKeyDown(keyCode, event);
    }

    // ENTERING NAVIGATION MODE LIST
    private void enterNavigationMode() {
        if (message != null && message.length() > 0) {
            Intent intent = new Intent(getApplicationContext(), ActivityAccessibleList.class);
            String[] chars = new StringBuilder(message).reverse().toString().split("");
            String[] charsRelevant = new String[chars.length - 1];
            for (int i = 1; i < chars.length; i++)
                charsRelevant[i-1] = chars[i];

            intent.putExtra("items", charsRelevant);
            intent.putExtra("introSpeakingSentence", getString(R.string.navigation_mode_intro));
            try {
                this.startActivityForResult(intent,2);
            } catch (ActivityNotFoundException e) {
                Log.d("ACTIVITY", "Can't find edition activity.");
            }
        }
    }

    // ENTERING SUGGESTIONS LIST
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

    // RETURNING FROM LIST VIEW, EITHER SUGGESTIONS OR NAVIGATION MODE
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Log.d("SUGGESTIONS", "Selecionado: " + data.getStringExtra("selectedItem"));
            applySuggestion(data.getStringExtra("selectedItem"));
        } else if (requestCode == 2 && resultCode == RESULT_OK && data != null) {
            Log.d("SELECTED WORD", "Selecionado: " + data.getStringExtra("selectedItem"));
//            removeCharacter(data.getStringExtra("selectedItem"), data.getIntExtra("selectedIndex", -1));
            cursorPosition = Math.abs(data.getIntExtra("selectedIndex", 0) - (message.length() - 1));
            Log.d("NEW CURSOR POSITION", cursorPosition + "");
        } else if (resultCode == RESULT_CANCELED ){
            Log.d("SUGGESTIONS", "Cancelado");
            if (isTTSInitialized) {
                tts.speak(getString(R.string.canceled), TextToSpeech.QUEUE_FLUSH, null, "suggestion_canceled");
            }
        }
    }

    // CHARACTER DELETING
    private void removeCharacter() {
        if (cursorPosition <= message.length() && cursorPosition > 0) {
            tts.speak(tts.getAdaptedText(String.valueOf(message.charAt(cursorPosition -  1))) + " " + getString(R.string.deleted), TextToSpeech.QUEUE_FLUSH, null, "character_deleted");

            StringBuilder stringBuilder = new StringBuilder(message);
            stringBuilder.deleteCharAt(cursorPosition -1);
            message = stringBuilder.toString();
            cursorPosition--;
        }
    }

    // SPELL CHECKER SUGGESTION ACCEPTATION
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
        tts.speak(currentChar, TextToSpeech.QUEUE_FLUSH, null, "Audio Character Output");
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
        }
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