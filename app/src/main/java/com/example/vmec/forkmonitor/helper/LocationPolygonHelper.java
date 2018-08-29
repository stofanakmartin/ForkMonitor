package com.example.vmec.forkmonitor.helper;

import android.content.Context;
import android.location.Location;

import com.example.vmec.forkmonitor.Constants;
import com.example.vmec.forkmonitor.Point;
import com.example.vmec.forkmonitor.Polygon;
import com.example.vmec.forkmonitor.R;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import timber.log.Timber;

/**
 * Created by Stofanak on 28/08/2018.
 */
public class LocationPolygonHelper {

    // BA SAMO
//    private final double homeLat =  48.151970;//49.164961
//    private final double homeLng =  17.109212;//17.503828
    // Napajedla Fatra
//    private double homeLat = 49.164962;
//    private double homeLng = 17.503829;
    private double homeLat = 48.1478244;
    private double homeLng = 17.0606898;

//    private static final double CORNER_LAT =  49.164962;//49.164961
//    private static final double CORNER_LNG =  17.503829;//17.503828
//    48.1478244,17.0606898
    private static final double CORNER_LAT =  48.1478244;//49.164961
    private static final double CORNER_LNG =  17.0606898;//17.503828
    private static final double MAP_X = 458.2235;
    private static final double MAP_Y = 1080.9670;
    private static final double SCREEN_X = 1896;//1106;
    private static final double SCREEN_Y = 4500;//2625;

    private List<Polygon> mPolygons;
    private Polygon mLastPolygonPosition;
    private int mSamePositionCounter = 0;

    public LocationPolygonHelper(final Context context) {
        mPolygons = readKMLPolygons(context);
    }

    private List<Polygon> readKMLPolygons(final Context context) {
        Timber.d("Read polygons data");
        final List<Polygon> polygons = new LinkedList<>();
        try {
            InputStream is = context.getResources().openRawResource(R.raw.polygons5);
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

                    polygon.add(latLngToXYPoint(CORNER_LAT, CORNER_LNG,lat,lng));
                }

                polygons.add(new Polygon("polygon",polygon));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        Timber.d("Read %d polygons successfully", polygons.size());

        return polygons;
    }

    private Point latLngToXYPoint(double relativeLat,double relativeLng,double pLat, double pLng) {
//        Point rotated = rotatePoint(pLat,pLng,relativeLat,relativeLng,-5.5);
        Point rotated = rotatePoint(pLat,pLng,relativeLat,relativeLng,-0.1);
        pLat = rotated.y;
        pLng = rotated.x;

        double deltaLatitude = pLat - relativeLat;
        double deltaLongitude = pLng - relativeLng;
        double latitudeCircumference = 40075160 * Math.cos(asRadians(relativeLat));
        double resultY = deltaLongitude * latitudeCircumference / 360;
        double resultX = deltaLatitude * 40008000 / 360;
        double resultDist = Math.sqrt((resultX * resultX) + (resultY * resultY));

        Timber.d("latLngToXYPoint X: %f Y: %f", resultX, resultY);

        double mapX = (resultX * (SCREEN_X / MAP_X));
        double mapY = (SCREEN_Y - resultY * (SCREEN_Y / MAP_Y));

        Timber.d("latLngToXYPoint MAP X: %f Y: %f", mapX, mapY);

        return new Point(mapX,mapY);
    }

    private static Point rotatePoint(double pointLat, double pointLon, double originLat,double originLon, double degree) {
        double x =  originLon   + (Math.cos(Math.toRadians(degree)) * (pointLon - originLon) - Math.sin(Math.toRadians(degree))  * (pointLat - originLat) / Math.abs(Math.cos(Math.toRadians(originLat))));
        double y = originLat + (Math.sin(Math.toRadians(degree)) * (pointLon - originLon) * Math.abs(Math.cos(Math.toRadians(originLat))) + Math.cos(Math.toRadians(degree))   * (pointLat - originLat));
        return new Point(x, y);
    }

    private double asRadians(double degrees) {
        return degrees * Math.PI / 180;
    }

    public int checkLocationStatus(Location location) {
        double lat = (location.getLatitude());
        double lng = (location.getLongitude());
        float acc = location.getAccuracy();

        final int evaluationCode = evaluateLocationInPolygons(lat,lng,acc);//    latLngToXYPoint(homeLat,homeLng,lat,lng);

        if (evaluationCode == 0) {
            Timber.d("Location polygon change");
//            sendPost(android.os.Build.SERIAL, lat, lng, mBatt, acc, -1);  //TODO bateriu posielat
//            str = "Odoslane poloha. Presnost: " +acc+" S:"+String.valueOf(counterS)+"F:"+String.valueOf(counterF);

        } else if (evaluationCode == -1) {
            Timber.d("Location not accurate %f", acc);
//            str ="Zla presnost: " + String.valueOf(acc) +" S:"+String.valueOf(counterS)+" F:"+String.valueOf(counterF);
        } else if (evaluationCode == -2) {
            Timber.d("Location coordinates are out of map");
//            str = "Bod mimo mapy" + " S:" + String.valueOf(counterS) + " F:" + String.valueOf(counterF);
        } else if (evaluationCode == -3) {
            Timber.d("Bod mimo polygonov");
//            str ="Bod mimo polygonov"+" S:"+String.valueOf(counterS)+" F:"+String.valueOf(counterF);
        } else if (evaluationCode > 0) {
            Timber.d("Rovnaka bunka - code %d", evaluationCode);
//            str ="Rovnaka bunka: " + String.valueOf(evaluationCode) + "x"+" S:"+String.valueOf(counterS)+" F:"+String.valueOf(counterF);
        }

        return evaluationCode;
    }

    private int evaluateLocationInPolygons(double lat, double lng, float acc){

        Point currPoint = latLngToXYPoint(homeLat,homeLng,lat,lng);

        //setLastPosition(currPoint,acc);

        //Log.i("img","currX:" +currPoint.x + " currY: " + currPoint.y);


        if (acc <= Constants.LOCATION_ACCURACY_TOLERANCE) {

            if ((currPoint.x >= 0 && currPoint.x < SCREEN_X) && (currPoint.y >= 0 && currPoint.y < SCREEN_Y)) {
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
                                    mSamePositionCounter += 1;
                                    return mSamePositionCounter;
                                } else {
                                    mSamePositionCounter = 0;
                                    polygon.setVisible(true);
                                    mLastPolygonPosition.setVisible(false);
                                    mLastPolygonPosition = polygon;
                                    return mSamePositionCounter;
                                }
                            }
                        }
                    }
                    return -3;
                }

            } else {
                return -2;
            }
        } else {
            return -1;
        }

        return mSamePositionCounter;
    }
}
