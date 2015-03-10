package ca.calvinrempel.firstimpressions_pof;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TreeSet;

/**
 * Created by Nav on 3/10/2015.
 */
public class Profile {
    // List of likes a profile contains
    public static final String[] LIKES = { "movies", "tv", "music", "books", "food" };

    private String name; // Full name of user
    private String gender; // Gender of user 'male' or 'female'
    private Calendar birthDate; // Date of birth in YYYY-MM-DD formate
    private HashMap<String, TreeSet<String>> likes; // List of likes mapped to each type of like
    private URL picture; // URL to a picture of the user

    public Profile( JSONObject obj )
    {
        String[] date;
        JSONArray likeArray;
        TreeSet<String> likeList;
        birthDate = Calendar.getInstance();
        likes = new HashMap<String, TreeSet<String>>();
        try{
            name = obj.getString("name");
            gender = obj.getString("gender");
            date = obj.getString( "birthdate" ).split("-");
            picture = new URL( obj.getString("picture") );
            birthDate.set(
                    Integer.parseInt(date[0]),
                    Integer.parseInt(date[1])-1, // Month is 0 based
                    Integer.parseInt(date[2])
            );
            for (int i = 0; i < LIKES.length; i++) {
                likeArray = obj.getJSONArray(LIKES[i]);
                likeList = new TreeSet<String>();
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

}