package mateuswetah.wearablebraille;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.Button;

public class ActivitySelectTech extends WearableActivity {

    private Button btn1, btn2, btn3, btn4, btn5, btn6, btnBack;
    private boolean isStudy = false;
    private boolean isScreenRotated = false;
    private boolean isUsingWordReading = false;
    private boolean isUsingAutoComplete = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_tech);
//        setAmbientEnabled();
        btn1 = (Button) findViewById(R.id.buttonStart);
        btn1.setOnClickListener(button1ClickListener);
        btn2 = (Button) findViewById(R.id.btn2);
        btn2.setOnClickListener(button2ClickListener);
        btn3 = (Button) findViewById(R.id.btn3);
        btn3.setOnClickListener(button3ClickListener);
        btn4 = (Button) findViewById(R.id.btn4);
        btn4.setOnClickListener(button4ClickListener);
        btn5 = (Button) findViewById(R.id.btn5);
        btn5.setOnClickListener(button5ClickListener);
        btn6 = (Button) findViewById(R.id.btn6);
        btn6.setOnClickListener(button6ClickListener);
        btnBack = (Button) findViewById(R.id.btnBack);
        btnBack.setOnClickListener(buttonBackClickListener);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            if (extras.getBoolean("study") == true) {
                isStudy = true;
            } else {
                isStudy = false;
            }
            if (extras.getBoolean("isScreenRotated") == true) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                isScreenRotated = true;
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                isScreenRotated = false;
            }

            if (extras.getBoolean("useWordReading") == true)
                isUsingWordReading = true;
            else
                isUsingWordReading = false;

            if (extras.getBoolean("useSpellCheck") == true)
                isUsingAutoComplete = true;
            else
                isUsingAutoComplete = false;
        }
    }

    Button.OnClickListener button1ClickListener = new Button.OnClickListener() {
        public void onClick(View arg0) {
            Bundle b = new Bundle();
            b.putBoolean("study", isStudy);
            b.putBoolean("isScreenRotated", isScreenRotated);
            b.putBoolean("useWordReading", isUsingWordReading);
            b.putBoolean("useSpellCheck", isUsingAutoComplete);
            Intent i = new Intent(getApplicationContext(),  ActivityTechTouch.class);
            i.putExtras(b);
            startActivity(i);
            finish();
        }
    };
    Button.OnClickListener button2ClickListener = new Button.OnClickListener() {
        public void onClick(View arg0) {
            Bundle b = new Bundle();
            b.putBoolean("study", isStudy);
            b.putBoolean("isScreenRotated", isScreenRotated);
            b.putBoolean("useWordReading", isUsingWordReading);
            b.putBoolean("useSpellCheck", isUsingAutoComplete);
            Intent i = new Intent(getApplicationContext(),  ActivityTechSwipe.class);
            i.putExtras(b);
            startActivity(i);
            finish();
        }
    };
    Button.OnClickListener button3ClickListener = new Button.OnClickListener() {
        public void onClick(View arg0) {
            Bundle b = new Bundle();
            b.putBoolean("study", isStudy);
            b.putBoolean("isScreenRotated", isScreenRotated);
            b.putBoolean("useWordReading", isUsingWordReading);
            b.putBoolean("useSpellCheck", isUsingAutoComplete);
            Intent i = new Intent(getApplicationContext(),  ActivityTechConnect.class);
            i.putExtras(b);
            startActivity(i);
            finish();
        }
    };
    Button.OnClickListener button4ClickListener = new Button.OnClickListener() {
        public void onClick(View arg0) {
            Bundle b = new Bundle();
            b.putBoolean("study", isStudy);
            b.putBoolean("isScreenRotated", isScreenRotated);
            b.putBoolean("useWordReading", isUsingWordReading);
            b.putBoolean("useSpellCheck", isUsingAutoComplete);
            Intent i = new Intent(getApplicationContext(),  ActivityTechPressure.class);
            i.putExtras(b);
            startActivity(i);
            finish();
        }
    };
    Button.OnClickListener button5ClickListener = new Button.OnClickListener() {
        public void onClick(View arg0) {
            Bundle b = new Bundle();
            b.putBoolean("study", isStudy);
            b.putBoolean("isScreenRotated", isScreenRotated);
            b.putBoolean("useWordReading", isUsingWordReading);
            b.putBoolean("useSpellCheck", isUsingAutoComplete);
            Intent i = new Intent(getApplicationContext(),  ActivityTechSerial.class);
            i.putExtras(b);
            startActivity(i);
            finish();
        }
    };
    Button.OnClickListener button6ClickListener = new Button.OnClickListener() {
        public void onClick(View arg0) {
            Bundle b = new Bundle();
            b.putBoolean("study", isStudy);
            b.putBoolean("isScreenRotated", isScreenRotated);
            b.putBoolean("useWordReading", isUsingWordReading);
            b.putBoolean("useSpellCheck", isUsingAutoComplete);
            Intent i = new Intent(getApplicationContext(),  ActivityTechPerkins.class);
            i.putExtras(b);
            startActivity(i);
            finish();
        }
    };
    Button.OnClickListener buttonBackClickListener = new Button.OnClickListener() {
        public void onClick(View arg0) {
            Bundle b = new Bundle();
            b.putBoolean("isScreenRotated", isScreenRotated);
            b.putBoolean("useWordReading", isUsingWordReading);
            b.putBoolean("useSpellCheck", isUsingAutoComplete);
            Intent i = new Intent(getApplicationContext(), ActivityMain.class);
            i.putExtras(b);
            startActivity(i);
            finish();
        }
    };
}

