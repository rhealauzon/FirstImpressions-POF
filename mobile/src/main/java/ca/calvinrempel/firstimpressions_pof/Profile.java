package ca.calvinrempel.firstimpressions_pof;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TreeSet;

/**
 * Created by Nav on 3/10/2015.
 */
public class Profile implements Serializable{
    
    // List of likes a profile contains
    public static final String[] LIKES = { "movies", "tv", "music", "books", "food" };

    private int id;
    private String name; // Full name of user
    private String gender; // Gender of user 'male' or 'female'
    private Calendar birthDate; // Date of birth in YYYY-MM-DD format
    private String phone; // Phone number
    private HashMap<String, TreeSet<String>> likes; // List of likes mapped to each type of like
    private URL picture; // URL to a picture of the user

    public Profile( JSONObject obj )
    {
        String[] date;
        JSONArray likeArray;
        TreeSet<String> likeList;
        birthDate = Calendar.getInstance();
        likes = new HashMap<String, TreeSet<String>>();
        // Get the properties from the JSONObject
        try{
            id = obj.getInt("_id");
            name = obj.getString("name");
            gender = obj.getString("gender");
            phone = obj.getString("phone");
            // Split the yyyy-mm-dd date format
            date = obj.getString( "birthdate" ).split("-");
            picture = new URL( obj.getString("picture") );
            birthDate.set(
                    Integer.parseInt(date[0]),      // Year
                    Integer.parseInt(date[1])-1,    // Month is 0 based
                    Integer.parseInt(date[2])       // Day
            );

            // Populate the likes with values
            for (int i = 0; i < LIKES.length; i++) {
                likeArray = obj.getJSONArray(LIKES[i]);
                likeList = new TreeSet<String>();

                // Iterate through a single type of like
                for (int j = 0; j < likeArray.length(); j++) {
                    likeList.add(likeArray.getString(j));
                }

                likes.put( LIKES[i], likeList );
            }
        }catch ( JSONException e ){
            Log.d( "Profile JSON", e.getLocalizedMessage() );
        }catch ( MalformedURLException e ){
            Log.d( "Profile URL", e.getLocalizedMessage() );
        }
    }

    public String getName(){ return name; }
    public String getGender(){ return gender; }
    public Calendar getBirthDate(){ return birthDate; }
    public HashMap<String,TreeSet<String>> getLikes(){ return likes; }
    public URL getPicture(){ return picture; }
    public int getId(){ return id; }

    public static byte[] serialize(Profile obj) throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        ObjectOutputStream o = new ObjectOutputStream(b);
        o.writeObject(obj);
        return b.toByteArray();
    }

    public static Profile deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream b = new ByteArrayInputStream(bytes);
        ObjectInputStream o = new ObjectInputStream(b);
        return (Profile)o.readObject();
    }
}