package ca.calvinrempel.firstimpressions_pof;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * FencedMeetingManager holds a collection of FencedMeetings which can be added to and are
 * removed after they have expired and the user has left the meeting.
 *
 * It also handles GeoFencing and provides callback mechanisms for when the user arrives at or
 * leaves a FencedMeeting.
 *
 * Created by Calvin on 2015-03-10.
 */
public class FencedMeetingManager
{
    private static final float FENCE_RADIUS_METERS = 500;
    private static final int TIME_BOUND_MS = 15 * 60 * 1000;    // 15 Minutes

    private Map<String, FencedMeeting> meetings;
    private GeofencingRequest.Builder geofencingRequestBuilder;
    private GeofencingRequest geofencingRequest;
    private Intent geofencingIntent;
    private GeofenceListenerCallbacks eventCallbacks;

    public FencedMeetingManager(GeofenceListenerCallbacks callbacks)
    {
        meetings = new HashMap <String, FencedMeeting>();
        geofencingRequestBuilder = new GeofencingRequest.Builder();
        geofencingRequest = null;
        geofencingIntent = null;
        eventCallbacks = callbacks;
    }

    /**
     * Start Geofencing service
     */
    public void startGeofencing(Context context)
    {
        if (geofencingIntent != null)
        {
            geofencingIntent = new Intent(context, GeofenceListener.class);
            PendingIntent.getService(context, 0, geofencingIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
    }

    /**
     * Create a new Meeting to be added to the Geofencing tracker
     *
     * @param userID the ID of the other user in the meeting
     * @param location the location of the meeting
     * @param meetingTime the time of the meeting
     * @return true if the meeting was added, false if the meeting could not be added
     */
    public boolean createMeeting(int userID, Location location, Calendar meetingTime)
    {
        // Only add the Meeting if after current date
        if( Calendar.getInstance().getTimeInMillis() > meetingTime.getTimeInMillis() )
        {
            FencedMeeting meeting = new FencedMeeting(meetingTime, location, userID);
            GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

            // Create a new Fence for the given location
            Geofence fence = new Geofence.Builder()
                                    .setCircularRegion(location.getLatitude(),
                                                       location.getLongitude(),
                                                       FencedMeetingManager.FENCE_RADIUS_METERS)
                                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                                    .build();

            // Add the Geofence and the Meeting
            builder.addGeofence(fence);
            meetings.put(fence.getRequestId(), meeting);

            return true;
        }

        return false;
    }

    /**
     * Listen for changes to the GeofenceListener
     */
    private class GeofenceEventListener implements GeofenceListenerCallbacks
    {

        @Override
        public void onEnter(String fenceId)
        {
            FencedMeeting meeting = FencedMeetingManager.this.meetings.get(fenceId);
            long diffMs = meeting.getMeetingTime().getTimeInMillis() - Calendar.getInstance().getTimeInMillis();

            if (Math.abs(diffMs) <= FencedMeetingManager.TIME_BOUND_MS)
            {
                if (FencedMeetingManager.this.eventCallbacks != null)
                {
                    meeting.setInMeeting(true);
                    FencedMeetingManager.this.eventCallbacks.onEnter(fenceId);
                }
            }
        }

        @Override
        public void onExit(String fenceId)
        {
            FencedMeeting meeting = FencedMeetingManager.this.meetings.get(fenceId);

            if (meeting.isMeeting())
            {
                meeting.setInMeeting(false);
                FencedMeetingManager.this.eventCallbacks.onExit(fenceId);
            }
        }

        @Override
        public void onConnected()
        {
            if (FencedMeetingManager.this.eventCallbacks != null)
            {
                FencedMeetingManager.this.eventCallbacks.onConnected();
            }
        }

        @Override
        public void onDisconnected()
        {
            if (FencedMeetingManager.this.eventCallbacks != null)
            {
                FencedMeetingManager.this.eventCallbacks.onDisconnected();
            }
        }

        @Override
        public void onError()
        {
            if (FencedMeetingManager.this.eventCallbacks != null)
            {
                FencedMeetingManager.this.eventCallbacks.onError();
            }
        }
    }
}
