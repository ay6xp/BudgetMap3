package com.example.ahmedyoussef.budgetmap;

import android.Manifest;
import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.support.v4.app.DialogFragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import android.util.JsonReader;
import android.util.JsonToken;
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

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.SphericalUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import static java.lang.Math.floor;

public class MapFragment extends Fragment implements OnMapReadyCallback, BudgetDialog.BudgetDialogListener {
    private static final String TAG = "Budget";
    private static final String TAG2 = "JSON";
    private Button btn;
    private GoogleMap mMap;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private boolean mLocationPermissionGranted = false;
    private static final int LOCATION_REQUST_CODE = 1234;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static final float DEFAULT_ZOOM = 10f;
    private LatLng mCoordinates;

    private double DEFAULT_RADIUS = 8046.72;
    private double mCurrentRadius = 8046.72;

    //widgets
    private EditText mSearchText;
    private PlaceAutocompleteFragment autocompleteFragment;
    private ImageView mGps;
    private ImageView mBudget;
    private ImageView mRadius;

    URL predictHqEndPoint;



    private BudgetDialog mBudgetDialog;


    @Nullable
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.v("func", "function is being called");
        View view = inflater.inflate(R.layout.map_tab, container, false);

        getLocationPermission();

       // mSearchText = (EditText) view.findViewById(R.id.input_search);
        autocompleteFragment = (PlaceAutocompleteFragment)
                getActivity().getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        mGps = (ImageView) view.findViewById(R.id.ic_gps);
        mBudget = (ImageView) view.findViewById(R.id.ic_money);
        mRadius = (ImageView) view.findViewById(R.id.ic_zoom);



        Log.v("framap", "Map has been created");


