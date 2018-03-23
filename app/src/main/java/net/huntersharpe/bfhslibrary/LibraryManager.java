package net.huntersharpe.bfhslibrary;

import android.widget.Button;

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

    private static LibraryManager instance;
    private String request = "";
    private String requestUrl = "https://www.googleapis.com/books/v1/volumes?q=" + request;
    private JSONObject jsonResponse = null;

    static LibraryManager getInstance(){
        if(instance == null){
            instance = new LibraryManager();
        }
        return instance;

    }

    JSONObject getJsonResponseFor( String requestString){
        request = requestString;
        try{
            HttpURLConnection connection;
            URL url = new URL(requestUrl);
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout(5000);
            connection.setConnectTimeout(5000);
            int responseCode = connection.getResponseCode();
            if(responseCode != 200){
                connection.disconnect();
            }
            StringBuilder builder = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String line = br.readLine();
            while(line != null){
                builder.append(line);
                line = br.readLine();
            }
            String responseString = builder.toString();
            jsonResponse = new JSONObject(responseString);
            connection.disconnect();
        }catch(IOException | JSONException e){
            e.printStackTrace();
        }
        return jsonResponse;
    }


    boolean checkOutConditions(){
        return false;
    }

}
