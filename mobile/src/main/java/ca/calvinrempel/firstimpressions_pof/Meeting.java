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

    private String id;
    private Calendar time;
    private LatLng place;
    private User user1;
    private User user2;

    public Meeting( JSONObject obj )
    {
        time = Calendar.getInstance();
        String[] dateTime;
        JSONArray loc;
        JSONArray users;
        try {
            id = obj.getJSONObject("_id").getString("$oid");
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
            user1 = new User( users.getJSONObject(0) );
            user2 = new User( users.getJSONObject(1) );
        }catch ( JSONException e ){
            Log.d( "Meeting Constructor", e.getLocalizedMessage() );
        }
    }

    public void setNearby( int id, boolean near )
    {
        if( user1.id == id )
            user1.nearby = near;
        else if( user2.id == id )
            user2.nearby = near;
        else
            throw new IllegalArgumentException( "User id is not going on this date" );
    }

    public void setArrived( int id, boolean arrive )
    {
        if( user1.id == id )
            user1.arrived = arrive;
        else if( user2.id == id )
            user2.arrived = arrive;
        else
            throw new IllegalArgumentException( "User id is not going on this date" );
    }

    public JSONObject toJSON()
    {
        JSONObject obj = new JSONObject();
        JSONObject temp;
        JSONArray tempArr;
        String date;
        try{
            // put the _id
            temp = new JSONObject();
            temp.put( "$oid", id );
            obj.put("_id", temp );

            // put the place
            tempArr = new JSONArray();
            tempArr.put( place.latitude );
            tempArr.put( place.longitude );
            obj.put( "location", tempArr );

            // put the date/time
            date = time.get( Calendar.YEAR ) + "-"
                    + time.get( Calendar.MONTH ) + "-"
                    + (time.get( Calendar.DAY_OF_MONTH ) + 1) + "-"
                    + time.get( Calendar.HOUR_OF_DAY ) + "-"
                    + time.get( Calendar.MINUTE );
            obj.put( "time", date );

            // put the users
            tempArr = new JSONArray();
            tempArr.put( user1.toJSON() );
            tempArr.put( user2.toJSON() );
        }catch (JSONException e){
            Log.d( "Meeting.toJSON", e.getLocalizedMessage() );
        }
        return obj;
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

    private class User
    {
        public int id;
        public boolean arrived;
        public boolean nearby;

        public User( JSONObject obj )
        {
            try {
                id = obj.getInt( "id" );
                arrived = obj.getBoolean("arrived");
                nearby = obj.getBoolean("nearby");
            }catch (JSONException e){
                Log.d( "User", e.getLocalizedMessage() );
            }
        }

        public JSONObject toJSON()
        {
            JSONObject obj = new JSONObject();
            try {
                obj.put("id", id);
                obj.put("nearby", nearby);
                obj.put("arrived", arrived);
            }catch (JSONException e){
                Log.d( "User.toJSON", e.getLocalizedMessage() );
            }
            return obj;
        }
    }
}
