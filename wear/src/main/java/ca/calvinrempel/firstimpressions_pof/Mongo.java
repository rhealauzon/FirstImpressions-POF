package ca.calvinrempel.firstimpressions_pof;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

/**
 * Created by Nav Bhatti on 11/11/2014.
 *
 * Code is based on Lola Priego's Android app + MongoDB + MongoLab [cloud] blog post
 * modified using AsyncTasks to comply with Android's enforcement of non-blocking network calls
 * Original post can be found @ http://lolapriego.com/blog/android-app-mongodb-mongolab-cloud/
 *
 * All class methods can be used statically to make HTTP Requests to a MongoLab database
 * hosted in the cloud using the MongoDB REST API documented @ http://docs.mongolab.com/restapi/.
 * Class methods do not have mechanism for all the optional parameters illustrated in the
 * documentation, but can be easily modified to suit the needs of the application.
 *
 * Any Activities or Classes wishing to use this class must implement the MongoReceiver Interface
 * and pass themselves as the first parameter in each of the methods.
 *
 * @author Nav Bhatti
 * @author Lola Priego
 */
public class Mongo {

    // Base mongolab URL
    private static final String BASE_URL = "https://api.mongolab.com/api/1/databases/";

    // API_KEY from the database user's account
    private static final String API_KEY = "bup2ZBWGDC-IlRrpRsjTtJqiM_QKSmKa";

    // Name of the database to access
    private static final String DB_NAME = "sandbox";

    // Name of collection containing user profiles
    private static final String COLL_PROFILES = "users";

    //Name of collection containing meetings between users
    private static final String COLL_MEETINGS = "dates";

    /**
     * Get the document with the given id from the given collection.
     *
     * @param handler handler containing the process() method to handle the result
     * @param id unique id of Profile
     */
    public static void getProfile( MongoReceiver handler, int id  )
    {
        String url = "";
        try {
            // Build the URL
            url = BASE_URL
                    + DB_NAME
                    + "/collections/" + COLL_PROFILES + "?"
                    + URLEncoder.encode("q={\"_id\":\"" + id + "\"}", "UTF-8")
                    + "&apiKey=" + API_KEY;
        }
        catch( UnsupportedEncodingException e){
            Log.d( "URL", e.getLocalizedMessage() );
        }
        Log.d( "URL", url );
        new GetTask( handler ).execute(url);
    }

    /**
     * Get the meetings pertaining to the given profile
     *
     * @param handler handler containing the process() method to handle the result
     * @param id profile id of person whose meetings should be returned
     */
    public static void getMeetings( MongoReceiver handler, int id )
    {
        String url = "";
        try {
            // Build the URL
            url = BASE_URL
                    + DB_NAME
                    + "/collections/" + COLL_MEETINGS + "?"
                    + URLEncoder.encode("q={\"users\":\"{$elemMatch\":{\"id\":" + id + "}}}", "UTF-8")
                    + "&apiKey=" + API_KEY;
        }
        catch( UnsupportedEncodingException e){
            Log.d( "URL", e.getLocalizedMessage() );
        }
        Log.d( "URL", url );
        new GetTask( handler ).execute(url);
    }

    /**
     * Update or create the given meeting in the database
     * @param m The meeting to be updated
     */
    public static void createOrUpdate( Meeting m )
    {
        String url = BASE_URL
                + DB_NAME
                + "/collections/" + COLL_MEETINGS
                + "?apiKey=" + API_KEY;

        new PostTask().execute( url, m.toJSON().toString() );
    }

    /**
     * Execute an HTTP DELETE request on a separate thread using an AsyncTask
     * DELETE requests are used to delete documents
     *
     * @param collection Name of the collection to delete from
     * @param id "_id" value of the document to delete
     */
    public static void delete( String collection, String id )
    {
        // Build the URL
        String url = BASE_URL
                + DB_NAME
                + "/collections/" + collection
                + "/" + id
                + "?apiKey=" + API_KEY;

        new DeleteTask().execute( url );
    }

    /**
     * Get all documents from the given collection
     * @param handler handler containing the process() method to handle the result
     * @param collection collection to search
     */
    public static void get( MongoReceiver handler, String collection )
    {
        // Build the URL
        String url = BASE_URL
                + DB_NAME
                + "/collections/" + collection + "?"
                + "apiKey=" + API_KEY;
        new GetTask( handler ).execute(url);
    }

    /**
     * Execute an HTTP POST request as a separate thread using an AsyncTask
     * POST requests are used to create and update documents
     *
     * @param collection Name of the collection to POST to
     * @param document JSONObject to place in the given collection
     */
    public static void post(  String collection, JSONObject document )
    {
        // Build the URL
        String url = BASE_URL
                + DB_NAME
                + "/collections/" + collection
                + "?apiKey=" + API_KEY;

        new PostTask().execute( url, document.toString() );
    }

