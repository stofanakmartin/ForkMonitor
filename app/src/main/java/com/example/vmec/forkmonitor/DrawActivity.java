package com.example.vmec.forkmonitor;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.graphics.Color;
import android.os.BatteryManager;
import android.support.v4.content.LocalBroadcastManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.vmec.forkmonitor.data.model.Post;
import com.example.vmec.forkmonitor.data.remote.APIService;
import com.example.vmec.forkmonitor.data.remote.ApiUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class DrawActivity extends Activity {

    /*status==
    * 1-nalozeny
    * 2-vylozeny
    * 3-nabija
    * 4-servis
    * 5-necinnost
    *
    *
    * */

    private double mLat=0,mLng=0;
    private float mAcc=1000.0f;
    private int mBatt=0;

    private TextView latituteField;
    private TextView longitudeField;
    private TextView accuracyGPSField;
    private TextView logField;
    private TextView batteryField;


    private PixelGridView1 mPixelGrid;
    private TouchImageView1 mTouchImageView;

    private LinearLayout mInfoBar;

    private Button mExpandButton;
    private Button mLayoutButton;

    public boolean isLayoutVisible=true;

    private boolean mAlreadyStartedService = false;

    private Intent receiverBatt;

    private APIService mAPIService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image1);

        receiverBatt = this.registerReceiver(this.batteryLevelReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        mAPIService = ApiUtils.getAPIService();


        latituteField = (TextView) findViewById(R.id.TextView02);
        longitudeField = (TextView) findViewById(R.id.TextView04);
        accuracyGPSField = (TextView) findViewById(R.id.TextView06);
        logField = (TextView) findViewById(R.id.TextView08);
        batteryField = (TextView) findViewById(R.id.TextView10);

        mInfoBar = (LinearLayout) findViewById(R.id.InfoBar);

        mInfoBar.setVisibility(View.VISIBLE);

        mPixelGrid = (PixelGridView1) findViewById(R.id.pixel_grid_view);
        mTouchImageView = (TouchImageView1) findViewById(R.id.touchImageView);
        mTouchImageView.setMaxZoom(4f);


        mTouchImageView.setDrawView(mPixelGrid);
        mPixelGrid.setParentMatrix(mTouchImageView.getParentMatrix());
        mPixelGrid.setLayoutVisible(isLayoutVisible);
        mPixelGrid.invalidate();

        mLayoutButton = (Button) findViewById(R.id.button1);
        mLayoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View button) {
                expandClick();
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


        Button button1 = (Button) findViewById(R.id.button3);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View button) {
                Log.i("rest","vylozit");
                sendPost(android.os.Build.SERIAL,mLat,mLng,mBatt,mAcc,1);
            }
        });


        Button button2 = (Button) findViewById(R.id.button4);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View button) {
                Log.i("rest","nalozeny");
                sendPost(android.os.Build.SERIAL,mLat,mLng,mBatt,mAcc,2);
            }
        });

        Button button3 = (Button) findViewById(R.id.button5);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View button) {
                Log.i("rest","nabija");
                sendPost(android.os.Build.SERIAL,mLat,mLng,mBatt,mAcc,3);
            }
        });

        Button button4 = (Button) findViewById(R.id.button6);
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View button) {
                Log.i("rest","servis");
                sendPost(android.os.Build.SERIAL,mLat,mLng,mBatt,mAcc,4);
            }
        });

        Button button5 = (Button) findViewById(R.id.button7);
        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View button) {
                Log.i("rest","necinnost");
                sendPost(android.os.Build.SERIAL,mLat,mLng,mBatt,mAcc,5);
            }
        });




        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        mLat = intent.getDoubleExtra(LocationMonitoringService.EXTRA_LATITUDE,-1000);
                        mLng = intent.getDoubleExtra(LocationMonitoringService.EXTRA_LONGITUDE,-1000);
                        mAcc = intent.getFloatExtra(LocationMonitoringService.EXTRA_ACCURACY,-1000);
                        String log = intent.getStringExtra(LocationMonitoringService.EXTRA_LOG);
                        mBatt = intent.getIntExtra(LocationMonitoringService.EXTRA_BATTERY,-1000);
                        //if (latitude != null && longitude != null) {

                            Log.d("Foreground", "== location != null");
                        mPixelGrid.evaluateLocation(mLat,mLng,mAcc);//    latLngToXYPoint(homeLat,homeLng,lat,lng);
                        logField.setText(log);
                        latituteField.setText(String.valueOf(mLat));
                        longitudeField.setText(String.valueOf(mLng));
                        accuracyGPSField.setText(String.valueOf(mAcc));




                        //}
                    }
                }, new IntentFilter(LocationMonitoringService.ACTION_LOCATION_BROADCAST)
        );

        Log.d("Foreground", "starting service");
        Intent intent = new Intent(this, LocationMonitoringService.class);
        startService(intent);
        mAlreadyStartedService = true;
    }



    private void expandClick()  {
        isLayoutVisible = !isLayoutVisible;
        mPixelGrid.setLayoutVisible(isLayoutVisible);
        mPixelGrid.invalidate();
    }

    @Override
    public void onDestroy() {


        //Stop location sharing service to app server.........

        stopService(new Intent(this, LocationMonitoringService.class));
        mAlreadyStartedService = false;
        //Ends................................................


        super.onDestroy();
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
            int batt = level;

            boolean isCharging = false;

            final Intent batteryIntent = ctxt.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            int status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean batteryCharge = status==BatteryManager.BATTERY_STATUS_CHARGING;

            int chargePlug = batteryIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
            boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;

            if (batteryCharge) isCharging=true;
            if (usbCharge) isCharging=true;
            if (acCharge) isCharging=true;

            if (batt < 30){
                if (isCharging){
                    batteryField.setText(String.valueOf(batt));
                    batteryField.setTextColor(Color.BLACK);
                }
                else {
                    batteryField.setText(String.valueOf(batt) + " -- Zapni nabijanie!");
                    batteryField.setTextColor(Color.RED);
                }
            }
            if (batt >= 30 && batt <= 90){
                batteryField.setText(String.valueOf(batt));
                batteryField.setTextColor(Color.BLACK);
            }
            if (batt > 90){
                if (isCharging) {
                    batteryField.setText(String.valueOf(batt) + " -- Vypni nabijanie!");
                    batteryField.setTextColor(Color.RED);
                }
                else{
                    batteryField.setText(String.valueOf(batt));
                    batteryField.setTextColor(Color.BLACK);
                }
            }



        }
    };

    public void sendPost(String name, double lat,double lng, double battery, double accuracy, int status) {
        mAPIService.savePost(name, lat, lng,battery, accuracy,status).enqueue(new Callback<Post>() {
            @Override
            public void onResponse(Call<Post> call, Response<Post> response) {
                if(response.isSuccessful()) {
                    Log.d("rest", "success");

                }
            }

            @Override
            public void onFailure(Call<Post> call, Throwable t) {
                //Log.e("rest", "Unable to submit post to API.");
                Log.d("rest", "fail");

            }
        });
    }

}
