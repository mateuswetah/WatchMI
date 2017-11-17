package mateuswetah.wearablebraille;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

public class ActivityMain extends WearableActivity {

    private Button btn1, btn2, btn3;
    private Switch screenRotateSwitch;
    private Boolean isScreenRotated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        setAmbientEnabled();
        btn1 = (Button) findViewById(R.id.btn1);
        btn1.setOnClickListener(button1ClickListener);
        btn2 = (Button) findViewById(R.id.btn2);
        btn2.setOnClickListener(button2ClickListener);
        btn3 = (Button) findViewById(R.id.btn3);
        btn3.setOnClickListener(button3ClickListener);
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
