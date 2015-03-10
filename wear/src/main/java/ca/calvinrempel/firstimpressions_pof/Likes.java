package ca.calvinrempel.firstimpressions_pof;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;


public class Likes extends Activity {

    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_likes);

        //get listView object from XML
        listView = (ListView) findViewById(R.id.likes_list);

        String[] categories = new String[] {"Movies", "Books", "Songs" };

        //define a new adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_item, R.id.textView, categories);

        //assign the adapter to the listView
        listView.setAdapter(adapter);

        //set the on click listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            //On click listener for the items clicked
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                //Item index
                int itemPosition = position;

                //Value of the item clicked
                String  category = (String) listView.getItemAtPosition(position);

                //go to the details page, passing off the category that was clicked
                Intent i = new Intent(Likes.this, LikesDetails.class);
                i.putExtra("category", category);
                startActivity(i);
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_likes, menu);
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
