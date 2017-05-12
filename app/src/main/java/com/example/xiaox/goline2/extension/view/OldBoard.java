package com.example.xiaox.goline2.extension.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.Point;
import android.support.design.widget.Snackbar;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

import com.example.xiaox.goline2.extension.helper.DrawPoint;
import com.example.xiaox.goline2.extension.helper.Logger;
import com.example.xiaox.goline2.extension.helper.MarkEx;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiaox on 1/20/2017.
 */
public class OldBoard extends View {
    private float pathStartx = 0f;
    private float pathStarty = 0f;
    private float starty = 0f;
    private float startx = 0f;
    private float endx = 0f;
    private float endy = 0f;
    private int maxCoor = 1;
    private Paint paint;
    private DrawPoint[] points;
    private DrawPoint[] drawingPoints;
    private Point[][] coorLives;
    private Path drawingPath;
    private int radius = 60;
    private ArrayList<Integer> colorList;
    private List<ArrayList<Point>> pointsList;
    private PaintFlagsDrawFilter pfd;
    public float lineWidth = 10f;
    public float latticeLength = 100f;
    public int lineFill = Color.parseColor("#87CEEB");
    private  boolean isDrawNeed = true;

    public OldBoard(Context context){
        super(context);
        init();
    }

    public OldBoard(Context context, AttributeSet attrs){
        this(context);
    }

    public OldBoard(Context context, AttributeSet attrs, int defStyle){
        this(context);
    }

