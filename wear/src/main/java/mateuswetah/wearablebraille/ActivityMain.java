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

import java.util.prefs.Preferences;

public class ActivityMain extends WearableActivity {

    private Button btn1, btn2, btn3;
    private Switch screenRotateSwitch, toneGeneratorSwitch, vibrationPatternSwitch, dotSpeakerSwitch;
    private Boolean isScreenRotated = false;
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

        btn1 = (Button) findViewById(R.id.btn1);
        btn1.setOnClickListener(button1ClickListener);
        //btn2 = (Button) findViewById(R.id.btn2);
        //btn2.setOnClickListener(button2ClickListener);
        //btn3 = (Button) findViewById(R.id.btn3);
        //btn3.setOnClickListener(button3ClickListener);
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

        dotSpeakerSwitch = (Switch) findViewById(R.id.dotSpeakerSwitch);
        dotSpeakerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                editor.putBoolean("useDotNumberSpeaking", b);
                editor.commit();
            }
        });
        dotSpeakerSwitch.setChecked(settings.getBoolean("useDotNumberSpeaking", false));

        // Checks if screen is rotated
        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            if (extras.getBoolean("isScreenRotated") == true) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                isScreenRotated = true;
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                isScreenRotated = false;
            }
        }
        this.screenRotateSwitch.setChecked(isScreenRotated);

    }

    Button.OnClickListener button1ClickListener = new Button.OnClickListener() {
        public void onClick(View arg0) {
            Bundle b = new Bundle();
            b.putBoolean("study", false);
            b.putBoolean("isScreenRotated", isScreenRotated);
            Intent i = new Intent(getApplicationContext(),  ActivitySelectTech.class);
            i.putExtras(b);
            startActivity(i);
            finish();
        }
    };
    Button.OnClickListener button2ClickListener = new Button.OnClickListener() {
        public void onClick(View arg0) {
            Bundle b = new Bundle();
            b.putBoolean("study", true);
            b.putBoolean("isScreenRotated", isScreenRotated);
            Intent i = new Intent(getApplicationContext(),  ActivitySelectTech.class);
            i.putExtras(b);
            startActivity(i);
            finish();
        }
    };
    Button.OnClickListener button3ClickListener = new Button.OnClickListener() {
        public void onClick(View arg0) {
            Bundle b = new Bundle();
            b.putBoolean("isScreenRotated", isScreenRotated);
            Intent i = new Intent(getApplicationContext(),  ActivitySelectApps.class);
            i.putExtras(b);
            startActivity(i);
            finish();
        }
    };
}
