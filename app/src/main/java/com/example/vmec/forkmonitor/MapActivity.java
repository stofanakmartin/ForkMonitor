package com.example.vmec.forkmonitor;


import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.vmec.forkmonitor.data.model.Post;
import com.example.vmec.forkmonitor.data.remote.APIService;
import com.example.vmec.forkmonitor.data.remote.ApiUtils;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MapActivity extends Activity implements LocationListener{
    private TextView latituteField;
    private TextView longitudeField;
    private TextView accuracyGPSField;
    private TextView logField;
    private TextView batteryField;
    private TextView info1Field;
    private TextView info2Field;
    private TextView apiField;

    private LocationManager locationManager;
    private String provider;
    private int counterS=0,counterF=0;
    //private LatLng corner = new LatLng(48.1111968,17.1065454);
    private LatLng corner = new LatLng(49.2610406,18.8734871);

    private Button mExpandButton;
    private Button mLayoutButton;

    private APIService mAPIService;

    private PixelGridView mPixelGrid;
    private TouchImageView mTouchImageView;

    Matrix matrix;
    public boolean isLayoutVisible=false;

    private double homeLat = 49.2610406;
    private double homeLng = 18.8734871;
    private int mBatt;


    private LinearLayout mInfoBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        String value = intent.getStringExtra("key"); //if it's a string you stored.
        Log.i("can","key: " + value);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_image);

        latituteField = (TextView) findViewById(R.id.TextView02);
        longitudeField = (TextView) findViewById(R.id.TextView04);
        accuracyGPSField = (TextView) findViewById(R.id.TextView06);
        logField = (TextView) findViewById(R.id.TextView08);
        batteryField = (TextView) findViewById(R.id.TextView10);
        info1Field = (TextView) findViewById(R.id.TextView12);
        info2Field = (TextView) findViewById(R.id.TextView14);
        apiField = (TextView) findViewById(R.id.TextView16);
        mInfoBar = (LinearLayout) findViewById(R.id.InfoBar);

        mInfoBar.setVisibility(View.INVISIBLE);

        mLayoutButton = (Button) findViewById(R.id.button1);
        mLayoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View button) {
                zoomClick();
            }
        });

        mExpandButton = (Button) findViewById(R.id.button2);
        mExpandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View button) {
                if (mInfoBar.getVisibility() == View.VISIBLE) {
                    mInfoBar.setVisibility(View.INVISIBLE);
                    mExpandButton.setText("+");
                }
                else {
                    mInfoBar.setVisibility(View.VISIBLE);
                    mExpandButton.setText("-");
                }
            }
        });


        mAPIService = ApiUtils.getAPIService();
        this.registerReceiver(this.batteryLevelReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        mPixelGrid = (PixelGridView) findViewById(R.id.pixel_grid_view);
        mTouchImageView = (TouchImageView) findViewById(R.id.touchImageView);
        mTouchImageView.setMaxZoom(4f);


        mTouchImageView.setDrawView(mPixelGrid);
        mPixelGrid.setParentMatrix(mTouchImageView.getParentMatrix());



        //setContentView(img);

        // Get the location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Define the criteria how to select the locatioin provider -> use
        // default
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }
        Location location = locationManager.getLastKnownLocation(provider);

        // Initialize the location fields
        if (location != null) {
            System.out.println("IMG " + provider + " has been selected.");
            //onLocationChanged(location);
        } else {
            latituteField.setText("Location not available");
            longitudeField.setText("Location not available");
        }



    }


    @Override
    protected void onResume() {
        super.onResume();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(provider, 500, 0.5f, this);

    }


    /* Remove the locationlistener updates when Activity is paused */
    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i("serial","" +android.os.Build.SERIAL);
        double lat = (location.getLatitude());
        double lng = (location.getLongitude());
        float acc = location.getAccuracy();
        latituteField.setText(String.valueOf(lat));
        longitudeField.setText(String.valueOf(lng));
        accuracyGPSField.setText(String.valueOf(acc));


        int evaualtionCode = mPixelGrid.evaluateLocation(lat,lng,acc);//    latLngToXYPoint(homeLat,homeLng,lat,lng);

        if (evaualtionCode == 0)
        {

            sendPost(android.os.Build.SERIAL, lat, lng, mBatt, acc);  //TODO bateriu posielat
            logField.setText("Odoslane poloha. Presnost: " +acc+" S"+String.valueOf(counterS)+"F:"+String.valueOf(counterF));

        }


        else if (evaualtionCode == -1) logField.setText("Zla presnost: " + String.valueOf(acc) +" S"+String.valueOf(counterS)+" F:"+String.valueOf(counterF));
        else if (evaualtionCode == -2) logField.setText("Bod mimo mapy"+" S"+String.valueOf(counterS)+" F:"+String.valueOf(counterF));
        else if (evaualtionCode == -3) logField.setText("Bod mimo polygonov"+" S"+String.valueOf(counterS)+" F:"+String.valueOf(counterF));


        else if (evaualtionCode > 0) logField.setText("Rovnaka bunka: " + String.valueOf(evaualtionCode) + "x"+" S"+String.valueOf(counterS)+" F:"+String.valueOf(counterF));

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

    public void sendPost(String name, double lat,double lng, double battery, double accuracy) {
        mAPIService.savePost(name, lat, lng,battery, accuracy,-1).enqueue(new Callback<Post>() {
            @Override
            public void onResponse(Call<Post> call, Response<Post> response) {
                if(response.isSuccessful()) {
                    counterS +=1;
                    apiField.setText(String.valueOf("Success"));
                    info2Field.setText(String.valueOf(counterS));
                }
            }

            @Override
            public void onFailure(Call<Post> call, Throwable t) {
                //Log.e("rest", "Unable to submit post to API.");
                counterF +=1;
                apiField.setText(String.valueOf("Fail"));
                info2Field.setText(String.valueOf(counterF));
            }
        });
    }



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

    private void zoomClick()  {
            isLayoutVisible = !isLayoutVisible;
            mPixelGrid.setLayoutVisible(isLayoutVisible);
            mPixelGrid.invalidate();
    }
}
