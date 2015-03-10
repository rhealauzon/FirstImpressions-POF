package ca.calvinrempel.firstimpressions_pof;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

/**
 * Created by Chris on 2015-03-10.
 */
public class WaitLocationService extends IntentService {

    public WaitLocationService()
    {
        super("WaitLocationService");
    }

    protected void onHandleIntent(Intent workIntent) {
        // Retrieve startup data
        String dataString = workIntent.getDataString();

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setContentTitle("My notification")
                        .setContentText("Hello World!");

        // Sets an ID for the notification
        int mNotificationId = 1;
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }
}
