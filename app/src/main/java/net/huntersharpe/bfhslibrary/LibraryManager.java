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

/**
 * Created by user on 3/6/2018.
 */

public class LibraryManager {

    private AsyncCall asyncCall;

    private static String isbn;

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
    }*/

    AsyncTask<CallType, Void, Object> loadScanResults(){
        asyncCall.execute(CallType.SCAN_INFO);
        return asyncCall;
    }

    boolean checkOutConditions(){
        return false;
    }

    private static class AsyncCall extends AsyncTask<CallType, Void, Object>{
        @Override
        protected Object doInBackground(CallType... callTypes) {
            switch (callTypes[0]){
                case SCAN_INFO:
                    return scanResults();
                case BOOK_INFO_ALL:
                    break;
                case BOOK_INFO_TAB:
                    break;
            }
            return null;
        }
    }

    void setIsbn(String barcode){
        isbn = barcode;
    }

    private enum CallType{
        BOOK_INFO_ALL,
        BOOK_INFO_TAB,
        SCAN_INFO
    }

    private static JSONObject scanResults() {
        if(isbn != null){
            String apiUrlString = "https://www.googleapis.com/books/v1/volumes?q=isbn:" + isbn;
            Log.i("urlString: ", apiUrlString);
            try {
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
                StringBuilder builder = new StringBuilder();
                BufferedReader responseReader = new BufferedReader(new InputStreamReader(
                        connection.getInputStream()));
                String line = responseReader.readLine();
                while (line != null) {
                    builder.append(line);
                    line = responseReader.readLine();
                }
                String responseString = builder.toString();JSONObject responseJson = new JSONObject(responseString);
                connection.disconnect();
                return responseJson;
            } catch (IOException | JSONException e) {
                return null;
            }
        }else{
            return null;
        }
    }
}
