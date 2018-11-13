package mateuswetah.wearablebraille.GestureDetectors;

import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

public abstract class TwoFingersSwipeDetector {
    private static final int SWIPE_MIN_DISTANCE = 60;
    private static final int NONE = 0;
    private static final int SWIPE = 1;
    public int mode = NONE;

    private float startX;
    private float stopX;

    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK)
        {
            case MotionEvent.ACTION_POINTER_DOWN:
                // This happens when you touch the screen with two fingers
                mode = SWIPE;
                startX = (event.getX(0) + event.getX(1))/2;
                break;
            case MotionEvent.ACTION_UP:
                mode = NONE;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                // This happens when you release the second finger
                mode = NONE;
                if(Math.abs(stopX - startX) > SWIPE_MIN_DISTANCE) {
                    if(startX <= stopX) {
                        Log.d("TWO FINGERS SWIPE", "RIGHT");
                        onTwoFingersSwipeRight();
                    } else {
                        Log.d("TWO FINGERS SWIPE", "LEFT");
                        onTwoFingersSwipeLeft();
                    }
                    return true;
                }
                mode = NONE;
                break;

            case MotionEvent.ACTION_MOVE:
                if(mode == SWIPE) {
                    stopX = (event.getX(0) + event.getX(1))/2;
                }
                break;
        }
        return false;
    }

    protected abstract void onTwoFingersSwipeLeft();

    protected abstract void onTwoFingersSwipeRight();

}
