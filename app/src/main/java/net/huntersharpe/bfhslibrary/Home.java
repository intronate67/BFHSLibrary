package net.huntersharpe.bfhslibrary;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Home extends Fragment {

    private static String profURL = "";
    private static Bitmap bm = null;
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
        @SuppressLint("RestrictedApi") GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getContext());
        if (account != null) {
            ((TextView)getView().findViewById(R.id.namePlaceholder)).setText(account.getDisplayName());
            ImageView imageView = getView().findViewById(R.id.imageView);
           //TODO: Set image from JSON Response
        }
        db = FirebaseDatabase.getInstance().getReference();
        //TextView ccob = getView().findViewById(R.id.ccobTextView1);
        //ccob.setText(getCcob(account.getId()));
    }

}
