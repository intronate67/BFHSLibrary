package net.huntersharpe.bfhslibrary;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;

/**
 * Created by user on 3/6/2018.
 */

public class LibraryManager {

    private AsyncCall asyncCall;
    static String content;
    private static LibraryManager instance;

    static LibraryManager getInstance(){
        if(instance == null){
            instance = new LibraryManager();
        }
        return instance;
    }

    private LibraryManager(){
        asyncCall = new AsyncCall();
    }

    AsyncTask<CallType, Void, Object> checkOut(){

        asyncCall.execute(CallType.BOOK_INFO_ALL);
        return asyncCall;
    }

    /*AsyncTask<CallType, Void, Object> reserveBook(){
        asyncCall = new AsyncCall();
        return asyncCall;
        return asyncCall;
    }*/

    AsyncTask<CallType, Void, Object> loadScanResults(){
        asyncCall.execute(CallType.SCAN_INFO);
        return asyncCall;
    }

    boolean checkOutConditions(){
        return false;
    }

    AsyncCall getAsyncInstance(){
        return new AsyncCall();
    }

    static class AsyncCall extends AsyncTask<CallType, Void, Object>{
        @Override
        protected Object doInBackground(CallType... callTypes) {
            switch (callTypes[0]){
                case SCAN_INFO:
                    //TODO: Fix Redundancy
                    return jsonRequest();
                case BOOK_INFO_ALL:
                    break;
                case BOOK_INFO_TAB:
                    break;
                case SEARCH_QUERY:
                    return jsonRequest();
            }
            return null;
        }
    }

    void setIsbn(String barcode){
        content = barcode;
    }

    public enum CallType{
        BOOK_INFO_ALL,
        BOOK_INFO_TAB,
        SCAN_INFO,
        SEARCH_QUERY
    }

    static JSONObject jsonRequest() {
        //TODO: Add "isbn" for dashboard request.
        String apiUrlString;
        JSONObject responseJson = null;
        apiUrlString = "https://www.googleapis.com/books/v1/volumes?q=" + content;
        Log.i("jsonRequest:", "SEARCH_QUERY");
        try {
            Log.i("jsonRequest", apiUrlString);
            Log.i("jsonRequest", "Attempting request!");
            HttpURLConnection connection;
            // Build Connection.
            URL url = new URL(apiUrlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout(5000); // 5 seconds
            connection.setConnectTimeout(5000); // 5 seconds
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                connection.disconnect();
                return null;
            }
            Log.i("jsonRequest", "success!");
            StringBuilder builder = new StringBuilder();
            BufferedReader responseReader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String line = responseReader.readLine();
            while (line != null) {
                builder.append(line);
                line = responseReader.readLine();
            }
            String responseString = builder.toString();
            responseJson = new JSONObject(responseString);
            connection.disconnect();
            return responseJson;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return responseJson;
    }
}
