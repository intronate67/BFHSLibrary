package net.huntersharpe.bfhslibrary;

import android.support.v4.app.Fragment;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.view.MenuItem;

public class HomeScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        final BottomNavigationView bnView = findViewById(R.id.bottom_navigation);
        if(savedInstanceState == null){
            bnView.setSelectedItemId(R.id.action_home);
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
                            final FragmentTransaction transaction = getSupportFragmentManager()
                                    .beginTransaction();
                            transaction.replace(R.id.fragment_container, fragment).commit();
                            return true;
                        }
                    }
            );
        }
    }
}
