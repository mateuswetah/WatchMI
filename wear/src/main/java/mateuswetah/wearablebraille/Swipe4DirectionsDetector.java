package mateuswetah.wearablebraille;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

/** source adapted from
 * https://vshivam.wordpress.com/2014/04/28/detecting-up-down-left-right-swipe-on-android/
 * */
public abstract class Swipe4DirectionsDetector extends GestureDetector.SimpleOnGestureListener {

    private static final String LOGTAG = "SWIPE";
    private static final int SWIPE_MIN_DISTANCE = 100;

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2,
                           float velocityX, float velocityY) {

        switch (getSlope(e1.getX(), e1.getY(), e2.getX(), e2.getY())) {
            case 1:
                Log.d(LOGTAG, "top");
                this.onTopSwipe();
                return true;
            case 2:
                Log.d(LOGTAG, "left");
                this.onLeftSwipe();
                return true;
            case 3:
                Log.d(LOGTAG, "bottom");
                this.onBottomSwipe();
                return true;
            case 4:
                Log.d(LOGTAG, "right");
                this.onRightSwipe();
                return true;
        }
        return false;
    }

    private int getSlope(float x1, float y1, float x2, float y2) {

        // Checks if user performed a minimum amount of swipe
        if (Math.hypot(y1 - y2, x2 - x1) < SWIPE_MIN_DISTANCE){
            return 0;
        }

        // Uses angle to determine swipe direction
        Double angle = Math.toDegrees(Math.atan2(y1 - y2, x2 - x1));
        if (angle > 45 && angle <= 135)
            // top
            return 1;
        if (angle >= 135 && angle < 180 || angle < -135 && angle > -180)
            // left
            return 2;
        if (angle < -45 && angle>= -135)
            // bottom
            return 3;
        if (angle > -45 && angle <= 45)
            // right
            return 4;
        return 0;
    }

    // Methods to be implemented on activity
    public abstract void onTopSwipe();
    public abstract void onLeftSwipe();
    public abstract void onRightSwipe();
    public abstract void onBottomSwipe();

}
