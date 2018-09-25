package com.forkmonitor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

public class BitmapGridView extends ImageView {

    private int numColumns =36, numRows = 57 ;
    //private int numColumns =18, numRows = 27 ;

    private int cellWidth, cellHeight;
    private Paint blackPaint = new Paint();
    private Paint greenPaint = new Paint();
    private Paint greenPaint1 = new Paint();
    private Paint redPaint = new Paint();
    private int lastgY = 0;
    private int lastgX = 0;
    private int sameCount = 0;

    private int[][] cellChecked;
    private Canvas mCanvas;
    private float cX=1f,cY=1f;


    private Bitmap mBitmap;

    private boolean first = true;


    public BitmapGridView(Context context) {
        this(context, null);
    }

    public BitmapGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
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
    }

    public int drawCircleGrid(float x, float y, float acc){



        if(mCanvas != null) {
            invalidate();
            Log.i("IMG","Height" + getHeight());
            Log.i("IMG","Width" + getWidth());
            Log.i("IMG","Height" + cY);
            Log.i("IMG","Width" + cX);

            cX = x*3f;
            cY = 1140-y*3.45f;

            if (acc <= 10) {
                int gX = (int) cX / (getWidth() / numColumns);
                int gY = (int) cY / (getHeight() / numRows);


                if ((lastgX == gX) && (lastgY == gY)) {
                    sameCount += 1;


                } else {
                    sameCount = 0;
                    cellChecked[gX][gY] = 2;
                    cellChecked[lastgX][lastgY] = 1;
                    lastgX = gX;
                    lastgY = gY;

                }
            }
        }
        else Log.i("IMG","Null Canvas");
        return sameCount;
    }

    public void setNumColumns(int numColumns) {
        this.numColumns = numColumns;
        calculateDimensions();
    }

    public int getNumColumns() {
        return numColumns;
    }

    public void setNumRows(int numRows) {
        this.numRows = numRows;
        calculateDimensions();
    }

    public int getNumRows() {
        return numRows;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        calculateDimensions();
    }

    private void calculateDimensions() {
        if (numColumns < 1 || numRows < 1) {
            return;
        }

        cellWidth = getWidth() / numColumns;
        cellHeight = getHeight() / numRows;

        cellChecked = new int[numColumns][numRows];

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT);
        mCanvas = canvas;

        int vWidth = getWidth();
        int vHeight = getHeight();
        int halfWidth = vWidth / 2;
        int halfHeight = vHeight / 2;




        if (numColumns == 0 || numRows == 0) {
            return;
        }

        int width = getWidth();
        int height = getHeight();

        for (int i = 0; i < numColumns; i++) {
            for (int j = 0; j < numRows; j++) {
                if (cellChecked[i][j]==2) {

                    canvas.drawRect(i * cellWidth, j * cellHeight,
                            (i + 1) * cellWidth, (j + 1) * cellHeight,
                            redPaint);
                }
                if (cellChecked[i][j]==1) {

                    canvas.drawRect(i * cellWidth, j * cellHeight,
                            (i + 1) * cellWidth, (j + 1) * cellHeight,
                            greenPaint1);
                }
            }
        }

        for (int i = 1; i < numColumns; i++) {
            canvas.drawLine(i * cellWidth, 0, i * cellWidth, height, blackPaint);
        }

        for (int i = 1; i < numRows; i++) {
            canvas.drawLine(0, i * cellHeight, width, i * cellHeight, blackPaint);
        }

        canvas.drawCircle(cX,cY,5,greenPaint);


    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int column = (int)(event.getX() / cellWidth);
            int row = (int)(event.getY() / cellHeight);

            cellChecked[column][row] = 0;
            invalidate();
        }

        return true;
    }
}