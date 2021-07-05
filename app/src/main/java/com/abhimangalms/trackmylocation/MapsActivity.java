package com.abhimangalms.trackmylocation;


import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;

import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    final static int PERMISSION_ALL = 1;
    final static String[] PERMISSIONS = {android.Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION};
    private GoogleMap mMap;
    private GoogleApiClient client;
    MarkerOptions mo;
    Marker marker;
    String type = "";

    ToggleButton mtoggleButton;
    TextView mcurrentLocation;
    Button mClearDataButton;

    LatLng mUserLocation;
    LatLng trackedLocation;
    LocationManager locationManager;
    double lat, lon;
    String area = " ";
    DatabaseHandler db;

    String VIEW_TYPE;
    String VIEW_TYPE_KEY = "viewType";
    boolean locationTrackEnabled = false; // tracking needed
    boolean locationTrackButton = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Bundle extras = getIntent().getExtras();
        VIEW_TYPE = extras.getString(VIEW_TYPE_KEY, "map");


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        db = new DatabaseHandler(this);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        mo = new MarkerOptions().position(new LatLng(0, 0)).title("My Current Location");

        if (Build.VERSION.SDK_INT >= 23 && !isPermissionGranted()) {
            requestPermissions(PERMISSIONS, PERMISSION_ALL);
        } else requestLocation();
        if (!isLocationEnabled())
            showAlert(1);


        mtoggleButton = findViewById(R.id.toggleButton);
        mcurrentLocation = findViewById(R.id.tvCurrentLocation);
        mClearDataButton = findViewById(R.id.btnClearData);


        if (VIEW_TYPE.equals("map")) { // VIEW_TYPE "map"

            mClearDataButton.setVisibility(View.GONE);
            locationTrackEnabled = true;

        } else { // VIEW_TYPE "track"

            mcurrentLocation.setVisibility(View.GONE);
            mtoggleButton.setVisibility(View.GONE);
            locationTrackEnabled = false;


//            showLocations(); //show tracked locations
        }


        mtoggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mtoggleButton.isChecked()) {

                    Toast.makeText(MapsActivity.this, "Loction tracking enabled", Toast.LENGTH_SHORT).show();
                    type = "Start";
                    locationTrackButton = true;
                    getLocation();
                } else {
                    Toast.makeText(MapsActivity.this, "Location tracking disabled", Toast.LENGTH_SHORT).show();
                    type = "Stop";
                    mcurrentLocation.setText(""
                    );
                    //getLocation();
                }
            }

        });

        mClearDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.deleteAllLocations();
                if (mMap != null) {
                    mMap.clear();
                } else {
                    Toast.makeText(MapsActivity.this, "Go back to see the update!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void showLocations() {

        // Reading all contacts
        Log.d("Reading: ", "Reading all contacts..");
        List<LocationModel> locations = db.getAllLocation();

        if (locations.isEmpty()) {
            Toast.makeText(this, "No tracked locations available !!", Toast.LENGTH_SHORT).show();
        } else {
            for (LocationModel cn : locations) {

                String log = "Lat: " + cn.getLatitude() + " ,Lon: " +
                        cn.getLongitude();
                Log.d("LOCATIONS: ", log);

                trackedLocation = new LatLng(Double.parseDouble(cn.getLatitude()), Double.parseDouble(cn.getLongitude()));
                marker = mMap.addMarker(new MarkerOptions()
                        .position(trackedLocation).title("You were here"));
                marker.setPosition(trackedLocation);
            }
        }
    }

    private void getLocation() {

        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 5, this);

        } catch (SecurityException e) {
            e.printStackTrace();
        }
        //  new SendRequest().execute();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
