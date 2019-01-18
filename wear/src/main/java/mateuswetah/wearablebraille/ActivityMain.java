package mateuswetah.wearablebraille;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

public class ActivityMain extends WearableActivity {

    private Button buttonStart, buttonSetLayout;
    private Switch  screenRotateSwitch,
                    toneGeneratorSwitch,
                    vibrationPatternSwitch,
                    wordReadingSwitch,
                    useSpellCheckSwitch,
                    dotSpeakerSwitch,
                    speakWordAtSpaceSwitch,
                    infoOnLongPressSwitch,
                    spaceAfterPunctuationSwitch,
                    reversedLinesSwitch;
    private Boolean isScreenRotated = false;
    private Boolean isUsingWordReading = false;
    private Boolean isUsingSpellCheck = false;
    private Boolean speakWordAtSpace = false;
    private Boolean infoOnLongPress = false;
    private Boolean useVibrationPatterns = false;
    private Boolean spaceAfterPunctuation = false;
    private Boolean useReversedLines = false;

    public static final String PREFS_NAME = "SettingsFile";
    public SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        setAmbientEnabled();

        // Sets Data Storage
        settings = getSharedPreferences(PREFS_NAME, 0);
        final SharedPreferences.Editor editor = settings.edit();

        buttonStart = (Button) findViewById(R.id.buttonStart);
        buttonStart.setOnClickListener(buttonStartClickListener);

        buttonSetLayout = (Button) findViewById(R.id.buttonSetLayout);
        buttonSetLayout.setOnClickListener(buttonSetLayoutClickListener);

        screenRotateSwitch = (Switch) findViewById(R.id.screenRotateSwitch);
        screenRotateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                isScreenRotated = b;
                if (isScreenRotated)
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                else
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        });

        toneGeneratorSwitch = (Switch) findViewById(R.id.toneGeneratorSwitch);
        toneGeneratorSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            editor.putBoolean("useToneGenerator", b);
            editor.commit();
            }
        });
        toneGeneratorSwitch.setChecked(settings.getBoolean("useToneGenerator", false));

        vibrationPatternSwitch = (Switch) findViewById(R.id.vibrationPatternsSwitch);
        vibrationPatternSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                editor.putBoolean("useVibrationPatterns", b);
                editor.commit();
            }
        });
        vibrationPatternSwitch.setChecked(settings.getBoolean("useVibrationPatterns", false));

        wordReadingSwitch = (Switch) findViewById(R.id.wordReadingSwitch);
        wordReadingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                isUsingWordReading = b;

                editor.putBoolean("useWordReading", b);
                editor.commit();
            }
        });
        wordReadingSwitch.setChecked(settings.getBoolean("useWordReading", false));

        useSpellCheckSwitch = (Switch) findViewById(R.id.spellCheckSwitch);
        useSpellCheckSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                isUsingSpellCheck = b;

                editor.putBoolean("useSpellCheck", b);
                editor.commit();
            }
        });
        useSpellCheckSwitch.setChecked(settings.getBoolean("useSpellCheck", false));

        dotSpeakerSwitch = (Switch) findViewById(R.id.dotSpeakerSwitch);
        dotSpeakerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                editor.putBoolean("useDotNumberSpeaking", b);
                editor.commit();
            }
        });
        dotSpeakerSwitch.setChecked(settings.getBoolean("useDotNumberSpeaking", false));

        speakWordAtSpaceSwitch = (Switch) findViewById(R.id.speakWordAtSpaceSwitch);
        speakWordAtSpaceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                editor.putBoolean("speakWordAtSpace", b);
                editor.commit();
            }
        });
        speakWordAtSpaceSwitch.setChecked(settings.getBoolean("speakWordAtSpace", false));

        infoOnLongPressSwitch = (Switch) findViewById(R.id.infoOnLongPressSwitch);
        infoOnLongPressSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                editor.putBoolean("infoOnLongPress", b);
                editor.commit();
            }
        });
        infoOnLongPressSwitch.setChecked(settings.getBoolean("infoOnLongPress", false));

        spaceAfterPunctuationSwitch = (Switch) findViewById(R.id.spaceAfterPunctuationSwitch);
        spaceAfterPunctuationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                editor.putBoolean("spaceAfterPunctuation", b);
                editor.commit();
            }
        });
        spaceAfterPunctuationSwitch.setChecked(settings.getBoolean("spaceAfterPunctuation", false));

        reversedLinesSwitch = (Switch) findViewById(R.id.reversedLinesSwitch);
        reversedLinesSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                editor.putBoolean("useReversedLines", b);
                editor.commit();
            }
        });
        reversedLinesSwitch.setChecked(settings.getBoolean("useReversedLines", false));

        // Checks if screen is rotated and is using word reading
        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            if (extras.getBoolean("isScreenRotated") == true) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                isScreenRotated = true;
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                isScreenRotated = false;
            }
            if (extras.getBoolean("useWordReading") == true) {
                isUsingWordReading = true;
            } else {
                isUsingWordReading = false;
            }
            if (extras.getBoolean("useSpellCheck") == true) {
                isUsingSpellCheck = true;
            } else {
                isUsingSpellCheck = false;
            }
            if (extras.getBoolean("speakWordAtSpace") == true) {
                speakWordAtSpace = true;
            } else {
                speakWordAtSpace = false;
            }
            if (extras.getBoolean("infoOnLongPress") == true) {
                infoOnLongPress = true;
            } else {
                infoOnLongPress = false;
            }
            if (extras.getBoolean("spaceAfterPunctuation") == true) {
                spaceAfterPunctuation = true;
            } else {
                spaceAfterPunctuation = false;
            }
