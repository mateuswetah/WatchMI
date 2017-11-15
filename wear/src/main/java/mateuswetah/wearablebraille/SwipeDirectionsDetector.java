package mateuswetah.wearablebraille;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * Created by orpheus on 15/11/17.
 */

public abstract class SwipeDirectionsDetector extends GestureDetector.SimpleOnGestureListener {

    private String LOGTAG = "SWIPE";

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2,
                           float velocityX, float velocityY) {
        switch (getSlope(e1.getX(), e1.getY(), e2.getX(), e2.getY())) {
            case 1:
                Log.d(LOGTAG, "top right");
                this.onTopRightSwipe();
                return true;
            case 2:
                Log.d(LOGTAG, "top left");
                this.onTopLeftSwipe();
                return true;
            case 3:
                Log.d(LOGTAG, "middle left");
                this.onMiddleLeftSwipe();
                return true;
            case 4:
                Log.d(LOGTAG, "bottom left");
                this.onBottomLeftSwipe();
                return true;
            case 5:
                Log.d(LOGTAG, "bottom right");
                this.onBottomRightSwipe();
                return true;
            case 6:
                Log.d(LOGTAG, "middle right");
                this.onMiddleRightSwipe();
                return true;
        }
        return false;
    }

    private int getSlope(float x1, float y1, float x2, float y2) {
        Double angle = Math.toDegrees(Math.atan2(y1 - y2, x2 - x1));
        if (angle > (45/2) && angle <= 45 + (45/2))
            // top right
            return 1;
        if (angle > 135 - (45/2) && angle <= 135 + (45/2))
            // top left
            return 2;
        if (angle > 135 + (45/2) && angle <= 180 + (45/2))
            // middle left
            return 3;
        if (angle > -135 - (45/2) && angle <= -135 + (45/2))
            // bottom left
            return 4;
        if (angle > -45 - (45/2) && angle <= -45 + (45/2))
            // bottom right
            return 5;
        if (angle > -45 + (45/2) && angle <= 45 - (45/2))
            // right
            return 6;
        return 0;
    }

    public abstract void onTopLeftSwipe();
    public abstract void onTopRightSwipe();
    public abstract void onMiddleLeftSwipe();
    public abstract void onMiddleRightSwipe();
    public abstract void onBottomLeftSwipe();
    public abstract void onBottomRightSwipe();

}