    private void init(){
        colorList = new ArrayList<>();
        pointsList = new ArrayList<>();
        pfd = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

        this.paint = new Paint();
        this.paint.setStyle(Paint.Style.STROKE);
        this.paint.setStrokeJoin(Paint.Join.ROUND);
        this.paint.setStrokeCap(Paint.Cap.ROUND);
        this.paint.setColor(lineFill);
        this.paint.setStrokeWidth(lineWidth);
        StringBuilder builder = new StringBuilder();
        builder.append("M2,1 H4 M0,3 H2 M4,3 H6 M2,5 H4 M3,0 H0 V6 H3");
        builder.append(" M3,4 V6 H6 V0 H3 L5,2 V4 L3,6 L1,4 L3,2 V0 L1,2 L3,4 L5,2 H1 V4 H5 L3,2");
        this.setDrawPoints(builder.toString());

        ViewTreeObserver vto = this.getViewTreeObserver();
        final OldBoard board = this;
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                board.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int width = board.getWidth();
                int height = board.getHeight();
                board.updateAdaptLength(height, width);
                board.invalidate();
            }
        });
    }

    public void setLineFill(int color){
        this.lineFill = color;
        this.paint.setColor(color);
    }
    public int getLineFill(){
        return this.lineFill;
    }

    public void setLineWidth(float width){
        this.lineWidth = width;
        this.paint.setStrokeWidth(width);
    }
    public float getLineWidth(){
        return this.lineWidth;
    }

    public void updateAdaptLength(int height, int width){
        float adaptLength = 0f;
        //Update Drawing rectangle
        if(width > height){
            adaptLength = height;
            this.startx = (width - height) / 2;
            this.starty = 0f;
            this.pathStartx = startx + this.radius;
            this.pathStarty = this.radius;
        }else{
            adaptLength = width;
            this.startx = 0f;
            this.starty = (height - width) / 2;
            this.pathStartx =  this.radius;
            this.pathStarty = starty + this.radius;
        }
        this.endx = startx + adaptLength;
        this.endy = starty + adaptLength;
        float innerAdaptLength = adaptLength - this.radius * 2;
        this.latticeLength = innerAdaptLength / this.maxCoor;
        for(int i = 0; i < this.drawingPoints.length; i++){
            this.drawingPoints[i].x = this.points[i].x * latticeLength + pathStartx;
            this.drawingPoints[i].y = this.points[i].y * latticeLength + pathStarty;
        }
        Logger.logLine("innerAdaptLength: " + innerAdaptLength + ", this.maxCoor: " + this.maxCoor);
        this.drawingPath = createPath(this.drawingPoints);
    }
    public float getLatticeLengthLength(){return this.latticeLength;}

    //Not support
    private void setDrawPoints(DrawPoint[] points){
        this.points = new DrawPoint[points.length];
        for(int i = 0; i < this.points.length; i++){
            this.points[i] = points[i];
            this.drawingPoints[i] = this.points[i];
            this.drawingPoints[i].x = (this.drawingPoints[i].x * this.latticeLength + pathStartx);
            this.drawingPoints[i].y = (this.drawingPoints[i].y *this.latticeLength + pathStarty);
        }
        this.points = points;
        this.drawingPath = createPath(this.drawingPoints);
    }
    public void setDrawPoints(String markExString){
        List<DrawPoint> pointList = DrawPoint.parseDrawPoints(markExString);
        this.points = new DrawPoint[pointList.size()];
        this.drawingPoints = new DrawPoint[pointList.size()];
        this.maxCoor = 0;
        for(int i = 0; i < this.points.length; i++){
            this.points[i] = pointList.get(i);
            int max = (int)Math.max(this.points[i].x, this.points[i].y);
            this.drawingPoints[i] = this.points[i].clone();
            this.drawingPoints[i].x = (this.drawingPoints[i].x * this.latticeLength + pathStartx);
            this.drawingPoints[i].y = (this.drawingPoints[i].y * this.latticeLength + pathStarty);
            if(max > this.maxCoor) this.maxCoor = max;
        }
        this.coorLives = new Point[this.maxCoor + 1][this.maxCoor + 1];
        this.drawingPath = createPath(this.drawingPoints);
    }

    private Path createPath(DrawPoint[] points){
        Path path = new Path();
        if(points == null) return path;
        for(int i = 0; i < points.length; ++i){
            DrawPoint point = points[i];
            if(point.mark == MarkEx.M){
                path.moveTo(point.x, point.y);
            }else{
                path.lineTo(point.x, point.y);
            }
        }
        return path;
    }

    public boolean addCircle(int column, int row,int fillColor){
        //Validate column and row
        if(column > coorLives.length || row > coorLives[0].length) return false;
        if(coorLives[column][row] != null) return false;
        //Calculate  x and y coordinate
        double colTarget = column * latticeLength + pathStartx;
        double rowTarget = row * latticeLength + pathStarty;
        Point p = new Point((int)(colTarget), (int)(rowTarget));
        //Add to point collection
        coorLives[column][row] = p;
        this.addCircleInPoint(p, fillColor);
        //Raise view update
        this.invalidate();
        return true;
    }
    private void addCircleInPoint(Point point, int color){
        int index = colorList.indexOf(color);
        ArrayList<Point> points = null;
        if(index < 0){
            points = new ArrayList<Point>();
            points.add(point);
            colorList.add(color);
            pointsList.add(points);
        }else{
            points = pointsList.get(index);
            points.add(point);
        }
        Snackbar.make(this, "New color of " + color + " " + points.size() , Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        if(event.getAction() == MotionEvent.ACTION_UP) {
            int x = (int) event.getX();
            int y = (int) event.getY();
            int[] colrow = this.tryGetColumnRow(x, y);
        }
        return true;
    }

    /**
     * Try to calculate column row value for special coordinate
     * Return int[]{column, row} if successful
     */
    private int[] tryGetColumnRow(int x, int y){
        //Validate x and y coordinate is in inner board
        if (x >= startx && x <= endx && y >= starty && y <= endy) {
            //Calculate relative coordinate and column/row
            float xRelative = x - pathStartx;
            float yRelative = y - pathStarty;
            int column = (int)Math.rint(xRelative / latticeLength);
            int row = (int)Math.rint(yRelative / latticeLength);
            double colTarget = column * latticeLength;
            double rowTarget = row * latticeLength;
            //Validate x and y relative coordinate is in circle with special radius
            if(Math.abs(xRelative - colTarget) < this.radius) {
                if (Math.abs(yRelative - rowTarget) < this.radius) {
                    return new int[]{column, row };
                }
            }
        }
        return null;
    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        if(isDrawNeed == false )return;
        if(drawingPath == null)drawingPath = createPath(this.drawingPoints);
        canvas.setDrawFilter(pfd);
        //canvas.drawColor(Color.WHITE);
        //Draw path
        this.paint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(drawingPath, paint);
        //Draw circle
        Paint strokepaint = new Paint();
        Paint circlepaint = new Paint();
        circlepaint.setStyle(Paint.Style.FILL);
        strokepaint.setStyle(Paint.Style.STROKE);
        strokepaint.setColor(Color.WHITE);
        strokepaint.setStrokeWidth(8f);
        float radiusStroke = radius + 2;
        for(int i = 0; i < colorList.size(); i++){
            circlepaint.setColor(colorList.get(i));
            ArrayList<Point> points = pointsList.get(i);
            for(int j = 0; j < points.size(); j++){
                Point p = points.get(j);
                canvas.drawCircle(p.x, p.y, radius, circlepaint);
                canvas.drawCircle(p.x, p.y, radiusStroke, strokepaint);
            }
        }
    }


}
