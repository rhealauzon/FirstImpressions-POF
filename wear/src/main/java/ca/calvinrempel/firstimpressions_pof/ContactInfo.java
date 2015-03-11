package ca.calvinrempel.firstimpressions_pof;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;


public class ContactInfo extends Activity {

    // You!
    private Profile user;

    //Date you have coming up
    private Meeting tryst;

    // User you're on a date with
    private Profile other;


    //
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_info);

        // Set the custom fonts
        FontManager.setFont(this, (TextView) findViewById(R.id.date), "cicero.ttf");
        FontManager.setFont(this, (TextView) findViewById(R.id.age), "cicero.ttf");
        FontManager.setFont(this, (TextView) findViewById(R.id.birthday), "cicero.ttf");
        FontManager.setFont(this, (TextView) findViewById(R.id.gender), "cicero.ttf");

        //fetch the current user from the database
        Mongo.getProfile(
                new MongoReceiver() {
                     @Override
                     public void process(JSONArray result) {
                         try {
                             user = new Profile( result.getJSONObject(0) );
                         } catch (JSONException f) {
                             Log.d("GetProfile", f.getLocalizedMessage());
                         }
                         Toast.makeText(getBaseContext(), user.getName(), Toast.LENGTH_LONG).show();
                     }
                 }
                ,1);

        // Get the meeting from the database
        Mongo.getMeetings(
                new MongoReceiver() {
                    @Override
                    public void process(JSONArray result) {
                        try {
                            tryst = new Meeting( result.getJSONObject(0) );
                        } catch (JSONException f) {
                            Log.d("GetMeetings", f.getLocalizedMessage());
                        }
                    }
                }
        , user.getId() );

        // Get the other user from the database
        Mongo.getProfile(
                new MongoReceiver() {
                    @Override
                    public void process(JSONArray result) {
                        try {
                            other = new Profile( result.getJSONObject(0) );
                        }catch (JSONException e){
                            Log.d("GetProfile", e.getLocalizedMessage());
                        }
                    }
                }
        , tryst.getOther(user) );


        TextView name = (TextView) findViewById(R.id.name);
        name.setText(name.getText() + other.getName());

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_contact_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
