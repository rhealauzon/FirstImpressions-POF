package ca.calvinrempel.firstimpressions_pof;

import android.app.IntentService;
import android.content.Intent;
import android.os.SystemClock;

/**
 * Created by Chris on 2015-03-10.
 */
public class WaitLocationService extends IntentService {

    /** An enum for the possible states of the waiting service */
    private enum WaitState {
        NEARBY, ARRIVAL, TALKING
    }

    /**
     * @author Chris Klassen
     */
    public WaitLocationService()
    {
        super("WaitLocationService");
    }

    /**
     * @author Chris Klassen
     * @param workIntent an intent containing the wait times for the service
     */
    protected void onHandleIntent(Intent workIntent) {
        // Retrieve start up data
        int nearbyTime = workIntent.getExtras().getInt("nearbyTime");
        int arrivalTime = workIntent.getExtras().getInt("arrivalTime");
        int talkingTime = workIntent.getExtras().getInt("talkingTime");
        WaitState state = WaitState.NEARBY;

        // Loop infinitely until the date ends
        while(true)
        {
            switch(state)
            {
                case NEARBY:
                {
                    // Sleep for the allotted period of time
                    SystemClock.sleep(nearbyTime * 1000);

                    // Check to see if the individual is nearby
                    break;
                }

                case ARRIVAL:
                {
                    // Sleep for the allotted period of time
                    SystemClock.sleep(arrivalTime * 1000);

                    // Check to see if the individual has arrived
                    break;
                }

                case TALKING:
                {
                    // Sleep for the allotted period of time
                    SystemClock.sleep(talkingTime * 1000);

                    // Create a Talking Point card
                    break;
                }
            }
        }
    }
}