//            if (extras.getBoolean("useVibrationPatterns") == true) {
//                useVibrationPatterns = true;
//            } else {
//                useVibrationPatterns = false;
//            }
//            if (extras.getBoolean("useReversedLines") == true) {
//                useReversedLines = true;
//            } else {
//                useReversedLines = false;
//            }
        }
        this.screenRotateSwitch.setChecked(isScreenRotated);
        this.wordReadingSwitch.setChecked(isUsingWordReading);
        this.useSpellCheckSwitch.setChecked(isUsingSpellCheck);
        this.speakWordAtSpaceSwitch.setChecked(infoOnLongPress);
        this.infoOnLongPressSwitch.setChecked(infoOnLongPress);
        this.spaceAfterPunctuationSwitch.setChecked(spaceAfterPunctuation);
//        this.vibrationPatternSwitch.setChecked(useVibrationPatterns);
//        this.reversedLinesSwitch.setChecked(useReversedLines);
    }

    Button.OnClickListener buttonStartClickListener = new Button.OnClickListener() {
        public void onClick(View arg0) {
            Bundle b = new Bundle();
            b.putBoolean("study", false);
            b.putBoolean("isScreenRotated", isScreenRotated);
            b.putBoolean("useWordReading", isUsingWordReading);
            b.putBoolean("useSpellCheck", isUsingSpellCheck);
            b.putBoolean("speakWordAtSpace", speakWordAtSpace);
            b.putBoolean("infoOnLongPress", infoOnLongPress);
            b.putBoolean("spaceAfterPunctuation", spaceAfterPunctuation);
            Intent i = new Intent(getApplicationContext(),  ActivitySelectTech.class);
            i.putExtras(b);
            startActivity(i);
            finish();
        }
    };

    Button.OnClickListener buttonSetLayoutClickListener = new Button.OnClickListener() {
        public void onClick(View arg0) {
            Bundle b = new Bundle();
            Intent i = new Intent(getApplicationContext(),  ActivitySetLayout.class);
            i.putExtras(b);
            startActivity(i);
            finish();
        }
    };
}
