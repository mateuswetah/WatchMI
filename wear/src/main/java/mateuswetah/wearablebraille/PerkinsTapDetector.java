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
    private boolean isScreenReversed = false;

    public PerkinsTapDetector(float screenWidth, boolean isScreenReversed) {
        super();
        this.width = screenWidth;
        this.isScreenReversed = isScreenReversed;
    }

    public boolean onTouchEvent(MotionEvent event) {

        switch(event.getActionMasked()) {
            case MotionEvent.ACTION_POINTER_UP:
                if(event.getPointerCount() == 2) {

                    MotionEvent.PointerCoords pointerCoords1 = new MotionEvent.PointerCoords();
                    MotionEvent.PointerCoords pointerCoords2 = new MotionEvent.PointerCoords();
                    event.getPointerCoords(0,pointerCoords1);
                    event.getPointerCoords(1,pointerCoords2);

                    float y1 = pointerCoords1.y;
                    float y2 = pointerCoords2.y;

                    final int line;

                    if (isScreenReversed) {
                        if (y1 - y2 > (width / 5) * 3) { // User has two fingers separate enough
                            line = 2;
                        } else {
                            if (y1 < (width / 3) * 2) {
                                line = 3;
                            } else {
                                line = 1;
                            }
                        }
                    } else {
                        if (y2 - y1 > (width / 5) * 3) { // User has two fingers separate enough
                            line = 2;
                        } else {
                            if (y2 < (width / 3) * 2) {
                                line = 1;
                            } else {
                                line = 3;
                            }
                        }
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

                float y = event.getY();
                final int line;
                if (y < width/3) {
                    line = isScreenReversed ? 3 : 1;
                } else if (y > (width/3)*2) {
                    line = isScreenReversed ? 1 : 3;
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
