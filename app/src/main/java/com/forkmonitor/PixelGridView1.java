package com.forkmonitor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

//TODO osetrit telefon bez poslednej polohy pre locatoin manager



public class PixelGridView1 extends AppCompatImageView {

    //private int numColumns =36, numRows = 57 ;
    private int numColumns = 79, numRows = 175 ;
    //private int numColumns = 11, numRows = 11 ;

    private int cellWidth, cellHeight;
    private Paint blackPaint = new Paint();
    private Paint greenPaint = new Paint();
    private Paint greenPaint1 = new Paint();
    private Paint redPaint = new Paint();
    private Paint erasePaint = new Paint();
    private Paint positionPaint1 = new Paint();
    private Paint positionPaint2 = new Paint();
    private Paint positionPaint3 = new Paint();
    private int lastgY = 0;
    private int lastgX = 0;
    private int samePosCount = 0;
    private int mCount=0;

    private int[][] cellChecked;
    private Canvas mCanvas;
    private float cX=1f,cY=1f;

    private Path drawPath;
    private Paint drawPaint;
    private Paint canvasPaint;
    private Canvas drawCanvas;
    private Bitmap canvasBitmap;
    private int paintColor = Color.RED;

    private Matrix parentMatrix;

    private boolean first=true;



// BA SAMO
//    private final double homeLat =  48.151970;//49.164961
//    private final double homeLng =  17.109212;//17.503828
    private final double CORNERLAT =  49.164962;//49.164961
    private final double CORNERLNG =  17.503829;//17.503828


     // Napajedla Fatra
    private double homeLat = 49.164962;
    private double homeLng = 17.503829;



    private final double MAPX = 458.2235;
    private final double MAPY = 1080.9670;

    private final double SCREENX = 1896;//1106;
    private final double SCREENY = 4500;//2625;
//GBV7T17091753

//GBV7T17091762

    private List<Polygon> mPolygons = new ArrayList<>();

    private Polygon mLastPolygonPosition;

    private boolean mLayoutVisible;
    private Point lastPosition;
    private float mAcc = 1;
    private static float mAccTolerance = 12;

    public PixelGridView1(Context context) {
        this(context, null);
    }

    public PixelGridView1(Context context, AttributeSet attrs) {
        super(context, attrs);

        setupDrawing();
        this.canvasPaint = new Paint(Paint.DITHER_FLAG);


        canvasBitmap = Bitmap.createBitmap((int)SCREENX,(int) SCREENY, Bitmap.Config.ARGB_4444);
        drawCanvas = new Canvas(canvasBitmap);


        parentMatrix = new Matrix();
        calculateDimensionsCanvas(drawCanvas);


        readKMLPolygons();

        invalidate();


    }

    public int evaluateLocation(double lat, double lng, float acc){


        Point currPoint = latLngToXYPoint(homeLat,homeLng,lat,lng);

        setLastPosition(currPoint,acc);
        invalidate();


        //Log.i("img","currX:" +currPoint.x + " currY: " + currPoint.y);

        if(drawCanvas != null) {
            if (acc <= mAccTolerance) {

                if ((currPoint.x >= 0 && currPoint.x < drawCanvas.getWidth()) && (currPoint.y >= 0 && currPoint.y < drawCanvas.getHeight())) {
                    if(mPolygons.size()>0) {
                        for (Polygon polygon : mPolygons) {
                            if(polygon.containsPoint(currPoint)){
                                if(mLastPolygonPosition == null){
                                    polygon.setVisible(true);
                                    mLastPolygonPosition = polygon;
                                    invalidate();
                                    return 0;
                                }
                                else {
                                    if (mLastPolygonPosition == polygon) {
                                        samePosCount += 1;
                                        invalidate();
                                        return samePosCount;
                                    } else {
                                        samePosCount = 0;
                                        polygon.setVisible(true);
                                        mLastPolygonPosition.setVisible(false);
                                        mLastPolygonPosition = polygon;
                                        invalidate();
                                        return samePosCount;
                                    }
                                }
                            }

                        }return -3;
                    }

                }else return -2;

            }else return -1;

        }else return -100;

        return samePosCount;
    }



    public void setParentMatrix(Matrix matrix){
        this.parentMatrix =matrix;

    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        calculateDimensionsCanvas(drawCanvas);
        //Log.i("img","resizing now!");

    }



