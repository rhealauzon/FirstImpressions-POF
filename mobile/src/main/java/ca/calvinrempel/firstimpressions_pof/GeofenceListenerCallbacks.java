package ca.calvinrempel.firstimpressions_pof;

/**
 * GeofenceListnerCallbacks can be implemented to provide functionality on geofencing events.
 *
 * Created by Calvin on 2015-03-10.
 */
public interface GeofenceListenerCallbacks
{
    public abstract void onNowHere(FencedMeeting meeting);
    public abstract void onNoLongerHere(FencedMeeting meeting);
    public abstract void onNowNear(FencedMeeting meeting);
    public abstract void onNoLongerNear(FencedMeeting meeting);
}
