package ca.calvinrempel.firstimpressions_pof;

/**
 * Created by Nav on 11/15/2014.
 *
 * Interface must be implemented by any Activities or Classes which wish to use the Mongo.java
 * database driver and should be passed as the first argument whenever calling one of
 * Mongo's static methods.
 */
public interface MongoAdapter {

    /**
     * Method to process the result given by a Mongo.get() call.
     * Other async HTTP requests can be modified to use the method as well.
     *
     * @param result The result string returned by the HTTP request.
     */
    public void processResult(String result);
}
