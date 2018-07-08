package com.example.ahmedyoussef.budgetmap;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment implements OnMapReadyCallback {
    private static final String TAG = "Budget";
    private Button btn;
    private GoogleMap mMap;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private boolean mLocationPermissionGranted = false;
    private static final int LOCATION_REQUST_CODE = 1234;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static final float DEFAULT_ZOOM = 7f;

    //widgets
    private EditText mSearchText;
    private ImageView mGps;

    @Nullable
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.v("func", "function is being called");
        View view = inflater.inflate(R.layout.map_tab, container, false);

        getLocationPermission();

        mSearchText = (EditText) view.findViewById(R.id.input_search);
        mGps = (ImageView) view.findViewById(R.id.ic_gps);

        Log.v("framap", "Map has been created");
        return view;

    }

    private void init() {
        Log.d("TAG", "init:Initializing");
        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId== EditorInfo.IME_ACTION_SEARCH
                    || actionId== EditorInfo.IME_ACTION_DONE
                        || event.getAction() == KeyEvent.ACTION_DOWN
                        || event.getAction() == KeyEvent.KEYCODE_ENTER){
                    Log.d("TAG", "init:button pressed");
                    //execture method for searching
                    geoLocate();
                }


                return false;
            }
        });
        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onclick: clicked gps icon");
                getDeviceLocation();
            }
        });
        hideSoftKeyBoard();
    }

    public void initMap() {
        Log.v("TAG", "InitMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting the devices current location");
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        try {
//            if(mLocationPermissionGranted) {
//                LocationRequest mLocationRequest = LocationRequest.create();
//                mLocationRequest.setInterval(60000);
//                mLocationRequest.setFastestInterval(5000);
//                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//                LocationCallback mLocationCallback = new LocationCallback() {
//                    @Override
//                    public void onLocationResult(LocationResult locationResult) {
//                        if (locationResult == null) {
//                            return;
//                        }
//                        for (Location location : locationResult.getLocations()) {
//                            if (location != null) {
//                                //TODO: UI updates.
//
//                            }
//                        }
//                    }
//                };
//
//
//                LocationServices.getFusedLocationProviderClient(getActivity()).getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
//                    @Override
//                    public void onSuccess(Location location) {
//                        //TODO: UI updates.
//                        moveCamera(new LatLng(location.getLatitude(), location.getLongitude()), DEFAULT_ZOOM);
//
//
//                    }
//                });
//            }

            final Task location = mFusedLocationProviderClient.getLastLocation();
            location.addOnCompleteListener(getActivity(),new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Log.d(TAG, "onComplete: found Location");
                        Location currentLocation = (Location) task.getResult();
                        // Log.d(TAG, String.valueOf(currentLocation));

                        moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM,"My Location");
                        init();

                    } else {
                        Log.d(TAG, "onComplete: current location is null");
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(30,30),DEFAULT_ZOOM));
                        mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        Toast.makeText(getActivity(), "unable to get current Location", Toast.LENGTH_SHORT).show();
                        init();
                    }
                }
            });


        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation:getting the devices current location");


        }


    }

    private void moveCamera(LatLng latlng, float zoom, String title) {
        Log.d(TAG, "moveCamera: moving camera to lat " + latlng.latitude + ", lng " + latlng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, zoom));

        if(!title.equals("My Location")) {
            MarkerOptions options = new MarkerOptions().position(latlng).title(title);
            mMap.addMarker(options);

       }

        hideSoftKeyBoard();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera

        // mMap.setMyLocationEnabled(true);
//        LatLng home = new LatLng(39.1157, 77.5636);
//
//        mMap.addMarker(new MarkerOptions().position(home).title("Marker in Leesburg"));
//            mMap.moveCamera(CameraUpdateFactory.newLatLng(home));

        if (mLocationPermissionGranted) {
            getDeviceLocation();
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            mMap.setMyLocationEnabled(true);
            //mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }

    }

    private void getLocationPermission() {
        Log.v(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),COURSE_LOCATION)== PackageManager.PERMISSION_GRANTED){
               initMap();

                mLocationPermissionGranted = true;
            }
        }else{
            ActivityCompat.requestPermissions(getActivity(),permissions, LOCATION_REQUST_CODE);
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.v(TAG, "onRequestPermission: requesting location");
        mLocationPermissionGranted = false;
        switch(requestCode){
            case LOCATION_REQUST_CODE: {
                if(grantResults.length > 0){

                    for(int i = 0; i < grantResults.length; i++){
                        if(grantResults[i]!=PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionGranted = false;
                            return;
                        }
                    }
                    mLocationPermissionGranted = true;
                    initMap();
                    //initialize map
                }
            }
        }

    }
    private void geoLocate() {
        Log.d(TAG, "geoLocate: geoLocating");
        String searchString = mSearchText.getText().toString();
        Geocoder geocoder = new Geocoder(getActivity());
        List<Address> list = new ArrayList<>();
        try {
            list = geocoder.getFromLocationName(searchString,1);


        }catch(IOException e ){
        Log.e(TAG, "geolocate: IOException" + e.getMessage());

        }
        if(list.size()>0) {
            Address address = list.get(0);
            Log.d(TAG, "geoLocate: found a location" + address.toString());
            //Toast.makeText(getActivity(), address.toString(), Toast.LENGTH_SHORT).show();

            moveCamera(new LatLng(address.getLatitude(),address.getLongitude()),DEFAULT_ZOOM, address.getAddressLine(0));

        }




    }

    private void hideSoftKeyBoard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mSearchText.getWindowToken(), 0);
    }

}
