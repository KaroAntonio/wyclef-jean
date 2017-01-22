package com.example.vickybilbily.wyclef_map;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import petrov.kristiyan.colorpicker.ColorPicker;

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
    private PolylineOptions mPolylineOptions;
    private List<Stroke> mStrokeList;
    private DataHandler dataHandler;
    private int mColor;
    private ColorPicker mColorPicker;
    private final double MAX_RADIUS = 1000;

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
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        dataHandler = new DataHandler();

        //Instantiate some stuff
        mPolylineList = new ArrayList<Polyline>();
        mStrokeList = new ArrayList<Stroke>();
        mColor = Color.RED;
        CanvasView canvas = (CanvasView) findViewById(R.id.touchme);
        canvas.setMotionListener(this);
        canvas.setVisibility(View.VISIBLE);
        setupColorPicker();
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            // already signed in
        } else {
            startActivity(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setProviders(Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                    new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build()))
                            .build());
        }
    }

    /**
     * Map setup on map ready
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //No gestures enabled by default
        UiSettings mapSettings = mMap.getUiSettings();
        mapSettings.setAllGesturesEnabled(false);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mProjection = mMap.getProjection();
        setupRadio();
        setupUndo();
    }

    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    if (location == null) {
                        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
                    } else {
                        handleLocation();
                    }
                }
                return;
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        else {
            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (location == null) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            } else {
                handleLocation();
            }
        }

    }

    public void handleLocation() {
        //Location permissions
        int permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {

            mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            if (mLocation != null) {
                mMap.addMarker(new MarkerOptions().position(new LatLng(mLocation.getLatitude(), mLocation.getLongitude())).title("YOU"));
                mMap.addCircle(new CircleOptions().radius(MAX_RADIUS).strokeColor(Color.LTGRAY).center(new LatLng(mLocation.getLatitude(), mLocation.getLongitude())));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()), 15));
                mProjection = mMap.getProjection();
            }
        }
        //Request location permission
        else {
            Log.d("permission", "not granted");
        }
    }

    @Override
    public void onDown(MotionEvent event) {
        LatLng position = mProjection.fromScreenLocation(new Point((int) event.getX(), (int) event.getY()));
        if (position != null &&
                distance(position.latitude, position.longitude, mLocation.getLatitude(), mLocation.getLongitude()) < MAX_RADIUS) {
            mPolylineOptions = new PolylineOptions()
                    .add(position)
                    .color(mColor)
                    .width(10);
            mPolylineList.add(mMap.addPolyline(mPolylineOptions));
        }
        //mMap.addCircle(new CircleOptions().center(position).radius(10));
    }

    @Override
    public void onMove(MotionEvent event) {
        LatLng position = mProjection.fromScreenLocation(new Point((int) event.getX(), (int) event.getY()));
        if (position != null &&
                distance(position.latitude, position.longitude, mLocation.getLatitude(), mLocation.getLongitude()) < MAX_RADIUS) {
            mPolylineOptions.add(position);
            mPolylineList.add(mMap.addPolyline(mPolylineOptions));
        }
        //mMap.addCircle(new CircleOptions().center(position).radius(10));
    }

    @Override
    public void onUp(MotionEvent event) {
        LatLng position = mProjection.fromScreenLocation(new Point((int) event.getX(), (int) event.getY()));
        if (position != null &&
                distance(position.latitude, position.longitude, mLocation.getLatitude(), mLocation.getLongitude()) < MAX_RADIUS) {
            mPolylineOptions.add(position);
            Polyline newLine = mMap.addPolyline(mPolylineOptions);
            mPolylineList.clear();
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                Stroke stroke = new Stroke(mPolylineOptions, user.getUid());
                mStrokeList.add(stroke);
                dataHandler.saveStroke(this, stroke);
            } else {
                startActivity(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setProviders(Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                        new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build()))
                                .build());
            }

        }
    }

    public double distance(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        float dist = (float) (earthRadius * c);
        return dist;
    }

    public void enableDrawing() {
        View canvas = findViewById(R.id.touchme);
        mProjection = mMap.getProjection();
        canvas.setVisibility(View.VISIBLE);
        UiSettings settings = mMap.getUiSettings();
        settings.setAllGesturesEnabled(false);
        settings.setZoomControlsEnabled(false);
        settings.setMyLocationButtonEnabled(false);
    }

    public void enableMoving() {
        View canvas = findViewById(R.id.touchme);
        canvas.setVisibility(View.GONE);
        UiSettings settings = mMap.getUiSettings();
        settings.setAllGesturesEnabled(true);
        settings.setZoomControlsEnabled(true);
        settings.setScrollGesturesEnabled(true);
        settings.setMyLocationButtonEnabled(true);
    }

    public void setupRadio() {
        RadioGroup rg = (RadioGroup) findViewById(R.id.modes);
        RadioButton drawBtn = (RadioButton) findViewById(R.id.draw);
        drawBtn.setChecked(true);
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.draw:
                        enableDrawing();
                        break;
                    case R.id.move:
                        enableMoving();
                        break;
                    default:
                        break;
                }
            }
        });
    }

    public void setupColorPicker() {
        View colorBtn = findViewById(R.id.colors);
        colorBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //v.getParent().removeView();
                mColorPicker = new ColorPicker(MapsActivity.this);
                mColorPicker.setOnChooseColorListener(new ColorPicker.OnChooseColorListener() {
                    @Override
                    public void onChooseColor(int position, int color) {
                        mColor = color;
                    }

                    @Override
                    public void onCancel() {
                        // put code
                    }
                });
                mColorPicker.show();
            }
        });
    }

    public void setupUndo() {
        View undoBtn = findViewById(R.id.undo);
        undoBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("action", Integer.toString(mStrokeList.size()));
                if (mStrokeList.size() > 0) {
                    mStrokeList.remove(mStrokeList.size() - 1);
                    redrawMap();
                }
            }
        });
    }

    public void redrawMap() {
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(new LatLng(mLocation.getLatitude(), mLocation.getLongitude())).title("YOU"));
        mMap.addCircle(new CircleOptions().radius(MAX_RADIUS).strokeWidth(5).strokeColor(Color.LTGRAY).center(new LatLng(mLocation.getLatitude(), mLocation.getLongitude())));
        for (Stroke s : mStrokeList) {
            mMap.addPolyline(s.getPolylineOptions());
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
//        dataHandler.subscribeToStrokes(location.getLatitude(), location.getLongitude(), new DataHandler.StrokeListener() {
//            @Override
//            public void onStroke(Stroke stroke) {
//                mStrokeList.add(stroke);
//                redrawMap();
//            }
//        });
    }

}
