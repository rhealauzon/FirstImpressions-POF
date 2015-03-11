package ca.calvinrempel.firstimpressions_pof;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class LikesDetails extends Activity
{
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_likes_details);

        //get listView object from XML
        listView = (ListView) findViewById(R.id.likes_details);

        //get the category from the bundle and set the text to it
        TextView title = (TextView) findViewById(R.id.likeCategory);

        Intent i = getIntent();
        title.setText(i.getStringExtra("category"));
        FontManager.setFont(this, title, "biko.otf");


        //TODO:
        //Populate this from the JSON
        String[] categories = new String[] {"Movies", "Books", "Songs" };

        //define a new adapter for the list
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_item_details, R.id.textView, categories);

        //assign the adapter to the listView
        listView.setAdapter(adapter);
   }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_likes_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
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
