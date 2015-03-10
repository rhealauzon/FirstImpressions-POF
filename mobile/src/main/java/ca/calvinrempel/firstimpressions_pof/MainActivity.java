package ca.calvinrempel.firstimpressions_pof;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;


public class MainActivity extends Activity
{

    private GoogleApiClient googleClient;
    private FencedMeetingManager meetingManager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        meetingManager = null;

        googleClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();

        googleClient.blockingConnect();
    }

    private void startGeofencing()
    {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Implement the behaviour that occurs when the user enters or exits, or state changes
     */
    private class GeofenceEventListener implements GeofenceListenerCallbacks
    {

        @Override
        public void onEnter(String fenceId) {

        }

        @Override
        public void onExit(String fenceId) {

        }

        @Override
        public void onConnected() {

        }

        @Override
        public void onDisconnected() {

        }

        @Override
        public void onError() {

        }
    }
}
