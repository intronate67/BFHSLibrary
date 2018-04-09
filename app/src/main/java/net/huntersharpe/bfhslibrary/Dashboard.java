package net.huntersharpe.bfhslibrary;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;


public class Dashboard extends Fragment {
    //TODO: Remove debug logs.
    private TextView titleLabel;
    private TextView authorLabel;
    private EditText barcodeTextbox;
    private ImageView bookCover;

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
        Button scanButton = getView().findViewById(R.id.scanButton);
        Button checkOutButton = getView().findViewById(R.id.checkOutInitButton);
        titleLabel = getView().findViewById(R.id.sbtValue);
        authorLabel = getView().findViewById(R.id.sbaValue);
        bookCover = getView().findViewById(R.id.dashBookCoverView);
        scanButton.setOnClickListener(buttonListener);
        checkOutButton.setOnClickListener(buttonListener);
        barcodeTextbox = getView().findViewById(R.id.barcodeTextbox);
    }

    //TODO: Add db check out and reservation limits
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
                    //TODO: Check out
                    new DatabaseManager();
                    DatabaseManager dbManager = new DatabaseManager();
                    dbManager.setUid(getContext());
                    dbManager.checkoutBook(barcodeTextbox.getText().toString());
                    if(dbManager.getResult()){
                        Toast.makeText(getContext(), "Check out Successful!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getContext(), HomeScreenActivity.class);
                        startActivity(intent);
                    }else{
                        Toast.makeText(getContext(), "Check out Failed!", Toast.LENGTH_SHORT).show();
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
            ((TextView)getNonNullView().findViewById(R.id.barcodeTextbox)).setText(scanResult
                    .getContents());
            makeJsonRequest(scanResult.getContents());
        }else{
            Log.i("TEST", "Else fired!");
            Toast.makeText(getContext(), "No book scanned!", Toast.LENGTH_SHORT).show();
        }
    }

    private View getNonNullView(){
        if(getView() != null){
            return getView();
        }else{
            return null;
        }
    }

    private void makeJsonRequest(String request){
        String url = "https://www.googleapis.com/books/v1/volumes?q=" + request;
        RequestQueue queue = Volley.newRequestQueue(getContext());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            final JSONObject bookObject = response.getJSONArray("items")
                                    .getJSONObject(0).getJSONObject("volumeInfo");
                            Log.i("JSON", "Try/Catch");
                            Log.i("Total Items", String.valueOf(response.getInt("totalItems")));
                            titleLabel.setText(bookObject.getString("title"));
                            authorLabel.setText(bookObject.getString("authors"));
                            try{
                                setBookCover(bookObject);
                            }catch (JSONException e){
                                e.printStackTrace();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Set to null or whatever
            }
        });
        queue.add(jsonObjectRequest);
    }

    private void setBookCover(JSONObject bookObject) throws JSONException {
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        ImageRequest imageRequest = new ImageRequest(
                bookObject.getJSONObject("imageLinks").getString("smallThumbnail"),
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        bookCover.setImageBitmap(response);
                    }
                },
                0,
                0,
                ImageView.ScaleType.CENTER_CROP,
                Bitmap.Config.RGB_565,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });
        requestQueue.add(imageRequest);
    }
}
