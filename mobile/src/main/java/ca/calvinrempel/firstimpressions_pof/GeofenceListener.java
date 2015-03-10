package ca.calvinrempel.firstimpressions_pof;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.wearable.Wearable;

import java.util.Iterator;
import java.util.List;

/**
 * GeofenceListener listens for Geofencing events.
 *
 * Created by Calvin on 2015-03-10.
 */
public class GeofenceListener extends IntentService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
{
    private GoogleApiClient googleClient;
    private GeofenceListenerCallbacks callbacks;

    private static GeofenceListener instance = null;

    /**
     * Create a new GeofenceListener
     */
    public GeofenceListener()
    {
        super(GeofenceListener.class.getSimpleName());
        callbacks = null;
    }

    /**
     * Get access to the last created GeofenceListener
     *
     * @return the last created GeofenceListener
     */
    public GeofenceListener getInstance()
    {
        return instance;
    }

    /**
     * Set the Callbacks to call on events
     *
     * @param callbacks the GeofenceListenerCallbacks to call
     */
    public void setCallbacks(GeofenceListenerCallbacks callbacks)
    {
        this.callbacks = callbacks;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        googleClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        instance = this;
    }

    /**
     * Handle Incoming Intents
     *
     * @param intent the sent intent
     */
    public void onHandleIntent(Intent intent)
    {
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);

        if (event.hasError())
        {
            triggerError();
        }
        else
        {
            int transitionType = event.getGeofenceTransition();

            switch(transitionType)
            {
                case Geofence.GEOFENCE_TRANSITION_ENTER:
                    triggerEnter(event);
                    break;

                case Geofence.GEOFENCE_TRANSITION_EXIT:
                    triggerExit(event);
                    break;
            }
        }
    }

    @Override
    public void onConnected(Bundle bundle)
    {
        if (callbacks != null)
        {
            callbacks.onConnected();
        }
    }

    @Override
    public void onConnectionSuspended(int i)
    {
        /* Restore the Geofences Here */
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult)
    {
        if (callbacks != null)
        {
            callbacks.onDisconnected();
        }
    }

    /**
     * Trigger the "entered area" callback
     *
     * @param event the Event that triggered the exit
     */
    private void triggerEnter(GeofencingEvent event)
    {
        if (callbacks != null)
        {
            List<Geofence> fences = event.getTriggeringGeofences();
            Iterator<Geofence> itr = fences.iterator();

            while (itr.hasNext())
            {
                callbacks.onEnter(itr.next().getRequestId());
            }
        }
    }

    /**
     * Trigger "exit area" callback
     *
     * @param event the Event that triggered the exit
     */
    private void triggerExit(GeofencingEvent event)
    {
        if (callbacks != null)
        {
            List<Geofence> fences = event.getTriggeringGeofences();
            Iterator<Geofence> itr = fences.iterator();

            while (itr.hasNext())
            {
                callbacks.onExit(itr.next().getRequestId());
            }
        }
    }

    /**
     * Trigger the callback error
     */
    private void triggerError()
    {
        if (callbacks != null)
        {
            callbacks.onError();
        }
    }
}
