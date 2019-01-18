package mateuswetah.wearablebraille;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.wearable.view.DelayedConfirmationView;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

/**
 * Created by mateus on 31/12/16.
 */
public class MobileConnectedConfirmationActivity extends Activity {

    DelayedConfirmationView delayedConfirmationView;
    boolean isConnected;

    private GoogleApiClient mGoogleApiClient;
    private Node mNode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connected_cofirmation_activity);

        delayedConfirmationView = (DelayedConfirmationView) findViewById(R.id.mobile_connected_confirmation);
        delayedConfirmationView.setTotalTimeMs(3000);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient)
                                .setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                                    @Override
                                    public void onResult(NodeApi.GetConnectedNodesResult nodes) {
                                        for (Node node : nodes.getNodes()) {
                                            if (node != null && node.isNearby()) {
                                                mNode = node;
                                                Log.d("CONNECTED CONFIRMATION", "Connected to " + mNode.getDisplayName());

                                                String id = mNode.getId();
                                                String name = mNode.getDisplayName();

                                                Log.d("WEAR CONNECTION", "Connected peer name & ID: " + name + "|" + id);
                                                isConnected = true;
                                            }
                                        }
                                        if (mNode == null) {
                                            Log.d("WEAR CONNECTION", "Not connected");
                                            isConnected = false;
                                        }
                                    }
                                });
                    }

                    @Override
                    public void onConnectionSuspended(int i) {}
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                })
                .build();

        delayedConfirmationView.setListener(new DelayedConfirmationView.DelayedConfirmationListener() {

            @Override
            public void onTimerFinished(View view) {

                if (isConnected) {
                    finish();
                } else {
                    delayedConfirmationView.start();
                }
            }

            @Override
            public void onTimerSelected(View view) {}



        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
        delayedConfirmationView.start();
    }

}
