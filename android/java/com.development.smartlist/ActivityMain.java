package com.development.smartlist;

import android.support.annotation.NonNull;
import android.app.FragmentManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.design.widget.BottomNavigationView;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;

public class ActivityMain extends AppCompatActivity {

    private Fragment selectedFragment = null;
    private ImageView imgDeleteIcon;
    private ImageView imgAddIcon;
    private ImageView imgMenuIcon;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            switch (item.getItemId()) {
                case R.id.navigation_create_edit_list:
                    selectedFragment = FragmentCreateEditList.newInstance();
                    transaction.replace(R.id.frame_layout, selectedFragment, "fragmentCreateEditList");
                    imgAddIcon.setEnabled(true);
                    imgDeleteIcon.setEnabled(true);
                    imgMenuIcon.setEnabled(true);
                    break;
                case R.id.navigation_view_list:
                    /*
                    selectedFragment = FragmentViewList.newInstance();
                    transaction.replace(R.id.frame_layout, selectedFragment, "fragmentViewList");
                    imgAddIcon.setEnabled(false);
                    imgDeleteIcon.setEnabled(true);
                    imgMenuIcon.setEnabled(true);
                    */
                    break;
                case R.id.navigation_compare_list:
                    /*
                    selectedFragment = FragmentCompareList.newInstance();
                    transaction.replace(R.id.frame_layout, selectedFragment, "fragmentCompareList");
                    */
                    break;
            }

            //FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            //transaction.replace(R.id.frame_layout, selectedFragment, "fragment");
            transaction.commit();
            return true;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Display and setup the custom actionbar.
        Toolbar toolBar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(toolBar);
        //getSupportActionBar().setDisplayShowTitleEnabled(false);
        imgAddIcon = (ImageView)findViewById(R.id.iconAdd);
        imgDeleteIcon = (ImageView)findViewById(R.id.iconDelete);
        imgMenuIcon = (ImageView)findViewById(R.id.iconMenu);

        // Setup the fragment navigation panel
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        //Manually displaying the first fragment - one time only
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, FragmentCreateEditList.newInstance());
        transaction.commit();
    }
}
