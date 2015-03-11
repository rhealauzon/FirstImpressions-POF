package ca.calvinrempel.firstimpressions_pof;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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
    private static final float NEAR_FENCE_RADIUS_METERS = 500;
    private static final float HERE_FENCE_RADIUS_METERS = 100;
    public static final int TIME_BOUND_MS = 15 * 60 * 1000;    // 15 Minutes
    private final int GPS_INTERVAL_TIME_MS = 5000;
    private final int GPS_DISTANCE_DELTA_M = 10;

    private static int idCounter = 0;
    private List<FencedMeeting> meetings;
    private GeofenceListenerCallbacks eventCallbacks;
    private LocationListener locListener;
    private LocationManager locManager;

    /**
     * Create a new FencedMeetingManager that allows FencedMeetings to be tracked
     * and events to be triggered when a user enters or exits them.
     *
     * @param callbacks the callbacks used to process Geofencing events.
     */
    public FencedMeetingManager(GeofenceListenerCallbacks callbacks)
    {
        meetings = new ArrayList<FencedMeeting>();
        eventCallbacks = callbacks;
    }

    /**
     * Start Geofencing service
     *
     * @param context the app context
     * @return true if started, false if gps not available
     */
    public boolean startGeofencing(Context context)
    {
        // Initialize everything if they haven't already been initialized.
        if ( locManager == null)
        {
            locManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            // Return false if location services not available
            if (!locManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            {
                return false;
            }

            locListener = new GPSListener();


            // If a position is already available, check if they are in a region
            Location loc = getLastKnownLocationAll();
            if( loc != null )
            {
                UpdateCurrentLocation( loc.getLatitude(), loc.getLongitude() );
            }

            // Request Continuous Updates
            updateLocation();
        }

        return true;
    }

    /**
     * Create a new Meeting to be added to the Geofencing tracker
     *
     * @param userID the ID of the other user in the meeting
     * @param location the location of the meeting
     * @param meetingTime the time of the meeting
     * @return true if the meeting was added, false if the meeting could not be added
     */
    public boolean createMeeting(int userID, LatLng location, Calendar meetingTime)
    {
        long timeToMeeting = meetingTime.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();

        // Only add the Meeting if after current date
        if( timeToMeeting > 0 )
        {
            meetings.add(new FencedMeeting(meetingTime, location, userID));
            return true;
        }

        return false;
    }

    /**
     * Check if a user has just arrived in the vicinity of the meeting.
     *
     * @param lat the users latitude
     * @param lon the users longitude
     * @param meeting the meeting object
     * @return the distance to the meeting, or -1 if not just arrived
     */
    private float isUserNear(double lat, double lon, FencedMeeting meeting)
    {
        float results[] = new float[1];
        Location.distanceBetween(lat, lon, meeting.getLocation().latitude, meeting.getLocation().longitude, results);

        // Ensure the user is in the distance range
        if (results[0] < FencedMeetingManager.NEAR_FENCE_RADIUS_METERS)
        {
            long diffMs = meeting.getMeetingTime().getTimeInMillis() - Calendar.getInstance().getTimeInMillis();

            // If the User arrived within 'n' minutes of the meeting, consider them as entered
            if (Math.abs(diffMs) <= FencedMeetingManager.TIME_BOUND_MS)
            {
                return results[0];
            }
        }

        return -1;
    }

    /**
     * Request new Location data from the GPS service on a continuous basis.
     */
    private void updateLocation()
    {
        // Prefer to get location from the network provider
        /** COMMENTED OUT BECAUSE GENYMOTION DOESN"T LET YOU CHANGE NETWORK PROVIDER LOCATION ??? */
        /*if ( locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) )
        {
            locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    GPS_INTERVAL_TIME_MS,
                    GPS_DISTANCE_DELTA_M,
                    locListener);
        }
        // Fallback on GPS
        else*/ if ( locManager.isProviderEnabled(LocationManager.GPS_PROVIDER ) )
        {
            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    GPS_INTERVAL_TIME_MS,
                    GPS_DISTANCE_DELTA_M,
                    locListener);
        }
    }

    /**
     * Get the last location from any provider
     *
     * @return the last known location (null if not available)
     */
    private Location getLastKnownLocationAll()
    {
        Location loc = null;

        // Prefer to get Network Location
        if ( locManager.getLastKnownLocation( LocationManager.NETWORK_PROVIDER) != null)
        {
            loc = locManager.getLastKnownLocation( LocationManager.NETWORK_PROVIDER );
        }
        // Fallback to GPS if necessary
        else if( locManager.getLastKnownLocation( LocationManager.GPS_PROVIDER ) != null)
        {
            loc = locManager.getLastKnownLocation( LocationManager.GPS_PROVIDER);
        }
        // Use location received by other apps as last resort
        else if( locManager.getLastKnownLocation( LocationManager.PASSIVE_PROVIDER ) != null)
        {
            loc = locManager.getLastKnownLocation( LocationManager.PASSIVE_PROVIDER );
        }

        return loc;
    }

    /**
     * Check if the user entered or exited a region after moving
     *
     * @param lat the new latitude
     * @param lon the new longitude
     */
    private void UpdateCurrentLocation( double lat, double lon )
    {
        FencedMeeting meeting;
        float results[] = new float[1];

        for (int i = 0; i < meetings.size(); i++)
        {
            meeting = meetings.get(i);
            float distance;

            // Check if the user has entered the meeting (check if distance >= 0 as -1 indicates non-distance related conflict)
            if ((distance = isUserNear(lat, lon, meeting)) < FencedMeetingManager.NEAR_FENCE_RADIUS_METERS && distance >= 0)
            {
                // If the user was not previously near, mark as near
                if (!meeting.isNear())
                {
                    meeting.setIsNear(true);
                    eventCallbacks.onNowNear(meeting);
                }

                // If the user was not previously here, and they are close enough, mark as here
                if (distance < FencedMeetingManager.HERE_FENCE_RADIUS_METERS)
                {
                    if (!meeting.isHere())
                    {
                        meeting.setIsHere(true);
                        eventCallbacks.onNowHere(meeting);
                    }
                }
                // If the user left the meeting, mark as no longer here
                else if (meeting.isHere())
                {
                    meeting.setIsHere(false);
                    eventCallbacks.onNoLongerHere(meeting);
                }
            }
            // If they moved away from the meeting, mark them as left
            else if (meeting.isNear())
            {
                meeting.setIsHere(false);
                meeting.setIsNear(false);
                eventCallbacks.onNoLongerNear(meeting);
            }

        }
    }

    /**
     * A LocationListener responds to actions fired by the GPS service.
     */
    private class GPSListener implements LocationListener {
        /**
         * When a new location is requested, update update the onscreen textview.
         *
         * @param l
         */
        public void onLocationChanged(Location l)
        {
            UpdateCurrentLocation( l.getLatitude(), l.getLongitude() );
        }

        /* Methods that should be implemented but I don't have time for! */
        public void onStatusChanged(String provider, int status, Bundle extras) {}
        public void onProviderEnabled(String s) {}
        public void onProviderDisabled(String s) {}
    }
}
