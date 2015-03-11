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
    private Profile user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_info);

        //fetch the user from the database
        Mongo.getProfile(
                new MongoReceiver() {
                     @Override
                     public void process(JSONArray result) {
                         try {
                             user = new Profile( result.getJSONObject(0) );
                         } catch (JSONException f) {
                             Log.d("ProcessResult", f.getLocalizedMessage());
                         }
                         Toast.makeText(getBaseContext(), user.getName(), Toast.LENGTH_LONG).show();
                     }
                 }
                ,1);

        TextView name = (TextView) findViewById(R.id.name);
        name.setText(name.getText() + user.getName());

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
