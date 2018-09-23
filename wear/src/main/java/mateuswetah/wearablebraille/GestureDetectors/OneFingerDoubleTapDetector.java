package mateuswetah.wearablebraille.GestureDetectors;

import android.view.MotionEvent;
import android.view.ViewConfiguration;

/** source from
 * http://stackoverflow.com/questions/12414680/how-to-implement-a-two-finger-double-click-in-android
 * */

public abstract class OneFingerDoubleTapDetector {
    private static final int TIMEOUT = ViewConfiguration.getDoubleTapTimeout() + 100;
    private long mFirstDownTime = 0;
    private byte mOneFingerTapCount = 0;

    private void reset(long time) {
        mFirstDownTime = time;
        mOneFingerTapCount = 0;
    }

    public boolean onTouchEvent(MotionEvent event) {
        switch(event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if(mFirstDownTime == 0 || event.getEventTime() - mFirstDownTime > TIMEOUT)
                    reset(event.getDownTime());
                else
                    mOneFingerTapCount++;
                break;
            case MotionEvent.ACTION_UP:
                if(mOneFingerTapCount == 1 && event.getEventTime() - mFirstDownTime < TIMEOUT) {
                    onOneFingerDoubleTap();
                    mFirstDownTime = 0;
                    return true;
                }
        }

        return false;
    }

    public abstract void onOneFingerDoubleTap();
}
