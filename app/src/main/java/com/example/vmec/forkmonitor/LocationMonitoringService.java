package com.example.vmec.forkmonitor;

import android.Manifest;
import android.app.Service;
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
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.vmec.forkmonitor.data.model.Post;
import com.example.vmec.forkmonitor.data.remote.APIService;
import com.example.vmec.forkmonitor.data.remote.ApiUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class LocationMonitoringService extends Service implements LocationListener {

    private static final String TAG = "background";
    private int mBatt = 0;


    public static final String ACTION_LOCATION_BROADCAST = LocationMonitoringService.class.getName() + "LocationBroadcast";
    public static final String EXTRA_LATITUDE = "extra_latitude";
    public static final String EXTRA_LONGITUDE = "extra_longitude";
    public static final String EXTRA_ACCURACY = "extra_accuracy";
    public static final String EXTRA_LOG = "extra_log";
    public static final String EXTRA_BATTERY = "extra_battery";


    private LocationManager locationManager;
    private String provider;

    private APIService mAPIService;

    private int counterS=0,counterF=0;

//BA SAMO
//    private final double homeLat =  48.151970;//49.164961
//    private final double homeLng =  17.109212;//17.503828


    //  Napajedla Fatra
       private double homeLat = 49.164962;
       private double homeLng = 17.503829;

    private final double CORNERLAT =  49.164962;//49.164961
    private final double CORNERLNG =  17.503829;//17.503828



    private final double MAPX = 458.2235;
    private final double MAPY = 1080.9670;

    private final double SCREENX = 1896;//1106;
    private final double SCREENY = 4500;//2625;

    private List<Polygon> mPolygons = new ArrayList<>();
    private Polygon mLastPolygonPosition;
    private Point lastPosition;
    private float mAcc = 1;
    private static float mAccTolerance = 12;
    private int samePosCount = 0;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //Log.d(TAG, "start inside background");
        this.registerReceiver(this.batteryLevelReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        mAPIService = ApiUtils.getAPIService();

        readKMLPolygons();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling

        }
        Location location = locationManager.getLastKnownLocation(provider);

        // Initialize the location fields
        if (location != null) {
            //Log.d(TAG, "Location available");
            //onLocationChanged(location);
        } else {
            //Log.d(TAG, "Location not available");
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        }
        locationManager.requestLocationUpdates(provider, 500, 0.5f, this);

        return START_STICKY;

    }

    @Override
    public void onDestroy(){
        unregisterReceiver(batteryLevelReceiver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }




    //to get the location change
    @Override
    public void onLocationChanged(Location location) {
        //Log.d(TAG, "Location changed");
        double lat = (location.getLatitude());
        double lng = (location.getLongitude());
        float acc = location.getAccuracy();
        String str = new String();



        //Log.i("serial","" +android.os.Build.SERIAL);




        int evaualtionCode = evaluateLocation(lat,lng,acc);//    latLngToXYPoint(homeLat,homeLng,lat,lng);

        if (evaualtionCode == 0)
        {

            sendPost(android.os.Build.SERIAL, lat, lng, mBatt, acc, -1);  //TODO bateriu posielat
            str = "Odoslane poloha. Presnost: " +acc+" S:"+String.valueOf(counterS)+"F:"+String.valueOf(counterF);

        }


        else if (evaualtionCode == -1) str ="Zla presnost: " + String.valueOf(acc) +" S:"+String.valueOf(counterS)+" F:"+String.valueOf(counterF);
        else if (evaualtionCode == -2) str ="Bod mimo mapy"+" S:"+String.valueOf(counterS)+" F:"+String.valueOf(counterF);
        else if (evaualtionCode == -3) str ="Bod mimo polygonov"+" S:"+String.valueOf(counterS)+" F:"+String.valueOf(counterF);


        else if (evaualtionCode > 0) str ="Rovnaka bunka: " + String.valueOf(evaualtionCode) + "x"+" S:"+String.valueOf(counterS)+" F:"+String.valueOf(counterF);

        if (location != null) {
            //Log.d(TAG, "== location != null");

            //Send result to activities
            sendMessageToUI(lat, lng, acc, str, mBatt);
            //Log.d("rest", str);
        }
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

    private void sendMessageToUI(double lat, double lng, float acc, String log, int batt) {

        //Log.d(TAG, "Sending info...");

        Intent intent = new Intent(ACTION_LOCATION_BROADCAST);
        intent.putExtra(EXTRA_LATITUDE, lat);
        intent.putExtra(EXTRA_LONGITUDE, lng);
        intent.putExtra(EXTRA_ACCURACY, acc);
        intent.putExtra(EXTRA_LOG, log);
        intent.putExtra(EXTRA_BATTERY, batt);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


    public void sendPost(String name, double lat,double lng, double battery, double accuracy, int status) {
        mAPIService.savePost(name, lat, lng,battery, accuracy,status).enqueue(new Callback<Post>() {
            @Override
            public void onResponse(Call<Post> call, Response<Post> response) {
                if(response.isSuccessful()) {
                    counterS +=1;
                    //Log.d("rest", "success");

                }
            }

            @Override
            public void onFailure(Call<Post> call, Throwable t) {
                //Log.e("rest", "Unable to submit post to API.");
                counterF +=1;
                //Log.d("rest", "fail");

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
            mBatt = level;
            //Log.d(TAG, "Battery Level" + mBatt);

        }
    };



















    public int evaluateLocation(double lat, double lng, float acc){


        Point currPoint = latLngToXYPoint(homeLat,homeLng,lat,lng);

        //setLastPosition(currPoint,acc);



        //Log.i("img","currX:" +currPoint.x + " currY: " + currPoint.y);


            if (acc <= mAccTolerance) {

                if ((currPoint.x >= 0 && currPoint.x < SCREENX) && (currPoint.y >= 0 && currPoint.y < SCREENY)) {
                    if(mPolygons.size()>0) {
                        for (Polygon polygon : mPolygons) {
                            if(polygon.containsPoint(currPoint)){
                                if(mLastPolygonPosition == null){
                                    polygon.setVisible(true);
                                    mLastPolygonPosition = polygon;

                                    return 0;
                                }
                                else {
                                    if (mLastPolygonPosition == polygon) {
                                        samePosCount += 1;
                                        return samePosCount;
                                    } else {
                                        samePosCount = 0;
                                        polygon.setVisible(true);
                                        mLastPolygonPosition.setVisible(false);
                                        mLastPolygonPosition = polygon;
                                        return samePosCount;
                                    }
                                }
                            }

                        }return -3;
                    }

                }else return -2;

            }else return -1;



        return samePosCount;
    }



    public double asRadians(double degrees) {
        return degrees * Math.PI / 180;
    }

    public Point latLngToXYPoint(double relativeLat,double relativeLng,double pLat, double pLng)
    {
        Point rotated = rotatePoint(pLat,pLng,relativeLat,relativeLng,-5.5);
        pLat = rotated.y;
        pLng = rotated.x;

        double deltaLatitude = pLat - relativeLat;
        double deltaLongitude = pLng - relativeLng;
        double latitudeCircumference = 40075160 * Math.cos(asRadians(relativeLat));
        double resultX = deltaLongitude * latitudeCircumference / 360;
        double resultY = deltaLatitude * 40008000 / 360;
        double resultDist = Math.sqrt((resultX * resultX) + (resultY * resultY));
        //Log.i("xml","resX: " + resultX + " resY: " +resultY  + "  resDist: " + resultDist);


        //double mapX = (resultX*(SCREENY/MAPY));
        double mapX = (resultX*(SCREENX/MAPX));
        double mapY = (SCREENY-resultY*(SCREENY/MAPY));


        //Log.i("xml","X: " + mapX + " Y: " +mapY);

        return new Point(mapX,mapY);



    }

    public static Point rotatePoint(double pointLat, double pointLon, double originLat,double originLon, double degree)
    {
        double x =  originLon   + (Math.cos(Math.toRadians(degree)) * (pointLon - originLon) - Math.sin(Math.toRadians(degree))  * (pointLat - originLat) / Math.abs(Math.cos(Math.toRadians(originLat))));
        double y = originLat + (Math.sin(Math.toRadians(degree)) * (pointLon - originLon) * Math.abs(Math.cos(Math.toRadians(originLat))) + Math.cos(Math.toRadians(degree))   * (pointLat - originLat));
        //Log.i("xml","rotated x:" + x + " y: " + y);
        return new Point(x, y);
    }

    public void readKMLPolygons() {


        try {
            InputStream is = getResources().openRawResource(R.raw.polygons5);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(is);

            Element element=doc.getDocumentElement();
            element.normalize();

            NodeList nList = doc.getElementsByTagName("coordinates");




            for (int i=0; i<nList.getLength(); i++) {

                String allCoords = nList.item(i).getTextContent().trim();
                List<String> coords = new ArrayList<>();
                int last = 0;

                for (int index = allCoords.indexOf(" "); index >= 0; index = allCoords.indexOf(" ", index + 1)){

                    coords.add(allCoords.substring(last,index));

                    //Log.i("xml","index "+ index +" --coords " + allCoords.substring(last,index));
                    last=index;
                }
                coords.add(allCoords.substring(last,allCoords.length()));
                //Log.i("xml","index "+ allCoords.length() +" --coords " + allCoords.substring(last,allCoords.length()));
                //Log.i("xml","**********************");


                List<Point> polygon = new ArrayList<>();
                for (String coord :coords){
                    int index = coord.indexOf(",");
                    double lng = Double.parseDouble(coord.substring(0,index));
                    //Log.i("xml","Lng " + lng);

                    int index1 = coord.indexOf(",",index+1);
                    double lat = Double.parseDouble(coord.substring(index+1,index1));
                    //Log.i("xml","Lat " + lat);

                    //Log.i("xml","---------------------------");


                    polygon.add(latLngToXYPoint(CORNERLAT,CORNERLNG,lat,lng));
                }



                mPolygons.add(new Polygon("polygon",polygon));

            }

        }
        catch (Exception e) {e.printStackTrace();}
    }
}