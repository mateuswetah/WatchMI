package mateuswetah.wearablebraille;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class FromMobileToWearService extends WearableListenerService {

    public static final String SUGGESTIONS_PATH = "/response/MainActivity";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        if (messageEvent.getPath().equals(SUGGESTIONS_PATH)) {
            // The string "received-message" will be used to filer the intent
            Intent intent = new Intent("received-message");
            // Adding some data
            intent.putExtra("receivedMessage", new String(messageEvent.getData()));
            this.sendBroadcast(intent);
        }
    }

}


