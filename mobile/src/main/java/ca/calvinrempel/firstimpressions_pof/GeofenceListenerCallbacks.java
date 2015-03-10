package ca.calvinrempel.firstimpressions_pof;

/**
 * Created by Calvin on 2015-03-10.
 */
public interface GeofenceListenerCallbacks
{
    public abstract void onEnter(String fenceId);
    public abstract void onExit(String fenceId);
    public abstract void onConnected();
    public abstract void onDisconnected();
    public abstract void onError();
}
