package mateuswetah.wearablebraille;

import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

public abstract class PerkinsTapDetector {
    private static final int TIMEOUT = ViewConfiguration.getDoubleTapTimeout() + 750;
    private float width;
    private boolean doubleTapApplied = false;
    private boolean waitForDoubleSingleTap = false;

    public PerkinsTapDetector(float screenWidth) {
        super();
        this.width = screenWidth;
    }

    public boolean onTouchEvent(MotionEvent event) {

        switch(event.getActionMasked()) {
            case MotionEvent.ACTION_POINTER_UP:
                if(event.getPointerCount() == 2) {

                    float x = event.getX();
                    final int line;
                    if (x < width/3) {
                        line = 1;
                    } else if (x > (width/3)*2) {
                        line = 3;
                    } else {
                        line = 2;
                    }

                    onPerkinsDoubleTap(line);
                    doubleTapApplied = true;
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            doubleTapApplied = false;
                        }
                    }, 1000);
                    return false;
                }
                break;

            case MotionEvent.ACTION_UP:

                float x = event.getX();
                final int line;
                if (x < width/3) {
                    line = 1;
                } else if (x > (width/3)*2) {
                    line = 3;
                } else {
                    line = 2;
                }

                if (!doubleTapApplied) {

                    waitForDoubleSingleTap = true;

                    // 800ms of opportunity to perform a double tap composed by two single taps
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!waitForDoubleSingleTap)
                                onPerkinsDoubleTap(line);
                            else
                                onPerkinsSingleTap(line);
                            waitForDoubleSingleTap = false;
                        }
                    }, 800);
                }
                return true;

            case MotionEvent.ACTION_CANCEL:
                break;
        }

        return false;
    }

    public abstract void onPerkinsDoubleTap(int line);
    public abstract void onPerkinsSingleTap(int line);

}
