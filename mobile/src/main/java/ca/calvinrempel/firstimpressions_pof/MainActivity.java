package ca.calvinrempel.firstimpressions_pof;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;


public class MainActivity extends Activity implements MongoAdapter {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    public void getName( View v )
    {
        Mongo.get( this, "users", 1 );
    }

    @Override
    public void processResult(String result) {
        Profile p = null;
        try{
            p = new Profile( new JSONArray(result).getJSONObject(0) );
        }catch( JSONException e ){
            Log.d( "ProcessResult", e.getLocalizedMessage() );
        }
        Toast.makeText( this, p.getName(), Toast.LENGTH_LONG ).show();
    }
}
