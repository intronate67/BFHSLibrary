package net.huntersharpe.bfhslibrary;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class HomeScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        final BottomNavigationView bnView = findViewById(R.id.bottom_navigation);
        if(savedInstanceState == null){
            bnView.setSelectedItemId(R.id.action_home);
            final FragmentTransaction transaction = getFragmentManager()
                    .beginTransaction();
            transaction.replace(R.id.fragment_container, new Home()).commit();
        }
        if(bnView != null){
            bnView.setOnNavigationItemSelectedListener(
                    new BottomNavigationView.OnNavigationItemSelectedListener() {
                        @Override
                        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                            Fragment fragment = null;
                            switch(item.getItemId()){
                                case R.id.action_home:
                                    fragment = new Home();
                                    break;
                                case R.id.action_dashboard:
                                    fragment = new Dashboard();
                                    break;
                                case R.id.action_search:
                                    fragment = new Search();
                                    break;
                            }
                            final FragmentTransaction transaction = getFragmentManager()
                                    .beginTransaction();
                            transaction.replace(R.id.fragment_container, fragment).commit();
                            return true;
                        }
                    }
            );
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){

        getMenuInflater().inflate(R.menu.action_bar_menu, menu);
        return true;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.actionBarLogOutItem:
                @SuppressLint("RestrictedApi") GoogleSignInClient client = GoogleSignIn.getClient(getApplicationContext(),
                        GoogleSignInOptions.DEFAULT_SIGN_IN);
                client.signOut().addOnCompleteListener(this,
                        new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                Toast.makeText(getBaseContext(), "Logged Out", Toast.LENGTH_SHORT).show();
                            }
                        });
                return(true);
            case R.id.notificationsItem:
                FragmentManager manager = getFragmentManager();
                Notifications notifications = new Notifications();
                FragmentTransaction fragmentTransaction = manager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, notifications);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                return(true);
        }
        return true;
    }
}
