package mateuswetah.wearablebraille;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.WindowManager;

public abstract class SerialTapDetector {
    private static final int TIMEOUT = ViewConfiguration.getDoubleTapTimeout() + 750;
    private float width;
    private boolean doubleTapApplied = false;
    private boolean waitForDoubleSingleTap = false;

    public SerialTapDetector(float screenWidth) {
        super();
        this.width = screenWidth;
    }

    public boolean onTouchEvent(MotionEvent event) {

        switch(event.getActionMasked()) {
            case MotionEvent.ACTION_POINTER_UP:
                if(event.getPointerCount() == 2) {
                    onSerialDoubleTap();
                    doubleTapApplied = true;
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            doubleTapApplied = false;
                        }
                    }, 100);
                    return false;
                }
                break;

            case MotionEvent.ACTION_UP:

                float x = event.getX();
                final boolean isRightSide = (x > width/2);

                if (!doubleTapApplied) {

                    waitForDoubleSingleTap = true;

                    // 10ms of opportunity to perform a double tap composed by two single taps
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!waitForDoubleSingleTap)
                                onSerialDoubleTap();
                            else
                                onSerialSingleTap(isRightSide);
                            waitForDoubleSingleTap = false;

                        }
                    }, 10);
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
        }

        return false;
    }

    public abstract void onSerialDoubleTap();
    public abstract void onSerialSingleTap(boolean isRightSide);

}
