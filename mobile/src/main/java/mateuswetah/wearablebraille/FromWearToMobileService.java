package mateuswetah.wearablebraille;

import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.net.sip.SipSession;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.textservice.SentenceSuggestionsInfo;
import android.view.textservice.SpellCheckerSession;
import android.view.textservice.SuggestionsInfo;
import android.view.textservice.TextInfo;
import android.view.textservice.TextServicesManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

public class FromWearToMobileService extends WearableListenerService implements SpellCheckerSession.SpellCheckerSessionListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private final IBinder mBinder = new MyBinder();
    private static final String SPELLCHECKER_WEAR_PATH = "/message-to-spellchecker";
    public static final String SUGGESTIONS_PATH = "/response/MainActivity";

    TextServicesManager tsm;
    SpellCheckerSession session;

    // GoogleApiClient, needed for starting the watch activity on cast connect
    private static GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate() {
        super.onCreate();
        // Setting up play services connection for wearable activity instantiation on cast connect.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();

        tsm = (TextServicesManager) getSystemService(TEXT_SERVICES_MANAGER_SERVICE);
        session = tsm.newSpellCheckerSession(null, null, this, true);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_NOT_STICKY;
    }

    private void fetchSuggestionsFor(String input){

        if (session!= null ) {
            session.getSentenceSuggestions(new TextInfo[]{ new TextInfo(input) }, 5);
        } else {
            ComponentName componentToLaunch = new ComponentName("com.android.settings",
                    "com.android.settings.Settings$SpellCheckersSettingsActivity");
            Intent intent = new Intent();
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setComponent(componentToLaunch);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            try {
                this.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Log.d("ACTIVITY", "Can't find settings activity.");
            }
        }
    }

    @Override
    public void onGetSuggestions(SuggestionsInfo[] suggestionsInfos) {
    }

    @Override
    public void onGetSentenceSuggestions(SentenceSuggestionsInfo[] results) {

        final StringBuffer sb = new StringBuffer("");
        final ArrayList<String> suggestions = new ArrayList<String>();
        for(SentenceSuggestionsInfo result:results){
            int n = result.getSuggestionsCount();
            for(int i=0; i < n; i++){
                int m = result.getSuggestionsInfoAt(i).getSuggestionsCount();

                for(int k=0; k < m; k++) {
                    sb.append(result.getSuggestionsInfoAt(i).getSuggestionAt(k))
                            .append("\n");
                    suggestions.add(result.getSuggestionsInfoAt(i).getSuggestionAt(k));
                }
                sb.append("\n");
            }
        }

        if (mGoogleApiClient != null) {
            Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(
                    new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                        @Override
                        public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                            for (Node node : getConnectedNodesResult.getNodes()) {
                                if (suggestions.size() > 0) {
                                    Log.d("SPELL CHECKER", "Suggestions:" + suggestions.toString());
                                    byte[] byteSuggestions = new byte[0];
                                    try {
                                        byteSuggestions = suggestions.toString().getBytes("UTF-8");
                                    } catch (UnsupportedEncodingException e) {
                                        e.printStackTrace();
                                    }
                                    Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), SUGGESTIONS_PATH, byteSuggestions).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                                        @Override
                                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                                            if (!sendMessageResult.getStatus().isSuccess()) {
                                                Log.e("GoogleApi", "Failed to send message with status code: "
                                                        + sendMessageResult.getStatus().getStatusCode());
                                            } else {
                                                Log.d("GoogleApi", "Message sent with success.");
                                            }
                                        }
                                    });
                                } else {
                                    Log.d("SPELL CHECKER", "Sorry, no suggestions do send.");
                                }
                            }
                        }
                    });
        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);

        if (messageEvent.getPath().equals(SPELLCHECKER_WEAR_PATH)) {
            String receivedMessage = new String(messageEvent.getData());
            Log.d("FROM WEAR TO MOBILE", "Mensagem chegou: " +  receivedMessage);
            fetchSuggestionsFor(receivedMessage);
        }
    }

    public class MyBinder extends Binder {
        FromWearToMobileService getService() {
            return FromWearToMobileService.this;
        }
    }

    @Override
    public void onPeerConnected(com.google.android.gms.wearable.Node peer) {
        super.onPeerConnected(peer);

        String id = peer.getId();
        String name = peer.getDisplayName();

        Log.d("MOBILE", "Connected peer name & ID: " + name + "|" + id);

    }

    @Override
    public void onPeerDisconnected(com.google.android.gms.wearable.Node peer) {

        String id = peer.getId();
        String name = peer.getDisplayName();

        Log.d("MOBILE", "Disconnected peer name & ID: " + name + "|" + id);
    }


    /* GOOGLE PLAY SERVICES RELATED------------------------------------------
        Necessary for starting the Wear activity once cast is connected.
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d("GoogleApi", "onConnected: " + bundle);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("GoogleApi", "onConnectionSuspended: " + i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("GoogleApi", "onConnectionFailed: " + connectionResult);
    }



}

