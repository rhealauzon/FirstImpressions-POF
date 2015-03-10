package ca.calvinrempel.firstimpressions_pof;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Nav on 3/10/2015.
 */
public class Meeting {
    private Calendar time;
    private LatLng place;
    private int user1;
    private int user2;

    public Meeting( JSONObject obj )
    {
        time = Calendar.getInstance();
        String[] dateTime;
        JSONArray loc;
        JSONArray users;
        try {
            loc = obj.getJSONArray("location");
            place = new LatLng( loc.getDouble(0), loc.getDouble(1));
            dateTime = obj.getString( "time" ).split( "-" );
            time.set(
                    Integer.parseInt(dateTime[0]),    // Year
                    Integer.parseInt(dateTime[1])-1,  // Month is 0 based
                    Integer.parseInt(dateTime[2]),    // Day
                    Integer.parseInt(dateTime[3]),    // Hour
                    Integer.parseInt(dateTime[4])     // Minute
            );
            users = obj.getJSONArray( "users" );
            user1 = users.getInt(0);
            user2 = users.getInt(1);
        }catch ( JSONException e ){
            Log.d( "Meeting Constructor", e.getLocalizedMessage() );
        }
    }

    public static List<Meeting> getMeetings ( JSONArray arr )
    {
        ArrayList<Meeting> results = new ArrayList<Meeting>();
        try {
            for (int i = 0; i < arr.length(); i++) {
                results.add(new Meeting(arr.getJSONObject(i)));
            }
        }catch (JSONException e){
            Log.d( "getMeetings", e.getLocalizedMessage() );
        }
        return results;
    }
}
