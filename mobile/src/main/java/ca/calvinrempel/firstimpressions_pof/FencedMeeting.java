package ca.calvinrempel.firstimpressions_pof;

import com.google.android.gms.maps.model.LatLng;

import java.util.Calendar;

/**
 * FencedMeeting holds information about a meeting that takes place at a time and a location.
 *
 * Created by Calvin on 2015-03-10.
 */
public class FencedMeeting
{
    private Calendar meetingTime;
    private LatLng location;
    private boolean near, here;
    private int otherUserId;

    /**
     * Create a new FencedMeeting
     *
     * @param meetingTime the Date and time at which the meeting takes place
     * @param location the location of the meeting
     * @param otherUserId the ID of the user being met with
     */
    public FencedMeeting(Calendar meetingTime, LatLng location, int otherUserId)
    {
        this.meetingTime = meetingTime;
        this.location = location;
        this.otherUserId = otherUserId;
        near = false;
        here = false;
    }

    /**
     * Set whether the user is at the meeting or not
     *
     * @param here true to indicate that the user has arrived, false to indicate that the user as left
     */
    public void setIsHere(boolean here)
    {
        this.here = here;
    }

    /**
     * Set whether the user is near the meeting or not
     *
     * @param near true to indicate that the user is near the meeting, false to indicate that they have
     *             left the vicinity.
     */
    public void setIsNear(boolean near)
    {
        this.near = near;
    }

    /**
     * Check if the user is at the meeting or not
     *
     * @return true if the user is here, false if not
     */
    public boolean isHere()
    {
        return here;
    }

    /**
     * Check if the user near the meeting
     *
     * @return true if the user is near the meeting, false if not
     */
    public boolean isNear()
    {
        return near;
    }

    /**
     * Get the ID of the user being met with.
     *
     * @return the ID of the user being met with.
     */
    public int getOtherUserId()
    {
        return this.otherUserId;
    }

    /**
     * Get the Location of the Meeting
     *
     * @return the location of the meeting
     */
    public LatLng getLocation()
    {
        return this.location;
    }

    /**
     * Get the time of the meeting
     *
     * @return the time of the meeting including date
     */
    public Calendar getMeetingTime()
    {
        return this.meetingTime;
    }
}
