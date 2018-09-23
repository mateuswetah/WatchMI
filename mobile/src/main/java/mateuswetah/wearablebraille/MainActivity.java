package mateuswetah.wearablebraille;

import android.app.IntentService;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.textservice.SentenceSuggestionsInfo;
import android.view.textservice.SpellCheckerSession;
import android.view.textservice.SuggestionsInfo;
import android.view.textservice.TextInfo;
import android.view.textservice.TextServicesManager;
import android.widget.EditText;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = (EditText) findViewById(R.id.editText);
        editText.setInputType(InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);

        Intent intent = new Intent(this, ListenerServiceFromWear.class);
        startService(intent);
    }

    /* LISTENER SERVICE - GETS MOVEMENTS DATA FROM WEAR -------------------------------------------
        Here is implemented the actions to be done when the Wear sends a message
     */
    public static class ListenerServiceFromWear extends WearableListenerService implements SpellCheckerSession.SpellCheckerSessionListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

        private static final String SPELLCHECKER_WEAR_PATH = "/gesture-from-wear";
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
            session = tsm.newSpellCheckerSession(null, Locale.getDefault(), this, true);

        }

        private void fetchSuggestionsFor(String input){

            if (session!= null ) {

                session.getSentenceSuggestions(
                        new TextInfo[]{ new TextInfo(input) },
                        5
                );
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
            for(SentenceSuggestionsInfo result:results){
                int n = result.getSuggestionsCount();
                for(int i=0; i < n; i++){
                    int m = result.getSuggestionsInfoAt(i).getSuggestionsCount();

                    for(int k=0; k < m; k++) {
                        sb.append(result.getSuggestionsInfoAt(i).getSuggestionAt(k))
                                .append("\n");
                    }
                    sb.append("\n");
                }
            }

            if (mGoogleApiClient != null) {
                Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                        for (Node node : getConnectedNodesResult.getNodes()) {
                            byte[] suggestions = new byte[0];
                            try {
                                suggestions = (sb.toString()).getBytes("UTF-8");
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }

                            Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), SUGGESTIONS_PATH, suggestions).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
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
                Log.d("MENSAGEM CHEGOU!", receivedMessage);
                fetchSuggestionsFor(receivedMessage);
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
}
