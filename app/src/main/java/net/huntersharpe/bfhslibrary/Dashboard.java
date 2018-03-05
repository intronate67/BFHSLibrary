package net.huntersharpe.bfhslibrary;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.concurrent.ExecutionException;

public class Dashboard extends Fragment {
    //TODO: Remove debug logs.
    private EditText barcodeTextbox;
    private TextView sbtValue;
    private TextView sbaValue;
    private TextView ccotValue;
    private static String isbn = "";
    DatabaseReference db;

    public Dashboard() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button signOutButton = getView().findViewById(R.id.signOutButton);
        Button scanButton = getView().findViewById(R.id.scanButton);
        Button checkOutButton = getView().findViewById(R.id.checkOutInitButton);
        signOutButton.setOnClickListener(buttonListener);
        scanButton.setOnClickListener(buttonListener);
        checkOutButton.setOnClickListener(buttonListener);
        barcodeTextbox = getView().findViewById(R.id.barcodeTextbox);
        sbtValue = getView().findViewById(R.id.sbtValue);
        sbaValue = getView().findViewById(R.id.sbaValue);
        ccotValue = getView().findViewById(R.id.ccotValue);
        db = FirebaseDatabase.getInstance().getReference("rvcont");
    }

    public Button.OnClickListener buttonListener = new Button.OnClickListener(){
        @RequiresApi(api = Build.VERSION_CODES.O)
        public void onClick(View v){
            switch(v.getId()){
                case R.id.signOutButton:
                    GoogleSignInClient client = GoogleSignIn.getClient(getContext(),
                            GoogleSignInOptions.DEFAULT_SIGN_IN);
                    client.signOut().addOnCompleteListener(getActivity(),
                            new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            startActivity(new Intent(getActivity(), MainActivity.class));
                        }
                    });
                    break;
                case R.id.scanButton:
                    IntentIntegrator.forSupportFragment(Dashboard.this).setPrompt("Scan Barcode")
                            .initiateScan();
                    break;
                case R.id.checkOutInitButton:
                    //TODO: Handle return values of checkOut();
                    //Run preCheckOut() -> checkOut()
                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode,
                intent);
        Log.i("onActivityResult", "called!");
        Log.i("ScanResult:", scanResult.getContents());
        if(scanResult.getContents() != null){
            Log.i("SCAN:", "Successful!");
            ((TextView)getView().findViewById(R.id.barcodeTextbox))
                    .setText(scanResult.getContents());
            JSONObject jsonObject;
            try {
                jsonObject = loadValues(barcodeTextbox.getText().toString());
                String bookTitle = jsonObject.getJSONArray("items") .getJSONObject(0)
                        .getJSONObject("volumeInfo")
                        .getString("title");
                String author =  jsonObject.getJSONArray("items").getJSONObject(0)
                        .getJSONObject("volumeInfo")
                        .getString("authors");
                sbtValue.setText(bookTitle);
                sbaValue.setText(author);
            } catch (ExecutionException | InterruptedException | JSONException e) {
                e.printStackTrace();
            }
            //TODO: Log user scanned item. DB references not needed until check out.

        }
    }

    private static class AsyncBookRequest extends AsyncTask<String, Object, JSONObject>{

        @Override
        protected JSONObject doInBackground(String... strings){
            if(isCancelled()){
                return null;
            }else{
                //TODO: Add toast messages.
                String apiUrlString = "https://www.googleapis.com/books/v1/volumes?q=isbn:" + isbn;
                Log.i("urlString: ", apiUrlString);
                try{
                    HttpURLConnection connection = null;
                    // Build Connection.
                    URL url = new URL(apiUrlString);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setReadTimeout(5000); // 5 seconds
                    connection.setConnectTimeout(5000); // 5 seconds
                    int responseCode = connection.getResponseCode();
                    if(responseCode != 200){
                        Log.w(getClass().getName(), "Books request failed. Response Code: " +
                                responseCode);
                        connection.disconnect();
                        return null;
                    }
                    StringBuilder builder = new StringBuilder();
                    BufferedReader responseReader = new BufferedReader(new InputStreamReader(
                            connection.getInputStream()));
                    String line = responseReader.readLine();
                    while (line != null){
                        builder.append(line);
                        line = responseReader.readLine();
                    }
                    String responseString = builder.toString();
                    Log.d(getClass().getName(), "Response String: " + responseString);
                    JSONObject responseJson = new JSONObject(responseString);
                    connection.disconnect();
                    return responseJson;
                } catch (IOException | JSONException e) {
                    Log.w(getClass().getName(), "Connection timed out. Returning null");
                    return null;
                }
            }
        }

    }

    private void makeToast(String message){
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private JSONObject loadValues(String barcode) throws ExecutionException, InterruptedException,
            JSONException{
        isbn = barcode;
        AsyncBookRequest bookRequest = new AsyncBookRequest();
        bookRequest.execute();
        return bookRequest.get();
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private Task<Void> checkOut(String barcode) {
        //TODO: NPE Handling for V
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getContext());
        Log.i("Date:", LocalDate.now().toString());
        Log.i("Id:", account.getId());
        return db.child("cob").child(barcode).child(account.getId()).setValue(LocalDate.now().toString());
    }
    private boolean isNumeric(String s){
        try{
            Long.parseLong(s);
            return true;
        }catch(NumberFormatException e){
            return false;
        }
    }
}
