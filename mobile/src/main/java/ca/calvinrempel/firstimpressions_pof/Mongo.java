package ca.calvinrempel.firstimpressions_pof;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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
 * Any Activities or Classes wishing to use this class must implement the MongoAdapter Interface
 * and pass themselves as the first parameter in each of the methods.
 *
 * @author Nav Bhatti
 * @author Lola Priego
 */
public class Mongo {

    private static final String BASE_URL = "https://api.mongolab.com/api/1/databases/";
    private static final String API_KEY = "bup2ZBWGDC-IlRrpRsjTtJqiM_QKSmKa";
    private static final String DB_NAME = "sandbox";


    /**
     * Execute an HTTP GET request as a separate thread using an AsyncTask
     * GET requests are used to retrieve documents
     *
     * @param context MongoAdapter Interface to be used as context for the request
     * @param collection Name of the collection to retrieve from
     * @param query Limit the returned documents to those matching the properties of query
     */
    public static void get( MongoAdapter context, String collection, int id  )
    {
        String url = "";
        try {
            url = BASE_URL
                    + DB_NAME
                    + "/collections/" + collection + "?"
                    + URLEncoder.encode("q={\"_id\":\"" + id + "\"}", "UTF-8")
                    + "&apiKey=" + API_KEY;
        }
        catch( UnsupportedEncodingException e){
            Log.d( "URL", e.getLocalizedMessage() );
        }
        Log.d( "URL", url );
        new GetTask( context ).execute(url);
    }

    public static void get( MongoAdapter context, String collection, Profile p )
    {
        String url = "";
        try {
            url = BASE_URL
                    + DB_NAME
                    + "/collections/" + collection + "?"
                    + URLEncoder.encode("q={\"users\":" + p.getId() + "}", "UTF-8")
                    + "&apiKey=" + API_KEY;
        }
        catch( UnsupportedEncodingException e){
            Log.d( "URL", e.getLocalizedMessage() );
        }
        Log.d( "URL", url );
        new GetTask( context ).execute(url);
    }

    public static void get( MongoAdapter context, String collection )
    {
       String url = BASE_URL
                    + DB_NAME
                    + "/collections/" + collection + "?"
                    + "apiKey=" + API_KEY;
        new GetTask( context ).execute(url);
    }

    /**
     * Execute an HTTP POST request as a separate thread using an AsyncTask
     * POST requests are used to create new documents
     *
     * @param context MongoAdapter Interface to be used as context for the request
     * @param collection Name of the collection to POST to
     * @param document JSONObject to place in the given collection
     */
    public static void post( MongoAdapter context, String collection, JSONObject document )
    {
        String url = BASE_URL
                + DB_NAME
                + "/collections/" + collection
                + "?apiKey=" + API_KEY;

        new PostTask().execute( url, document.toString() );
    }

    /**
     * Execute an HTTP PUT request as a separate thread using an AsyncTask
     * PUT requests are used to update documents
     *
     * @param context MongoAdapter Interface to be used as context for the request
     * @param collection Name of the collection to PUT into
     * @param query Only documents matching the properties in query will be updated
     * @param newValue New key/value pairs to add to document, or new value to update if key exists
     */
    public static void put( MongoAdapter context, String collection, JSONObject query, JSONObject newValue )
    {
        String url = BASE_URL
                + DB_NAME
                + "/collections/" + collection
                + "?apiKey=" + API_KEY;

        String queryStr = "";
        try {
            queryStr = "&q=" + URLEncoder.encode( query.toString(), "UTF-8" );
        }catch (UnsupportedEncodingException e ){
            Log.d("Mongo.Put", e.getLocalizedMessage() );
        }

        url += queryStr;

        String update = "{\"$set\":" + newValue.toString() + "}";

        new PutTask().execute( url, update );
    }

    /**
     * Execute an HTTP DELETE request on a separate thread using an AsyncTask
     * DELETE requests are used to delete documents
     *
     * @param context MongoAdapter Interface to be used as context for the request
     * @param collection Name of the collection to delete from
     * @param id "_id" value of the document to delete
     */
    public static void delete( MongoAdapter context, String collection, String id )
    {
        String url = BASE_URL
                + DB_NAME
                + "/collections/" + collection
                + "/" + id
                + "?apiKey=" + API_KEY;

        new DeleteTask().execute( url );
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
     * be passed to the processResult method of the given context via the onPostExecute() method.
     *
     * The other 3 tasks have empty onPostExecute() methods, but these can be modified to return
     * status strings or other debugging information.
     */
    private static class GetTask
            extends AsyncTask<String, Void, String>   // params, progress, result
    {
        private final MongoAdapter context;

        public GetTask(final MongoAdapter c)
        {
            context = c;
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
                context.processResult(result);
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

    private static class PutTask
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
                final HttpPut httpPut;
                final HttpResponse httpResponse;

                httpclient   = new DefaultHttpClient();
                httpPut      = new HttpPut(params[0]);
                httpPut.setEntity( new StringEntity(params[1]) );
                httpPut.setHeader( "Content-Type", "application/json");
                httpResponse = httpclient.execute(httpPut);
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
            catch ( final Exception e ){ e.printStackTrace(); }

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
