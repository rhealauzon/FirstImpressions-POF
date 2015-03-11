package ca.calvinrempel.firstimpressions_pof;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);

        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener()
        {
            @Override
            public void onLayoutInflated(WatchViewStub stub)
            {
                mTextView = (TextView) stub.findViewById(R.id.text);
            }
        });
    }

    /**
     * onClick listener for the date info button
     * @author: Rhea Lauzon
     * @param v - The Calling view
     */
    public void dateInfo(View v)
    {
    }

    /**
     * onClick listener for the contact info button
     * @author: Rhea Lauzon
     * @param v  - The Calling view
     */
    public void contactInfo (View v)
    {
        Intent i = new Intent(this, ContactInfo.class);
        startActivity(i);

        return;
    }

    /**
     * onClick listener for the likes button
     * @author: Rhea Lauzon
     * @param v - The Calling view
     */
    public void likes(View v)
    {
        Intent i = new Intent(this, Likes.class);
        startActivity(i);
    }

}
