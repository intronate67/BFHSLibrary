package net.huntersharpe.bfhslibrary;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.net.URI;

public class MainActivity extends AppCompatActivity {
    //TODO: Switch all auth and oth req. to GCP
    //TODO: Fix email and password textboxes.
    private static final String TAG = "MainActivity";
    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient signInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .build();
        signInClient = GoogleSignIn.getClient(this, gso);
        SignInButton signInButton = findViewById(R.id.btn_login);
        signInButton.setColorScheme(SignInButton.COLOR_DARK);
        signInButton.setSize(SignInButton.SIZE_WIDE);
        signInButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                authenticate();
            }
        });
    }

    @Override
    public void onStart(){
        super.onStart();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if(account != null){
            Intent intent = new Intent(MainActivity.this, HomeScreenActivity.class);
            startActivity(intent);
            Toast.makeText(getApplicationContext(), "Welcome Back", Toast.LENGTH_SHORT).show();
        }
    }

    private void authenticate(){
        //Start Authentication
        Intent signInIntent = signInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            Log.i(TAG, "Activity Result");
            handleSignInResult(task);
        }
    }
    //TODO: set display name
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask){
        try{
            Log.i(TAG, "Handling Sign In");
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            Intent intent = new Intent(MainActivity.this, HomeScreenActivity.class);
            startActivity(intent);
            Toast.makeText(getBaseContext(), "Success!", Toast.LENGTH_SHORT).show();
        }catch (ApiException e){
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(getBaseContext(), "Login Failed!", Toast.LENGTH_SHORT).show();
        }
    }
}
