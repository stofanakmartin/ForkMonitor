package com.forkmonitor;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements LocationListener{
    private TextView latituteField;
    private TextView longitudeField;
    private TextView accuracyGPSField;
    private TextView accuracyGoogleField;
    private TextView batteryField;
    private TextView info1Field;
    private TextView info2Field;
    private TextView apiField;
    private LocationManager locationManager;
    private String provider;




    private int hue;
    private int mBatt;

    private Button mLogButton,mMapButton;




    private BroadcastReceiver batteryLevelReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctxt, Intent intent) {
            int rawlevel = intent.getIntExtra("level", -1);
            int scale = intent.getIntExtra("scale", -1);
            int level = -1;
            if (rawlevel >= 0 && scale > 0) {
                level = (rawlevel * 100) / scale;
            }
            batteryField.setText(String.valueOf(level + "%"));
            mBatt = level;
        }
    };

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        hue = 0;
        mBatt = 0;

        mMapButton = (Button) findViewById(R.id.Buton1);
        mMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View button) {
                Log.i("can","key: " + 100);
                Intent myIntent = new Intent(MainActivity.this, MapActivity.class);
                myIntent.putExtra("key", 100); //Optional parameters
                MainActivity.this.startActivity(myIntent);
            }
        });


        mLogButton = (Button) findViewById(R.id.Buton2);
        mLogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View button) {

            }
        });


        this.registerReceiver(this.batteryLevelReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));



        latituteField = (TextView) findViewById(R.id.TextView02);
        longitudeField = (TextView) findViewById(R.id.TextView04);
        accuracyGPSField = (TextView) findViewById(R.id.TextView06);
        accuracyGoogleField = (TextView) findViewById(R.id.TextView08);
        batteryField = (TextView) findViewById(R.id.TextView10);
        info1Field = (TextView) findViewById(R.id.TextView12);
        info2Field = (TextView) findViewById(R.id.TextView14);
        apiField = (TextView) findViewById(R.id.TextView16);

        // Get the location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Define the criteria how to select the locatioin provider -> use
        // default
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = locationManager.getLastKnownLocation(provider);

        // Initialize the location fields
        if (location != null) {
            System.out.println("Provider " + provider + " has been selected.");
            onLocationChanged(location);
        } else {
            latituteField.setText("Location not available");
            longitudeField.setText("Location not available");
        }


    }

    /* Request updates at startup */
    @Override
    protected void onResume() {
        super.onResume();

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
        locationManager.requestLocationUpdates(provider, 400, 1, this);

    }

    /* Remove the locationlistener updates when Activity is paused */
    @Override
    protected void onPause() {
        super.onPause();
        //locationManager.removeUpdates(this);

        //mSensorManager.unregisterListener(this);
    }

    @Override
    public void onLocationChanged(Location location) {


        double lat = (location.getLatitude());
        double lng = (location.getLongitude());
        float acc = location.getAccuracy();
        float[] dist = new float[2];

        latituteField.setText(String.valueOf(lat));
        longitudeField.setText(String.valueOf(lng));
        accuracyGPSField.setText(String.valueOf(acc));

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


}