    /**
     * Converts the passed InputStream into a String
     *
     * @param is InputStream to convert
     * @return Converted InputStream as a String
     * @throws java.io.IOException
     */
    private static String convertStreamToString(final InputStream is)
            throws IOException {
        InputStreamReader isr;
        BufferedReader reader;
        final StringBuilder builder;
        String line;

        isr = new InputStreamReader(is);
        reader = new BufferedReader(isr);
        builder = new StringBuilder();

        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }

        return builder.toString();
    }

    /**
     * A new instance of the classes below will be created and executed Asynchronously
     * for the corresponding HTTP request verbs. In the case of a GetTask, a result String will
     * be passed to the process method of the given handler via the onPostExecute() method.
     *
     * The other 3 tasks have empty onPostExecute() methods, but these can be modified to return
     * status strings or other debugging information.
     */
    private static class GetTask
            extends AsyncTask<String, Void, String>   // params, progress, result
    {
        private final MongoReceiver handler;

        public GetTask(final MongoReceiver c)
        {
            handler = c;
        }

        @Override
        protected String doInBackground(final String... params)
        {
            InputStream inputStream;
            String      result;

            if(params.length != 1)
            {
                throw new IllegalArgumentException("You must provide one uri only");
            }

            inputStream = null;

            try
            {
                final HttpClient httpclient;
                final HttpGet httpGet;
                final HttpResponse httpResponse;

                httpclient   = new DefaultHttpClient();
                httpGet      = new HttpGet( new URI(params[0]) );
                httpResponse = httpclient.execute(httpGet);
                inputStream  = httpResponse.getEntity().getContent();

                if(inputStream != null)
                {
                    result = convertStreamToString(inputStream);
                }
                else
                {
                    result = null;
                }

                return (result);
            }
            catch(final ClientProtocolException ex)
            {
                Log.d("InputStream", ex.getLocalizedMessage());
            }
            catch(final IOException ex)
            {
                Log.d("InputStream", ex.getLocalizedMessage());
            }
            catch ( URISyntaxException ex )
            {
                Log.d("InputStream", ex.getLocalizedMessage());
            }
            return (null);
        }

        @Override
        protected void onPostExecute(final String result)
        {
            if (result != null)
            {
                try {
                    handler.process( new JSONArray( result ) );
                }catch ( JSONException e ){
                    Log.d( "GetTask", e.getLocalizedMessage() );
                }
            }
        }

    }

    private static class PostTask
            extends AsyncTask<String, Void, String>   // params, progress, result
    {

        @Override
        protected String doInBackground(final String... params)
        {
            InputStream inputStream;
            String      result;

            if(params.length != 2)
            {
                throw new IllegalArgumentException("You must provide 2 arguments");
            }

            inputStream = null;

            try
            {
                final HttpClient httpclient;
                final HttpPost httpPost;
                final HttpResponse httpResponse;

                httpclient   = new DefaultHttpClient();
                httpPost      = new HttpPost(params[0]);
                httpPost.setEntity( new StringEntity(params[1]) );
                httpPost.setHeader( "Content-Type", "application/json");
                httpResponse = httpclient.execute(httpPost);
                inputStream  = httpResponse.getEntity().getContent();

                if(inputStream != null)
                {
                    result = convertStreamToString(inputStream);
                }
                else
                {
                    result = null;
                }

                return (result);
            }
            catch(final ClientProtocolException ex)
            {
                Log.d("InputStream", ex.getLocalizedMessage());
            }
            catch(final IOException ex)
            {
                Log.d("InputStream", ex.getLocalizedMessage());
            }

            return (null);
        }

        @Override
        protected void onPostExecute(final String result){}

    }

    private static class DeleteTask
            extends AsyncTask<String, Void, String>   // params, progress, result
    {
        @Override
        protected String doInBackground(final String... params)
        {
            InputStream inputStream;
            String      result;

            if(params.length != 1)
            {
                throw new IllegalArgumentException("You must provide 1 parameter only");
            }

            inputStream = null;

            try
            {
                final HttpClient httpclient;
                final HttpDelete httpDelete;
                final HttpResponse httpResponse;

                httpclient   = new DefaultHttpClient();
                httpDelete      = new HttpDelete(params[0]);
                httpResponse = httpclient.execute(httpDelete);
                inputStream  = httpResponse.getEntity().getContent();

                if(inputStream != null)
                {
                    result = convertStreamToString(inputStream);
                }
                else
                {
                    result = null;
                }

                return (result);
            }
            catch(final ClientProtocolException ex)
            {
                Log.d("InputStream", ex.getLocalizedMessage());
            }
            catch(final IOException ex)
            {
                Log.d("InputStream", ex.getLocalizedMessage());
            }

            return (null);
        }

        @Override
        protected void onPostExecute(final String result){}
    }
}
