package net.huntersharpe.bfhslibrary;

import android.content.Intent;
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
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class Dashboard extends Fragment {
    //TODO: Remove debug logs.
    private EditText barcodeTextbox;
    DatabaseReference db;
    LibraryManager libManager;

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
        Button signOutButton = getView().findViewById(R.id.signOutButton);
        Button scanButton = getView().findViewById(R.id.scanButton);
        Button checkOutButton = getView().findViewById(R.id.checkOutInitButton);
        signOutButton.setOnClickListener(buttonListener);
        scanButton.setOnClickListener(buttonListener);
        checkOutButton.setOnClickListener(buttonListener);
        barcodeTextbox = getView().findViewById(R.id.barcodeTextbox);
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
                    if(libManager.checkOutConditions()){
                        libManager.setIsbn(barcodeTextbox.getText().toString());
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
        Log.i("onActivityResult", "called!");
        Log.i("ScanResult:", scanResult.getContents());
        if(scanResult.getContents() != null){
            Log.i("SCAN:", "Successful!");
            ((TextView)getView().findViewById(R.id.barcodeTextbox))
                    .setText(scanResult.getContents());
            JSONObject results;
            try {
                 results = (JSONObject) libManager.loadScanResults().get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            //TODO: Load JSON Results into sbt and sba labels.
        }
    }

    private void makeToast(String message){
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

}
