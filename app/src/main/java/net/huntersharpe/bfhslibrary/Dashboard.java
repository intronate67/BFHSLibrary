package net.huntersharpe.bfhslibrary;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class Dashboard extends Fragment {
    //TODO: Remove debug logs.
    private EditText barcodeTextbox;
    private LibraryManager libManager;

    public Dashboard() {
        // Required empty public constructor
        libManager = LibraryManager.getInstance();
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
        Button scanButton = getView().findViewById(R.id.scanButton);
        Button checkOutButton = getView().findViewById(R.id.checkOutInitButton);
        scanButton.setOnClickListener(buttonListener);
        checkOutButton.setOnClickListener(buttonListener);
        barcodeTextbox = getView().findViewById(R.id.barcodeTextbox);
    }

    public Button.OnClickListener buttonListener = new Button.OnClickListener(){
        @SuppressLint("RestrictedApi")
        @RequiresApi(api = Build.VERSION_CODES.O)
        public void onClick(View v){
            switch(v.getId()){
                case R.id.scanButton:
                    IntentIntegrator.forFragment(Dashboard.this).setPrompt("Scan Barcode")
                            .initiateScan();
                    break;
                case R.id.checkOutInitButton:
                    if(libManager.checkOutConditions()){
                        //TODO: Check out!
                    }
                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode,
                intent);
        if(scanResult.getContents() != null){
            Log.i("SCAN:", "Successful!");
            ((TextView)getNonNullView().findViewById(R.id.barcodeTextbox))
                    .setText(scanResult.getContents());
            //TODO: Remove comments
            /*String[] results = libManager.runTask(LibraryManager.RequestType.BOOK_ALL);
            if(results[0].equals("null")){
                //Book Not Found
                Toast.makeText(getContext(), "Test Failure!", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getContext(), "Test Success!", Toast.LENGTH_SHORT).show();
                //Set book title, author, cover picture
            }*/
            /*JSONObject results = null;
            String bookTitle = "";
            String bookAuthor = "";
            try {
                 results = (JSONObject) libManager.loadScanResults().get();
                 bookTitle = results.getJSONArray("items")
                         .getJSONObject(0).getJSONObject("volumeInfo")
                         .getString("title");
                 bookAuthor = results.getJSONArray("items").getJSONObject(0)
                         .getJSONObject("volumeInfo").getString("authors");
            } catch (InterruptedException | ExecutionException | JSONException e) {
                e.printStackTrace();
            }
            if(getView() != null && results != null){
                ((TextView)getNonNullView().findViewById(R.id.sbtValue)).setText(bookTitle);
                ((TextView)getNonNullView().findViewById(R.id.sbaValue)).setText(bookAuthor);
            }else{
                makeToast("Book not Found!");
            }*/
        }else{
            Log.i("TEST", "Else fired!");
            //TODO: Remove - Temp for debug
            JSONObject jsonObject = libManager.getJsonResponseFor("0545162076");
            String title = "";
            try{
                title = jsonObject.getJSONArray("items").getJSONObject(0)
                        .getJSONObject("volumeInfo").getString("title");
            }catch(JSONException e){
                e.printStackTrace();
            }
            Log.i("Results[0]", title);
            /*if(results[0].equals("null") || results[0].equals("error")){
                //Book Not Found
                Toast.makeText(getContext(), "Test Failure!", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getContext(), "Test Success!", Toast.LENGTH_SHORT).show();
                //Set book title, author, cover picture
            }*/
        }
    }

    private View getNonNullView(){
        if(getView() != null){
            return getView();
        }else{
            return null;
        }
    }

    private void makeToast(String message){
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

}
