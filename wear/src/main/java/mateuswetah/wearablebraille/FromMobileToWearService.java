package mateuswetah.wearablebraille;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class FromMobileToWearService extends WearableListenerService {

    public static final String SUGGESTIONS_PATH = "/response/MainActivity";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        Log.d("FROM MOBILE TO WEAR", String.valueOf(messageEvent.getData()));
        if (messageEvent.getPath().equals(SUGGESTIONS_PATH)) {
            Log.d("FROM MOBILE TO WEAR", String.valueOf(messageEvent.getData()));
        }
    }
}


