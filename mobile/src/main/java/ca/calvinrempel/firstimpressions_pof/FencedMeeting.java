package ca.calvinrempel.firstimpressions_pof;

import android.location.Location;

import java.util.Calendar;

/**
 * FencedMeeting holds information about a meeting that takes place at a time and a location.
 *
 * Created by Calvin on 2015-03-10.
 */
public class FencedMeeting
{
    private Calendar meetingTime;
    private Location location;
    private boolean inMeeting;
    private int otherUserId;

    /**
     * Create a new FencedMeeting
     *
     * @param meetingTime the Date and time at which the meeting takes place
     * @param location the location of the meeting
     * @param otherUserId the ID of the user being met with
     */
    public FencedMeeting(Calendar meetingTime, Location location, int otherUserId)
    {
        this.meetingTime = meetingTime;
        this.location = location;
        this.otherUserId = otherUserId;
    }

    /**
     * Set whether or not the user is currently in the meeting.
     *
     * @param inMeeting whether the user is currently in the meeting or not
     */
    public void setInMeeting(boolean inMeeting)
    {
        this.inMeeting = inMeeting;
    }

    /**
     * Check if the user is currently in the meeting or not.
     *
     * @return true if the user is in the meeting, false if the user is not in the meeting.
     */
    public boolean isMeeting()
    {
        return this.inMeeting;
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
    public Location getLocation()
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
