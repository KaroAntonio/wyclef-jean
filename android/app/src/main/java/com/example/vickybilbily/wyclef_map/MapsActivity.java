package com.example.vickybilbily.wyclef_map;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.example.vickybilbily.wyclef_map.CanvasView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        MotionListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    private LocationRequest mLocationRequest;
    private Projection mProjection;
    private List<Polyline> mPolylineList;
    private Polyline mPolyline;
    private PolylineOptions mPolylineOptions;
    private List<Stroke> mStrokeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds

        // Request permission for location
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        //Instantiate some stuff
        mPolylineList = new ArrayList<Polyline>();
        mStrokeList = new ArrayList<Stroke>();
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("action", "do you work???");
        mMap = googleMap;
        //No gestures enabled by default
        UiSettings mapSettings = mMap.getUiSettings();
        mapSettings.setAllGesturesEnabled(false);
        CanvasView canvas = (CanvasView) findViewById(R.id.touchme);
        canvas.setMotionListener(this);
        mProjection = mMap.getProjection();
        setupRadio();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        } else {
            handleLocation();
        }

    }

    public void handleLocation() {
        //Location permissions
        int permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {

            mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            if (mLocation != null) {
                Log.d("location", Double.toString(mLocation.getLongitude()));
                mMap.addMarker(new MarkerOptions().position(new LatLng(mLocation.getLatitude(), mLocation.getLongitude())).title("HERE"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()), 15));
                mProjection = mMap.getProjection();
            }
        }
        //Request location permission
        else {
            Log.d("permission", "not granted");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        handleLocation();
    }


    @Override
    public void onDown(MotionEvent event){
        LatLng position = mProjection.fromScreenLocation(new Point((int) event.getX(), (int) event.getY()));
        mPolylineOptions = new PolylineOptions()
                .add(position)
                .color(Color.RED)
                .width(10);
        mPolylineList.add(mMap.addPolyline(mPolylineOptions));
        //mMap.addCircle(new CircleOptions().center(position).radius(10));
    }

    @Override
    public void onMove(MotionEvent event){
        LatLng position = mProjection.fromScreenLocation(new Point((int) event.getX(), (int) event.getY()));
        mPolylineOptions.add(position);
        mPolylineList.add(mMap.addPolyline(mPolylineOptions));
        //mMap.addCircle(new CircleOptions().center(position).radius(10));
    }

    @Override
    public void onUp(MotionEvent event){
        //TODO create stroke! and clear all other polylines
        LatLng position = mProjection.fromScreenLocation(new Point((int) event.getX(), (int) event.getY()));
        mPolylineOptions.add(position);
        Polyline newLine = mMap.addPolyline(mPolylineOptions);
        mPolylineList.clear();
        mStrokeList.add(new Stroke(mPolylineOptions, "currentUid"));
    }

    public void setupRadio(){
        RadioGroup rg = (RadioGroup) findViewById(R.id.modes);
        RadioButton drawBtn = (RadioButton) findViewById(R.id.draw);
        drawBtn.setChecked(true);
        final View canvas = findViewById(R.id.touchme);
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId){
                    case R.id.draw:
                        mProjection = mMap.getProjection();
                        canvas.setVisibility(View.VISIBLE);
                        mMap.getUiSettings().setAllGesturesEnabled(false);
                        break;
                    case R.id.move:
                        canvas.setVisibility(View.GONE);
                        mMap.getUiSettings().setAllGesturesEnabled(true);
                        mMap.getUiSettings().setZoomControlsEnabled(true);
                        mMap.getUiSettings().setScrollGesturesEnabled(true);
                        break;
                    default:
                        break;
                }
            }
        });
    }
}