    private void setupDrawing() {
        positionPaint1.setAntiAlias(true);
        positionPaint1.setStyle(Paint.Style.FILL_AND_STROKE);
        positionPaint1.setARGB(255,68,117,215);

        positionPaint2.setAntiAlias(true);
        positionPaint2.setStyle(Paint.Style.FILL_AND_STROKE);
        positionPaint2.setARGB(70,68,117,215);

        positionPaint3.setAntiAlias(true);
        positionPaint3.setStrokeWidth(3);
        positionPaint3.setStyle(Paint.Style.STROKE);
        positionPaint3.setARGB(255,68,117,215);
/*
        erasePaint.setColor(0x00000000);
        erasePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        erasePaint.setAlpha(0x00);
        erasePaint.setAntiAlias(true);
        erasePaint.setDither(true);
        erasePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        erasePaint.setStrokeJoin(Paint.Join.ROUND);
        erasePaint.setStrokeCap(Paint.Cap.ROUND);
*/
        blackPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        blackPaint.setColor(Color.BLACK);
        greenPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        greenPaint.setColor(Color.GREEN);
        greenPaint1.setStyle(Paint.Style.FILL_AND_STROKE);
        greenPaint1.setARGB(70,0,255,0);

        redPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        redPaint.setARGB(127,255,0,0);
        //redPaint.setAlpha(127);
        this.setBackgroundColor(Color.TRANSPARENT);
        //this.setZOrderOnTop(true); //necessary

        drawPaint = new Paint();
        drawPaint.setColor(paintColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setDither(true);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        drawPaint.setStrokeWidth(10);

    }
    @Override
    protected void onDraw(Canvas canvas) {
        eraseLayout();

        if(first) {

            //canvas.drawColor(Color.TRANSPARENT);
            first=false;
        }

        if (isLayoutVisible()){
            drawLayout();
        }

        if(mPolygons.size()>0){
            for (Polygon polygon : mPolygons) {
                if (polygon.isVisible()) {
                    drawPolygon(drawCanvas,redPaint,polygon);
                }
            }
        }
        drawPosition();
        canvas.drawBitmap(canvasBitmap, parentMatrix, null);

    }

    public void setLayoutVisible(boolean visible){
        this.mLayoutVisible = visible;
    }

    public boolean isLayoutVisible(){
        return mLayoutVisible;
    }

    private void setLastPosition(Point position,float acc){
        this.lastPosition = position;
        this.mAcc=acc;
    }

    private void calculateDimensionsCanvas(Canvas canvas) {
        if (numColumns < 1 || numRows < 1) {
            return;
        }

        cellWidth = canvas.getWidth() / numColumns;
        cellHeight = canvas.getHeight() / numRows;

        cellChecked = new int[numColumns][numRows];

        invalidate();
    }





    private void drawPolygon(Canvas canvas, Paint polyPaint, Polygon polygon) {

        // line at minimum...

        List<Point> points = polygon.getPoints();
        if (points.size() < 3) {
            return;
        }


        Path polyPath = new Path();
        polyPath.moveTo((float)points.get(0).x,(float) points.get(0).y);
        int i, len;
        len = points.size();
        for (i = 0; i < len; i++) {
            polyPath.lineTo((float)points.get(i).x,(float) points.get(i).y);
        }
        //polyPath.lineTo(points[0].x, points[0].y);

        // draw
        canvas.drawPath(polyPath, polyPaint);
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

          //          Log.i("xml","index "+ index +" --coords " + allCoords.substring(last,index));
                    last=index;
                }
                coords.add(allCoords.substring(last,allCoords.length()));
              //  Log.i("xml","index "+ allCoords.length() +" --coords " + allCoords.substring(last,allCoords.length()));
            //    Log.i("xml","**********************");


                List<Point> polygon = new ArrayList<>();
                for (String coord :coords){
                    int index = coord.indexOf(",");
                    double lng = Double.parseDouble(coord.substring(0,index));
                //    Log.i("xml","Lng " + lng);

                    int index1 = coord.indexOf(",",index+1);
                    double lat = Double.parseDouble(coord.substring(index+1,index1));
                  //  Log.i("xml","Lat " + lat);

                    //Log.i("xml","---------------------------");


                    polygon.add(latLngToXYPoint(CORNERLAT,CORNERLNG,lat,lng));
                }



                mPolygons.add(new Polygon("polygon",polygon));
                mCount+=1;
             //   Log.i("cxml","--" + mCount);
            }

        }
        catch (Exception e) {e.printStackTrace();}
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
        //Log.i("lxml","resX: " + resultX + " resY: " +resultY  + "  resDist: " + resultDist);


        //double mapX = (resultX*(SCREENY/MAPY));
        double mapX = (resultX*(SCREENX/MAPX));
        double mapY = (SCREENY-resultY*(SCREENY/MAPY));


        //Log.i("lxml","X: " + mapX + " Y: " +mapY);

        return new Point(mapX,mapY);



    }

    public static Point rotatePoint(double pointLat, double pointLon, double originLat,double originLon, double degree)
    {
        double x =  originLon   + (Math.cos(Math.toRadians(degree)) * (pointLon - originLon) - Math.sin(Math.toRadians(degree))  * (pointLat - originLat) / Math.abs(Math.cos(Math.toRadians(originLat))));
        double y = originLat + (Math.sin(Math.toRadians(degree)) * (pointLon - originLon) * Math.abs(Math.cos(Math.toRadians(originLat))) + Math.cos(Math.toRadians(degree))   * (pointLat - originLat));
        //Log.i("lxml","rotated x:" + x + " y: " + y);
        return new Point(x, y);
    }

    public void drawLayout(){
        Paint randPaint = new Paint();
        randPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        randPaint.setAntiAlias(true);

        if (mPolygons.size() > 0) {

            for (Polygon polygon:mPolygons) {
                if(!polygon.isVisible()) {
                    randPaint.setARGB(100, polygon.r, polygon.g, polygon.b);
                    drawPolygon(drawCanvas, randPaint, polygon);
                }

            }
        }
    }

    public void eraseLayout(){
        canvasBitmap.eraseColor(Color.TRANSPARENT);
        //invalidate();
    }

    private void drawPosition(){
        //TODO ak je mensia presnost alebo vacsi zoom zmenit proporcie ukazovatela
        float transformedAcc = (float) (mAcc * (((SCREENX / MAPX) + (SCREENY / MAPY)) / 2));
        if(mAcc<mAccTolerance) {
            positionPaint2.setARGB(70,68,117,215);
            positionPaint3.setARGB(255,68,117,215);
        }else{
            positionPaint2.setARGB(70,215,40,40);
            positionPaint3.setARGB(255,215,40,40);
        }
        if(lastPosition != null) {
            drawCanvas.drawCircle((float) lastPosition.x, (float) lastPosition.y, 5, positionPaint1);
            drawCanvas.drawCircle((float) lastPosition.x, (float) lastPosition.y, transformedAcc, positionPaint2);
            drawCanvas.drawCircle((float) lastPosition.x, (float) lastPosition.y, transformedAcc, positionPaint3);
        }
    }
}

