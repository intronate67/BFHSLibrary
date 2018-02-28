package net.huntersharpe.bfhslibrary;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class Home extends Fragment {

    private static String profURL = "";
    private static Bitmap bm = null;
    private TextView ccob1;
    private DatabaseReference db;

    public Home() {
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
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getContext());
        if (account != null) {
            ((TextView)getView().findViewById(R.id.namePlaceholder)).setText(account.getDisplayName());
            ImageView imageView = getView().findViewById(R.id.imageView);
            if(account.getPhotoUrl() != null){
                profURL = account.getPhotoUrl().toString();
            }
            AsyncBitmapRequest task = new AsyncBitmapRequest();
            task.execute();
            imageView.setImageBitmap(bm);
        }
        db = FirebaseDatabase.getInstance().getReference();
        ccob1 = getView().findViewById(R.id.ccobTextView1);
        ccob1.setText(getCcob(account.getId()));
    }

    private String getCcob(String uid){
        return "";
    }

    private static class AsyncBitmapRequest extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... voids) {
            bm = getImageBitmap(profURL);
            return null;
        }
    }
    private static Bitmap getImageBitmap(String url){
        Bitmap bm = null;
        try {
            URL aURL = new URL(url);
            URLConnection conn = aURL.openConnection();
            conn.connect();
            InputStream is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            bm = BitmapFactory.decodeStream(bis);
            bis.close();
            is.close();
        } catch (IOException e) {
            Log.e("Test", "Error getting bitmap", e);
        }
        return bm;
    }

}