//        marker = mMap.addMarker(mo);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            bulidGoogleApiClient();
            mMap.setMyLocationEnabled(true);
            mMap.setTrafficEnabled(false);
            mMap.setIndoorEnabled(false);
            mMap.setBuildingsEnabled(false);
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.setPadding(0, 10, 0, 250);

            if (locationTrackEnabled) { //only for tracking location

            } else { //only for showing tracked location
                showLocations();
            }

        }

    }

    protected synchronized void bulidGoogleApiClient() {
        client = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        client.connect();

    }

    @Override
    public void onLocationChanged(Location location) {


        if (locationTrackEnabled) { //if tracking is needed

            mUserLocation = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.clear();
            marker = mMap.addMarker(new MarkerOptions()
                    .position(mUserLocation).title("You here"));
            //.icon(BitmapDescriptorFactory.fromResource(R.drawable.icon))
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mUserLocation, 5.0f));
            lat = location.getLatitude();
            lon = location.getLongitude();


            if (locationTrackButton) { //store location
                Log.d("onLocationChanged ", "Inserting ..");
                db.addLocation(new LocationModel(String.valueOf(lat), String.valueOf(lon)));

                Log.d("onLocationChanged", "Latitude: " + location.getLatitude() + "\n Longitude: " + location.getLongitude());
                try {
                    Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    mcurrentLocation.setText("Latitude: " + location.getLatitude() + "\nLongitude: " + location.getLongitude() + "\n" + addresses.get(0).getAddressLine(0));

                    area = addresses.get(0).getLocality();
                } catch (Exception e) {

                }
            } else {

                // no need to store locatin
            }

            //  new SendRequest().execute();
            LatLng myCoordinates = new LatLng(location.getLatitude(), location.getLongitude());
            marker.setPosition(myCoordinates);
            //mMap.moveCamera(CameraUpdateFactory.newLatLng(myCoordinates));

            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                    .target(mMap.getCameraPosition().target)
                    .zoom(11)
                    .bearing(30)
                    .tilt(45)
                    .build()));


            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker arg0) {

                    final LatLng startPosition = marker.getPosition();
                    final LatLng finalPosition = new LatLng(12.7801569, 77.4148528);
                    final Handler handler = new Handler();
                    final long start = SystemClock.uptimeMillis();
                    final Interpolator interpolator = new AccelerateDecelerateInterpolator();
                    final float durationInMs = 3000;
                    final boolean hideMarker = false;

                    handler.post(new Runnable() {
                        long elapsed;
                        float t;
                        float v;

                        @Override
                        public void run() {
                            // Calculate progress using interpolator
                            elapsed = SystemClock.uptimeMillis() - start;
                            t = elapsed / durationInMs;

                            LatLng currentPosition = new LatLng(
                                    startPosition.latitude * (1 - t) + finalPosition.latitude * t,
                                    startPosition.longitude * (1 - t) + finalPosition.longitude * t);

                            marker.setPosition(currentPosition);

                            // Repeat till progress is complete.
                            if (t < 1) {
                                // Post again 16ms later.
                                handler.postDelayed(this, 16);
                                //marker.setVisible(true);
                            } else {
                                if (hideMarker) {
                                    marker.setVisible(false);
                                } else {
                                    marker.setVisible(true);
                                }
                            }
                        }
                    });

                    return true;

                }

            });
        } else { // to show tracked location
//            showLocations();
        }


    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String s) {


    }

    @Override
    public void onProviderDisabled(String s) {

        Toast.makeText(MapsActivity.this, "Please Enable GPS and Internet", Toast.LENGTH_SHORT).show();

    }

    private void requestLocation() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        String provider = locationManager.getBestProvider(criteria, true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(provider, 5000, 10, this);
    }

    private boolean isLocationEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    // @RequiresApi(api = Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean isPermissionGranted() {
        if (checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED || checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Log.v("mylog", "Permission is granted");
            return true;
        } else {
            Log.v("mylog", "Permission not granted");
            return false;
        }
    }

    private void showAlert(final int status) {
        String message, title, btnText;
        if (status == 1) {
            message = "Your Locations Settings is set to 'Off'.\nPlease Enable Location to " +
                    "use this app";
            title = "Enable Location";
            btnText = "Location Settings";
        } else {
            message = "Please allow this app to access location!";
            title = "Permission access";
            btnText = "Grant";
        }
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setCancelable(false);
        dialog.setTitle(title)
                .setMessage(message)
                .setPositiveButton(btnText, new DialogInterface.OnClickListener() {
                    //  @RequiresApi(api = Build.VERSION_CODES.M)
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        if (status == 1) {
                            Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(myIntent);
                        } else
                            requestPermissions(PERMISSIONS, PERMISSION_ALL);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        finish();
                    }
                });
        dialog.show();
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

}

