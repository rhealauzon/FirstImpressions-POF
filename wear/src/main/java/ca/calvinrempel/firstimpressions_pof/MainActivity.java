package ca.calvinrempel.firstimpressions_pof;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.wearable.view.WatchViewStub;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private TextView mTextView;

    /** The request code used by this application for voice command */
    private static final int SPEECH_REQUEST_CODE = 0;

    /** The wait time before checking the nearby status of your date again */
    private static final int NEARBY_WAIT = 10;

    /** The wait time before checking the nearby status of your date again */
    private static final int ARRIVAL_WAIT = 5;

    /** The wait time before checking the nearby status of your date again */
    private static final int TALKING_WAIT = 600;

    /** The message to send when the Late notification is started */
    private static final String LATE_MESSAGE = "Hi! I'm running a little late, but I'll be there as soon as I can!";

    /** The message to send when the Here notification is started */
    private static final String HERE_MESSAGE = "Hi! I just got here. See you soon!";

    /** The message to send when the Can't Come notification is started */
    private static final String CANT_COME_MESSAGE = "Hi. Something came up last minute and I can't make it today. I'm sorry!";

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

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);

        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener()
        {
            @Override
            public void onLayoutInflated(WatchViewStub stub)
            {
                mTextView = (TextView) stub.findViewById(R.id.text);

                // Set the custom fonts for this page
                FontManager.setFont(stub.getContext(), (Button) findViewById(R.id.buttonDateInfo), "biko.otf");
            }
        });

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
    }

    /**
     * onClick listener for the date info button
     * @author: Rhea Lauzon
     * @param v - The Calling view
     */
    public void dateInfo(View v)
    {
        Intent i = new Intent(this, DateInfo.class);
        startActivity(i);

        return;
    }

    /**
     * onClick listener for the contact info button
     * @author: Rhea Lauzon
     * @param v  - The Calling view
     */
    public void contactInfo (View v)
    {
        Intent i = new Intent(this, ContactInfo.class);
        startActivity(i);

        return;
    }

    /**
     * onClick listener for the likes button
     * @author: Rhea Lauzon
     * @param v - The Calling view
     */
    public void likes(View v)
    {
        Intent i = new Intent(this, Likes.class);
        startActivity(i);
    }

    /**
     * onClick listener for the photo displayed
     * @author Rhea Lauzon
     * @param v - Calling view
     */
    public void bigFace(View v)
    {
        Intent i = new Intent(this, Face.class);
        startActivity(i);
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
                Toast.makeText(this, "Invalid voice command.", Toast.LENGTH_SHORT).show();
            }
            else
            {
                // Interpret the command details
                if (!interpretCommand(cType, speechText))
                {
                    Toast.makeText(this, "Invalid voice command details.", Toast.LENGTH_SHORT).show();
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
        Intent voiceIntent;

        switch (cType) {
            case LOCATION: {
                for (String option : voiceLocationDetails) {
                    if (command.toLowerCase().contains(option)) {
                        // Location request
                        if (option.equals("date"))
                        {
                            voiceIntent = new Intent(this, MainActivity.class);
                            startActivity(voiceIntent);
                        }

                        return true;
                    }
                }

                break;
            }

            case TIME: {
                for (String option : voiceTimeDetails) {
                    if (command.toLowerCase().contains(option)) {
                        // Time request
                        if (option.equals("date"))
                        {
                            voiceIntent = new Intent(this, MainActivity.class);
                            startActivity(voiceIntent);
                        }

                        return true;
                    }
                }

                break;
            }

            case LIKES: {
                for (String option : voiceLikesDetails) {
                    if (command.toLowerCase().contains(option)) {
                        // Likes request
                        if (option.equals("book"))
                        {
                            voiceIntent = new Intent(this, LikesDetails.class);
                            voiceIntent.putExtra("category", "book");
                            startActivity(voiceIntent);
                        }
                        else if (option.equals("movie"))
                        {
                            voiceIntent = new Intent(this, LikesDetails.class);
                            voiceIntent.putExtra("category", "movie");
                            startActivity(voiceIntent);
                        }
                        else if (option.equals("music"))
                        {
                            voiceIntent = new Intent(this, LikesDetails.class);
                            voiceIntent.putExtra("category", "music");
                            startActivity(voiceIntent);
                        }
                        else if (option.equals("tv"))
                        {
                            voiceIntent = new Intent(this, LikesDetails.class);
                            voiceIntent.putExtra("category", "tv");
                            startActivity(voiceIntent);
                        }
                        else if (option.equals("food"))
                        {
                            voiceIntent = new Intent(this, LikesDetails.class);
                            voiceIntent.putExtra("category", "food");
                            startActivity(voiceIntent);
                        }

                        return true;
                    }
                }

                break;
            }

            case NOTIFY: {
                String phoneNumber = "6048129538";

                for (String option : voiceNotifyDetails) {
                    if (command.toLowerCase().contains(option)) {
                        // Notify request
                        if (option.equals("late"))
                        {
                            sendTextMessage(phoneNumber, LATE_MESSAGE);
                        }
                        else if (option.equals("here"))
                        {
                            sendTextMessage(phoneNumber, HERE_MESSAGE);
                        }
                        else if (option.equals("can't come"))
                        {
                            sendTextMessage(phoneNumber, CANT_COME_MESSAGE);
                        }

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

    /**
     * @author Chris Klassen
     * @param pNumber the phone number to send to
     * @param message the message to send
     */
    public void sendTextMessage(String pNumber, String message)
    {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(pNumber, null, message, null, null);
    }
}
