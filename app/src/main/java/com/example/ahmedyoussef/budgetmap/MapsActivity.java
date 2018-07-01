package com.example.ahmedyoussef.budgetmap;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TableLayout;
import 	android.support.design.widget.TabLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;


public class MapsActivity extends AppCompatActivity {

    private GoogleMap mMap;
    private static final String TAG = "MainActivity";
    private SectionPageAdapter mSectionPageAdapter;
    private ViewPager mViewPager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        mSectionPageAdapter = new SectionPageAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.container);
        setupViewPage(mViewPager);
        Log.d("creating","on create is working here");
        TabLayout tabLayout =(TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        Log.d("complete","on create is done");



    }

    private void setupViewPage(ViewPager viewPager){
        SectionPageAdapter adapter = new SectionPageAdapter(getSupportFragmentManager());
        adapter.addFragment(new MapFragment(),"Budget");
        adapter.addFragment(new ProfileFragment(), "Profile");
        adapter.addFragment(new SettingFragment(), "Trending");
        adapter.addFragment(new TrendingFragment(), "Settings");
        viewPager.setAdapter(adapter);
        Log.d("adapter","adapter is setup");
    }


}
