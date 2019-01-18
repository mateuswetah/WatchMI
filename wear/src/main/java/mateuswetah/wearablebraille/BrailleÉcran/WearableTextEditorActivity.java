package mateuswetah.wearablebraille.BrailleÉcran;

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
import android.util.Log;
import android.view.KeyEvent;
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

import mateuswetah.wearablebraille.R;

public class WearableTextEditorActivity
        extends WearableActivity
        implements MessageApi.MessageListener,
        MessageClient.OnMessageReceivedListener {

    // View Components
    protected MyBoxInsetLayout mContainerView;
    protected BrailleDots brailleDots;
    protected WearableActivity activity;
    protected TextView tv1, tv2, tv3, resultLetter;

    // Final Text
    protected String message;
    protected int cursorPosition;

    // Feedback Tools
    protected ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME);
    protected Vibrator vibrator = null;
    protected CharacterToSpeech tts;

    //Flags
    protected boolean isStudy = false;
    protected boolean isScreenRotated = false;
    protected boolean isUsingWordReading = false;
    protected boolean isSpellChecking = false;
    protected boolean speakWordAtSpace = false;
    protected boolean infoOnLongPress = false;
    protected boolean spaceAfterPunctuation = false;
    protected boolean isTTSInitialized = false;
    protected boolean hasJustTwoFingerSwiped = false;

    //Spell Checker
    protected SpellCheckerSession spellCheckerSession;
    protected ArrayList<String> suggestions;

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

        // Initializes message and cursor position
        message = new String();
        cursorPosition = 0;
        
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
            if (extras.getBoolean("study")) {
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

            if (extras.getBoolean("useWordReading"))
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

    };

    public void confirmCharacter() {
        final String latinChar = brailleDots.checkCurrentCharacter(false, false, false, false);
        Log.d("CHAR OUTPUT: ", latinChar);

        resultLetter.setText(latinChar);
        if (!latinChar.equals("Ma") &&
                !latinChar.equals("MA") &&
                !latinChar.equals("Nu") &&
                !latinChar.equals("NU") &&
                !latinChar.equals("In") &&
                !latinChar.equals("IN") ) {
            if (message.length() > 1 && cursorPosition < message.length() - 1) {
                message = (message.substring(0, cursorPosition + 1).concat(latinChar)).concat(message.substring(cursorPosition + 1));
            } else {
                message = message.concat(latinChar);
            }
            Log.d("MESSAGE OUTPUT: ", "message:" + message);
            cursorPosition++;
        }

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
    protected void enterNavigationMode() {
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
    protected void removeCharacter() {
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

    // LIFECYCLE HOOKS ---------------------------------------
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