        return view;

    }

    private void init() {
        Log.d("TAG", "init:Initializing");
//        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                if(actionId== EditorInfo.IME_ACTION_SEARCH
//                    || actionId== EditorInfo.IME_ACTION_DONE
//                        || event.getAction() == KeyEvent.ACTION_DOWN
//                        || event.getAction() == KeyEvent.KEYCODE_ENTER){
//                    Log.d("TAG", "init:button pressed");
//                    //execture method for searching
//                    geoLocate();
//                }
//
//
//                return false;
//            }
//        });


        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                geoLocate(place.getName().toString());

                Log.i(TAG, "Place: " + place.getName());
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onclick: clicked gps icon");
                getDeviceLocation();
            }
        });
       // hideSoftKeyBoard();

        mBudget.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Log.d(TAG, "OnClick: budget icon pressed");
                //call budget function
                showBudgetDialog();

            }
        });


        mRadius.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Log.d(TAG, "OnClick: radius icon pressed");
                //radius function
                mCurrentRadius += DEFAULT_RADIUS;
                mMap.moveCamera(CameraUpdateFactory.zoomTo(getZoomLevel(mCoordinates,mCurrentRadius)));
            }
        });

        apiReader();

    }


    public void initMap() {
        Log.v(TAG, "InitMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public void apiReader() {

    new JsonTask().execute("");
    }


    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting the devices current location");
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        try {

            final Task location = mFusedLocationProviderClient.getLastLocation();
            location.addOnCompleteListener(getActivity(),new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Log.d(TAG, "onComplete: found Location");
                        Location currentLocation = (Location) task.getResult();
                        // Log.d(TAG, String.valueOf(currentLocation));

                        mCoordinates = new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());
                        moveCamera(mCoordinates, mCurrentRadius,"My Location");
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



    private void moveCamera(LatLng latlng, double radius, String title) {
        Log.d(TAG, "moveCamera: moving camera to lat " + latlng.latitude + ", lng " + latlng.longitude);


        if(!title.equals("My Location")) {
            MarkerOptions options = new MarkerOptions().position(latlng).title(title).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            mMap.addMarker(options);



       }

       float zoomRadius =  getZoomLevel(latlng, radius);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, zoomRadius));

        hideSoftKeyBoard();

    }
    public double convertMilesToMeters(double miles) {
        double meters = miles * 1609.344f;

        return meters;

    }

    public float getZoomLevel(LatLng latlng, double radius) {
        Log.v(TAG, "getZoomLevel");
        Circle circle = mMap.addCircle(new CircleOptions().center(latlng).radius(radius).strokeColor(Color.RED));
        circle.setVisible(true);
        int zoomLevel = 0;
        if (circle != null){
            double circleRadius = circle.getRadius();
            double scale = circleRadius / 500;
            zoomLevel =(int) (16 - Math.log(scale) / Math.log(2));
        }
        return zoomLevel;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

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
    private void geoLocate(String location) {
        Log.d(TAG, "geoLocate: geoLocating");
       // String searchString = mSearchText.getText().toString();
        String searchString = location;
        Geocoder geocoder = new Geocoder(getActivity());
        List<Address> list = new ArrayList<>();
        try {
            Log.d(TAG, " geoLocate: inside try statement");
            list = geocoder.getFromLocationName(searchString,1);


        }catch(IOException e ){
        Log.e(TAG, "geolocate: IOException" + e.getMessage());

        }
        if(list.size()>0) {
            Address address = list.get(0);
            Log.d(TAG, "geoLocate: found a location" + address.toString());
            //Toast.makeText(getActivity(), address.toString(), Toast.LENGTH_SHORT).show();
            mCoordinates = new LatLng(address.getLatitude(), address.getLongitude());
            Log.d(TAG, "coordinates: coordinates" + mCoordinates.latitude + " , " + mCoordinates.longitude);
            moveCamera(mCoordinates,mCurrentRadius, address.getAddressLine(0));

        }




    }

    private void hideSoftKeyBoard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

    }

    public void showBudgetDialog() {

        mBudgetDialog = new BudgetDialog();

        mBudgetDialog.setTargetFragment(this,0);

        mBudgetDialog.show(getActivity().getSupportFragmentManager(),"Budget");

    }


    @Override
    public void onDialogPositiveClick(DialogFragment dialog, String budget, String radius) {
        Log.v(TAG, "Dialog: Update clicked " + budget + " radius: " + radius);
        mCurrentRadius = convertMilesToMeters(Double.valueOf(radius));
        moveCamera(mCoordinates,mCurrentRadius,"New Location");
        apiReader();

    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
    Log.v(TAG, "Dialog: Cancel clicked");
    }


    private class JsonTask extends AsyncTask<String, Integer, String> {

        ArrayList<MarkerOptions> markerList;
        @Override
        protected void onPreExecute() {
            markerList = new ArrayList<MarkerOptions>();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... values) {


                    try {
                        Log.v(TAG, "Coordinates: " + Double.toString(mCoordinates.latitude) + " " + Double.toString(mCoordinates.longitude) + " radius " + Integer.toString((int)mCurrentRadius));
                        predictHqEndPoint = new URL("https://api.predicthq.com/v1/events/?within=" + Integer.toString((int)mCurrentRadius) + "m@" + Double.toString(mCoordinates.latitude) + "," + Double.toString(mCoordinates.longitude));
                        //predictHqEndPoint = new URL("https://api.predicthq.com/v1/events/?within=10mi@39.0988655,-77.5703502");
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }

                    try {
                        HttpsURLConnection myConnection =
                                (HttpsURLConnection) predictHqEndPoint.openConnection();
                        myConnection.addRequestProperty("client_id", "phq.XP5diLSO3dLXtJRQxj5cui8mLam3M96jlswwgaQi");
                        myConnection.addRequestProperty("client_secret", "2bE4EzjFS5rDQvf6ogmplmw14QZbFhdDrff5eKwn");
                        myConnection.setRequestProperty("Authorization", "Bearer " + "qbml8YlC7zgwG7fU87WBwcQYEXKfj2");

                        if (myConnection.getResponseCode() == 200) {

                            Log.v(TAG, "PredictHQ: request success");
                            InputStream responseBody = myConnection.getInputStream();
                            InputStreamReader responseBodyReader =
                                    new InputStreamReader(responseBody, "UTF-8");

                            JsonReader jsonReader = new JsonReader(responseBodyReader);
                            //markerList.clear();
                            readBegin(jsonReader);

                            jsonReader.close();

                        } else {
                            // Error handling code goes here
                            Log.v(TAG, "PredictHQ: request fail, code:" + myConnection.getResponseCode());
                        }


                        myConnection.disconnect();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


//            double latitude = (double) values[0];
//            double longitude = (double) values[1];
//            String title = (String) values[2];
//
//            MarkerOptions options = new MarkerOptions().position(new LatLng(latitude, longitude)).title(title);
//            mMap.addMarker(options);




            return "Success";
        }


        public void readBegin(JsonReader reader)
        {
            try
            {
                reader.beginObject();
                while (reader.hasNext())
                {
                    String name = reader.nextName();
                    Log.v(TAG, "json: " + name);
                    if (name.equals("results"))
                    {
                        reader.beginArray();
                        while (reader.hasNext())
                        {
                            read(reader);
                        }
                        reader.endArray();
                    }
                    else
                    {
                        reader.skipValue();
                    }
                }
                reader.endObject();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        public void read(JsonReader reader) throws Exception
        {
            String title = "";
            reader.beginObject();
            while (reader.hasNext())
            {
                String name = reader.nextName();
                if (name.equals("id"))
                {
                    String id = reader.nextString();
                    Log.v(TAG2, "reading id: " + id);
                }
                else if (name.equals("title"))
                {

                    title = reader.nextString();
                    Log.v(TAG2, " title: " + title);

                }
                else if (name.equals("description"))
                {

                    String description = reader.nextString();
                    Log.v(TAG2, " description: " + description);

                }
                else if (name.equals("location"))
                {
                    if (reader.hasNext())
                    {
                        JsonToken peek = reader.peek();
                        if (peek == JsonToken.NULL)
                        {
                            reader.skipValue();
                        }
                        else
                        {

                            // String s = reader.nextName();

                            reader.beginArray();
                            double latitude = 0;
                            double longitude = 0;
                            while (reader.hasNext())
                            {
                                latitude = reader.nextDouble();
                                longitude = reader.nextDouble();
                            }
                            reader.endArray();


                            Log.v(TAG2, "latitude: " + latitude + " longitude: "+ longitude);
                            Log.v(TAG, "markers: adding them");
                            markerList.add(new MarkerOptions().position(new LatLng(longitude, latitude)).title(title).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                            // new MarkerTask().execute(latitude, longitude, title);
//                        final double finalLatitude = latitude;
//                        final double finalLongitude = longitude;
//                        final String finalTitle = title;
//                        getActivity().runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        MarkerOptions options = new MarkerOptions().position(new LatLng(finalLatitude, finalLongitude)).title(finalTitle).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
//                                        Log.v(TAG, "SyncTask called" + finalLatitude + " , " + finalLongitude + " " + finalTitle);
//                                        mMap.addMarker(options);
//
//
//                                    }
//                                });



                        }
                    }

                }
                else if (name.equals("start"))
                {

                    String start = reader.nextString();
                    Log.v(TAG2, "start: " + start);

                }
                else if (name.equals("end"))
                {
                    String end = reader.nextString();
                    Log.v(TAG2, "end: " + end);
                }
                else
                {
                    reader.skipValue();
                }
            }
            reader.endObject();


        }


        @Override
        protected void onPostExecute(String result) {
            Log.v(TAG, "AsyncTask: addingMarkers " + result);
            Log.v(TAG, "addMarkers() adding markers, size = " + markerList.size());
            for(int i = 0; i < markerList.size();i++) {
                Log.v(TAG, "addMarkers(): " + markerList.get(i).getTitle());
                mMap.addMarker(markerList.get(i));

            }

        }
    }


}
