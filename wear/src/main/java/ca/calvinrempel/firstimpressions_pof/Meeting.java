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
            // Split YYYY-MM-DD-HH-mm into parts
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

    public void isNearby( Profile p, boolean near )
    {
        if( user1.id == p.getId() )
            user1.nearby = near;
        else if( user2.id == p.getId() )
            user2.nearby = near;
    }


    public boolean isNearby( Profile p  )
    {
        if( user1.id == p.getId() )
            return user1.nearby;
        else if( user2.id == p.getId() )
            return user2.nearby;
        else // We shouldn't get here
            return false;
    }

    public void hasArrived( Profile p, boolean arrive )
    {
        if( user1.id == p.getId() )
            user1.arrived = arrive;
        else if( user2.id == p.getId() )
            user2.arrived = arrive;
    }

    public boolean hasArrived( Profile p )
    {
        if( user1.id == p.getId() )
            return user1.arrived;
        else if( user2.id == p.getId() )
            return user2.arrived;
        else // We shouldn't get here
            return false;

    }

    public int getOther( Profile p )
    {
        if( user1.id == p.getId() )
            return user2.id;
        else if( user2.id == p.getId() )
            return user1.id;
        else // We shouldn't get here
            return 0;
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
                    + (time.get( Calendar.MONTH )+1) + "-"
                    + time.get( Calendar.DAY_OF_MONTH ) + "-"
                    + time.get( Calendar.HOUR_OF_DAY ) + "-"
                    + time.get( Calendar.MINUTE );
            obj.put( "time", date );

            // put the users
            tempArr = new JSONArray();
            tempArr.put( user1.toJSON() );
            tempArr.put( user2.toJSON() );
            obj.put( "users", tempArr );

        }catch (JSONException e){
            Log.d( "Meeting.toJSON", e.getLocalizedMessage() );
        }
        return obj;
    }

    public String getId(){ return id; }
    public Calendar getTime(){ return time; }
    public LatLng getPlace(){ return place; }
    public User[] getUsers()
    {
        User[] result = new User[2];
        result[0] = user1;
        result[1] = user2;
        return result;
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
