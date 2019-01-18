package mateuswetah.wearablebraille.BrailleÉcran;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

public class BrailleÉcranAccessibilityService extends AccessibilityService {

    AccessibilityServiceInfo serviceInfo = new AccessibilityServiceInfo();

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
    }

    @Override
    public void onInterrupt() {
    }

    private void disableTouchExploration() {

        serviceInfo.flags &= ~AccessibilityServiceInfo.DEFAULT;

        setServiceInfo(serviceInfo);
    }

    @Override
    public void onServiceConnected() {

        Log.d("BRAILLEÉCRAN SERVICE", "CONNECTED");
        serviceInfo.packageNames = new String[]{"mateuswetah.wearablebraille"};
        setServiceInfo(serviceInfo);

        disableTouchExploration();

    }

    @Override
    protected boolean onGesture(int gestureId) {
        return true;
    }

}
