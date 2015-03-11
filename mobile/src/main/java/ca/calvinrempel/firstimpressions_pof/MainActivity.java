package ca.calvinrempel.firstimpressions_pof;

import android.app.Activity;
import android.app.Notification;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity implements MongoReceiver
{
    private FencedMeetingManager meetingManager;

    /** The request code used by this application for voice command */
    private static final int SPEECH_REQUEST_CODE = 0;

    /** The wait time before checking the nearby status of your date again */
    private static final int NEARBY_WAIT = 10;

    /** The wait time before checking the nearby status of your date again */
    private static final int ARRIVAL_WAIT = 5;

    /** The wait time before checking the nearby status of your date again */
    private static final int TALKING_WAIT = 600;

    /** An enumerated list containing all possible voice command options */
    private enum CommandOptions {
        NONE, LOCATION, TIME, LIKES, NOTIFY
    }

    /** A list of possible voice commands to indicate a Location request */
    private List<String> voiceLocationOptions = new ArrayList<>();

    /** A list of possible voice commands to indicate a Time request */
    private List<String> voiceTimeOptions = new ArrayList<>();

    /** A list of possible voice commands to indicate a Likes request */
    private List<String> voiceLikesOptions = new ArrayList<>();

    /** A list of possible voice commands to indicate a Notify request */
    private List<String> voiceNotifyOptions = new ArrayList<>();

    /** A list of all Location details */
    private List<String> voiceLocationDetails = new ArrayList<>();

    /** A list of all Time details */
    private List<String> voiceTimeDetails = new ArrayList<>();

    /** A list of all Likes details */
    private List<String> voiceLikesDetails = new ArrayList<>();

    /** A list of all Notify details */
    private List<String> voiceNotifyDetails = new ArrayList<>();

    // Google API Client
    GoogleApiClient googleClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create the waiting service intent
        Intent msgIntent = new Intent(this, WaitLocationService.class);
        msgIntent.putExtra("nearbyTime", NEARBY_WAIT);
        msgIntent.putExtra("arrivalTime", ARRIVAL_WAIT);
        msgIntent.putExtra("talkingTime", TALKING_WAIT);

        // Start the waiting service
        startService(msgIntent);

        // Set up Location options
        voiceLocationOptions.add("what location");
        voiceLocationOptions.add("where");
        voiceLocationOptions.add("what place");

        // Set up Time options
        voiceTimeOptions.add("what time");
        voiceTimeOptions.add("when");

        // Set up Likes options
        voiceLikesOptions.add("favourite");
        voiceLikesOptions.add("like");
        voiceLikesOptions.add("enjoy");

        // Set up Notify options
        voiceNotifyOptions.add("tell");
        voiceNotifyOptions.add("message");
        voiceNotifyOptions.add("say");

        // Set up Location details
        voiceLocationDetails.add("date");

        // Set up Time details
        voiceTimeDetails.add("date");

        // Set up Likes details
        voiceLikesDetails.add("book");
        voiceLikesDetails.add("music");
        voiceLikesDetails.add("tv");
        voiceLikesDetails.add("movie");
        voiceLikesDetails.add("food");

        // Set up Notify details
        voiceNotifyDetails.add("late");
        voiceNotifyDetails.add("here");
        voiceNotifyDetails.add("can't come");

        sendNotification("Hello", "Test");
    }

    public void onResume()
    {
        super.onResume();

        // Ensure Geofencing is available on app start/restart
        if (meetingManager == null)
        {
            meetingManager = new FencedMeetingManager(new GeofenceEventListener());
        }

        while(!meetingManager.startGeofencing(this))
        {
            Intent gpsOptionsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(gpsOptionsIntent);
        }
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
        public void onNowHere(FencedMeeting meeting)
        {
            Log.d("GEO", "User: " + meeting.getOtherUserId() + " now here.");
        }

        public void onNoLongerHere(FencedMeeting meeting)
        {
            Log.d("GEO", "User: " + meeting.getOtherUserId() + " no longer here.");
        }

        public void onNowNear(FencedMeeting meeting)
        {
            Log.d("GEO", "User: " + meeting.getOtherUserId() + " now near.");
        }

        public void onNoLongerNear(FencedMeeting meeting)
        {
            Log.d("GEO", "User: " + meeting.getOtherUserId() + " no longer near.");
        }
    }

    /**
     * @author Chris Klassen
     * @param v the calling view
     */
    public void startVoiceCommand(View v)
    {
        // Create the voice command intent
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        // Start the intent
        startActivityForResult(intent, SPEECH_REQUEST_CODE);
    }

    /**
     * @author Chris Klassen
     * @param requestCode the request code of the voice command intent
     * @param resultCode the result code of the voice command
     * @param data the data received from the voice command
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        // If this is called from within our app and the voice command succeeded
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK)
        {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);

            // Retrieve the voice command text
            String speechText = results.get(0);

            // Handle the command
            CommandOptions cType;

            if ((cType = getVoiceCommandType(speechText)) == CommandOptions.NONE)
            {
                // There was no command
                Toast.makeText(this, "Invalid command identified.", Toast.LENGTH_SHORT).show();
            }
            else
            {
                //Toast.makeText(this, "Command: " + cType, Toast.LENGTH_SHORT).show();

                // Interpret the command details
                if (!interpretCommand(cType, speechText))
                {
                    Toast.makeText(this, "Invalid command details.", Toast.LENGTH_SHORT).show();
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * @author Chris Klassen
     * @param command the voice command to interpret
     * @return the type of command identified
     */
    private CommandOptions getVoiceCommandType(String command) {
        // Identify the nature of the request
        for (String option : voiceLocationOptions) {
            if (command.toLowerCase().contains(option)) {
                // Location request
                return CommandOptions.LOCATION;
            }
        }

        for (String option : voiceTimeOptions) {
            if (command.toLowerCase().contains(option)) {
                // Location request
                return CommandOptions.TIME;
            }
        }

        for (String option : voiceLikesOptions) {
            if (command.toLowerCase().contains(option)) {
                // Location request
                return CommandOptions.LIKES;
            }
        }

        for (String option : voiceNotifyOptions) {
            if (command.toLowerCase().contains(option)) {
                // Location request
                return CommandOptions.NOTIFY;
            }
        }

        return CommandOptions.NONE;
    }

    /**
     * @author Chris Klassen
     * @param cType the type of command identified
     * @param command the command body
     * @return whether or not a valid command was identified
     */
    private boolean interpretCommand(CommandOptions cType, String command) {
        Toast.makeText(this, command, Toast.LENGTH_LONG).show();

        switch (cType) {
            case LOCATION: {
                for (String option : voiceLocationDetails) {
                    if (command.toLowerCase().contains(option)) {
                        // Location request
                        Toast.makeText(this, "LOCATION details: " + option, Toast.LENGTH_SHORT).show();
                        return true;
                    }
                }

                break;
            }

            case TIME: {
                for (String option : voiceTimeDetails) {
                    if (command.toLowerCase().contains(option)) {
                        // Location request
                        Toast.makeText(this, "TIME details: " + option, Toast.LENGTH_SHORT).show();
                        return true;
                    }
                }

                break;
            }

            case LIKES: {
                for (String option : voiceLikesDetails) {
                    if (command.toLowerCase().contains(option)) {
                        // Location request
                        Toast.makeText(this, "LIKES details: " + option, Toast.LENGTH_SHORT).show();
                        return true;
                    }
                }

                break;
            }

            case NOTIFY: {
                for (String option : voiceNotifyDetails) {
                    if (command.toLowerCase().contains(option)) {
                        // Location request
                        Toast.makeText(this, "NOTIFY details: " + option, Toast.LENGTH_SHORT).show();
                        return true;
                    }
                }

                break;
            }

            default: {
                // We should never get here
            }
        }

        return false;
    }

    // SAMPLE CODE FOR GETTING A PROFILE BY ID
    public void getProfile( View v )
    {
        // Get the id number from the EditText box
        int id = Integer.parseInt(((EditText) findViewById(R.id.txtId)).getText().toString());

        // Result TextView
        final TextView resultText = (TextView)findViewById(R.id.txtResult);

        // getProfile takes a handler and an integer ID
        Mongo.getProfile(
                // Anonymous inner class handler for result of Mongo call
                new MongoReceiver() {
                    @Override
                    public void process(JSONArray result) {
                        try {
                            // Set the result as the first object in the returned array
                            resultText.setText(result.getJSONObject(0).toString(2));
                        }catch (JSONException e){}
                    }
                } ,id );
    }

    private Meeting m;
    // SAMPLE CODE FOR GETTING A MEETING BY USER ID
    public void getMeeting( View v )
    {
        // Get the id number from the EditText box
        int id = Integer.parseInt(((EditText) findViewById(R.id.txtId)).getText().toString());

        // getMeetings takes a handler and an integer ID for the user you're searching for
        Mongo.getMeetings(this, id);
    }

    @Override
    public void process(JSONArray result) {
        PutDataRequest request;
        try {
            // Set the result as the first object in the returned array
            m = new Meeting(result.getJSONObject(0));
            request = PutDataRequest.create("meet/one");
            request.setData( m.serialize() );
        }catch (Exception e){}
    }

    /**
     * @Author Rhea Lauzon
     * @param title -- Title of the notification
     * @param description -- Description of the notification
     * Sends a notification to the wearable
     */
    public void sendNotification(String title, String description)
    {
        //add notification features
        NotificationCompat.WearableExtender wearableExtender =
                new NotificationCompat.WearableExtender()
                        .setHintShowBackgroundOnly(true);

        Notification notification =
                new NotificationCompat.Builder(this)
                        .setVibrate(new long[] {100, 250, 100, 250, 100, 25})
                        .setLights(Color.BLUE, 500, 500)
                        .setSmallIcon(R.drawable.fish)
                        .setLargeIcon(BitmapFactory.decodeResource(
                                getResources(), R.drawable.fishes))
                        .setColor(getResources().getColor(R.color.wallet_holo_blue_light))
                        .setContentTitle(title)
                        .setContentText(description)
                        .extend(wearableExtender)
                        .build();

        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);

        //fire off a notification
        int notificationId = 1;
        notificationManager.notify(notificationId, notification);
        googleClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
    }

}
