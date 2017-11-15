package mateuswetah.wearablebraille;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.support.wearable.view.WatchViewStub;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by orpheus on 15/11/17.
 */

public class ActivityTechSerial extends WearableActivity{
    private BoxInsetLayout mContainerView;
    private DrawView drawView;
    private TextView tv1, tv2, tv3;
    TwoFingersDoubleTapDetector twoFingersListener;
    private SensorManager mSensorManager;
    private Sensor mSensor;

    boolean started = false;
    boolean stopped = true;
    boolean isStudy = false;
    boolean reset = false;

    int trialCount = 0;
    Util util;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_braille_core_stub);
//region Bundle getIntent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.getBoolean("study") == true) {
                isStudy = true;
                Toast.makeText(getApplicationContext(), "User study mode", Toast.LENGTH_SHORT).show();
            } else isStudy = false;
        }
//endregion
        WatchViewStub stub = (WatchViewStub) findViewById(R.id.stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mContainerView = (BoxInsetLayout) findViewById(R.id.container);
                drawView = (DrawView) findViewById(R.id.draw_view);
                mContainerView.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                    @Override
                    public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                        int mChinSize = insets.getSystemWindowInsetBottom();
                        v.onApplyWindowInsets(insets);
                        drawView.setShape(mContainerView.isRound(), mChinSize);
                        drawView.setDrawModes(DrawView.DrawModes.PRESSURE);
                        drawView.setStudyModes(isStudy);
                        return insets;
                    }
                });
                setTouchListener();
                tv1 = (TextView) findViewById(R.id.tv1);
                tv2 = (TextView) findViewById(R.id.tv2);
                tv3 = (TextView) findViewById(R.id.tv3);
                if (!isStudy) {
                    tv1.setText("PRESS on any side of screen (near the edge) with varying pressure!");
                    tv3.setText("double tap with 2 fingers to exit");
                } else {
                    //InitTrials();
                    //SetNextTrial();
                    tv1.setText("Correct/Wrong");
                    tv3.setText("Trial:" + trialCount);
                }
            }
        });

    }

    void setTouchListener() {
        mContainerView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                twoFingersListener.onTouchEvent(event);
                return false;
            }
        });
    }

}
