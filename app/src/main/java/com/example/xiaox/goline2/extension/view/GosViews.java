package com.example.xiaox.goline2.extension.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.example.xiaox.goline2.R;
import com.example.xiaox.goline2.common.ColorEx;
import com.example.xiaox.goline2.extension.helper.ActionEx;
import com.example.xiaox.goline2.extension.helper.Bound;
import com.example.xiaox.goline2.extension.helper.DensityUtil;
import com.example.xiaox.goline2.extension.helper.DrawPoint;
import com.example.xiaox.goline2.extension.helper.IAction;
import com.example.xiaox.goline2.extension.helper.Logger;
import com.example.xiaox.goline2.extension.helper.MarkEx;
import com.example.xiaox.goline2.service.SFXService;

import java.util.List;

public class GosViews extends ViewGroup implements core.interfaces.IBoard {
    public GosViews(Context context){
        super(context);
        this.setWillNotDraw(false);
        init();
    }
    public GosViews(Context context, AttributeSet attrs){
        super(context, attrs);
        this.setWillNotDraw(false);
        init();
    }
    public GosViews(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
        this.setWillNotDraw(false);
        init();
    }

    private Bound boardBound;
    private Bound pathBound;
    private int maxCoorLength = 1;
    private DrawPoint[] originPoints;
    private DrawPoint[] drawingPoints;
    private Path drawingPath;
    private PaintFlagsDrawFilter pfd;
    public float pathLineWidth = 10f;
    public int pathStrokeFill = ColorEx.SKYBLUE;
    private int columnCount = 10;
    private int rowCount = 10;
    private int radius;
    private int padding;
    private float latticeLength = 100;
    private Paint pathPaint = null;
    private View[][] internalViews;
    private Circle newestTipView;
    //For adapt density
    private int radiusDP = 20;
    private int paddingDP = 16;
    private void init(){
        internalViews = new View[columnCount][rowCount];
        this.radius = DensityUtil.dip2px(this.getContext(), this.radiusDP);
        this.padding = DensityUtil.dip2px(this.getContext(), this.paddingDP);
        System.out.println("DensityUtil.px2dip " + DensityUtil.px2dip(this.getContext(), 100));
        this.pathBound = new Bound(0);
        this.boardBound = new Bound(0);
        this.pathPaint = new Paint();
        this.pathPaint.setStyle(Paint.Style.STROKE);
        this.pathPaint.setStrokeJoin(Paint.Join.ROUND);
        this.pathPaint.setStrokeCap(Paint.Cap.ROUND);
        this.pathPaint.setColor(pathStrokeFill);
        this.pathPaint.setStrokeWidth(pathLineWidth);
        StringBuilder builder = new StringBuilder();
        builder.append("M2,1 H4 M0,3 H2 M4,3 H6 M2,5 H4 M3,0 H0 V6 H3");
        builder.append(" M3,4 V6 H6 V0 H3 L5,2 V4 L3,6 L1,4 L3,2 V0 L1,2 L3,4 L5,2 H1 V4 H5 L3,2");
        this.setDrawPoints(builder.toString());

        final GosViews self = this;
        ViewTreeObserver vto = this.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                self.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int width = self.getWidth();
                int height = self.getHeight();
                self.updateAdaptLength(height, width);
                self.invalidate();
            }
        });
        this.bringToFront();
    }

    public void setColumnCount(int columnCount){
        this.columnCount = columnCount;
    }
    public void setRowCount(int rowCount){
        this.rowCount = rowCount;
    }

    public void setLineFill(int color){
        this.pathStrokeFill = color;
        this.pathPaint.setColor(color);
        this.invalidate();
    }
    public int getLineFill(){
        return this.pathStrokeFill;
    }

    public void setLineWidth(float width){
        this.pathLineWidth = width;
        this.pathPaint.setStrokeWidth(width);
        this.invalidate();
    }
    public float getLineWidth(){
        return this.pathLineWidth;
    }

    public void updateAdaptLength(int height, int width){
        Logger.logLine("updateAdaptLength -> " + height + ", " + width);
        int adaptLength = 0;
        //Update Drawing rectangle
        int left = 0, right = 0;
        if(width > height){
            adaptLength = height;
            left = (width - height) / 2;
        }else{
            adaptLength = width;
            right = (height - width) / 2;
        }
        //Update bound
        this.boardBound.updateBy(left, right, adaptLength, adaptLength);
        this.pathBound = this.boardBound.recreateWithPadding(this.radius + this.padding);
        //Update points
        float pathAdaptLength = adaptLength - this.radius * 2 - this.padding * 2;
        this.latticeLength = pathAdaptLength / this.maxCoorLength;
        for(int i = 0; i < this.drawingPoints.length; i++){
            this.drawingPoints[i].x = this.originPoints[i].x * latticeLength + this.pathBound.left;
            this.drawingPoints[i].y = this.originPoints[i].y * latticeLength + this.pathBound.top;
        }
        this.drawingPath = createPath(this.drawingPoints);
    }
    public float getLatticeLengthLength(){return this.latticeLength;}

    public void setDrawPoints(String markExString){
        List<DrawPoint> pointList = DrawPoint.parseDrawPoints(markExString);
        this.originPoints = new DrawPoint[pointList.size()];
        this.drawingPoints = new DrawPoint[pointList.size()];
        this.maxCoorLength = 0;
        for(int i = 0; i < this.originPoints.length; i++){
            this.originPoints[i] = pointList.get(i);
            int max = (int)Math.max(this.originPoints[i].x, this.originPoints[i].y);
            this.drawingPoints[i] = this.originPoints[i].clone();
            this.drawingPoints[i].x = (this.drawingPoints[i].x * this.latticeLength + this.pathBound.left);
            this.drawingPoints[i].y = (this.drawingPoints[i].y * this.latticeLength + this.pathBound.top);
            if(max > this.maxCoorLength) this.maxCoorLength = max;
        }
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

    /**
     * Try to calculate column row value for special coordinate
     * Return int[]{column, row} if successful
     */
    private int[] tryGetColumnRow(int x, int y){
        //Validate x and y coordinate is in inner board
        if (this.boardBound.hasInsideOf(x,y)) {
            //Calculate relative coordinate and column/row
            float relativeX = this.pathBound.relativeX(x);
            float relativeY = this.pathBound.relativeY(y);
            int column = (int)Math.rint(relativeX / latticeLength);
            int row = (int)Math.rint(relativeY / latticeLength);
            double colTarget = column * latticeLength;
            double rowTarget = row * latticeLength;
            //Validate x and y relative coordinate is in circle with special radius
            if(Math.abs(relativeX - colTarget) < this.radius) {
                if (Math.abs(relativeY - rowTarget) < this.radius) {
                    return new int[]{column, row };
                }
            }
        }
        return null;
    }

    public boolean addViewIn(View v, int column, int row){
        if(column > internalViews.length || row > internalViews[0].length){
            throw new IndexOutOfBoundsException("Column/Row can't large than columnCount/rowCount");
        }
        if(internalViews[column][row] != null){
            return false;
        }
        internalViews[column][row] = v;
        super.addView(v);
        return true;
    }

    public void tips(final int column, final int row){
        if(column > internalViews.length || row > internalViews[0].length){
            return;
        }
        if(internalViews[column][row] != null){
            internalViews[column][row].bringToFront();
            tips(internalViews[column][row], 3);
        }
    }

    private void tips(final View view, final int times){
        final int remainTime = times - 1;
        view.animate().scaleX(1.5f).scaleY(1.5f).alpha(0.9f).setDuration(500).withEndAction(new Runnable() {
            @Override
            public void run() {
                view.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(500).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        if(remainTime > 0){
                            postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    tips(view, remainTime);
                                }
                            }, 200);
                        }
                    }
                }).start();
            }
        }).start();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event){
        return super.onInterceptTouchEvent(event);
    }

    public static final int MIN_CLICK_DELAY_TIME = 1000;
    private long lastClickTime = 0;
    private int downX = 0;
    private int downY = 0;
    @Override
    public boolean onTouchEvent(MotionEvent event){
        if(event.getAction() == MotionEvent.ACTION_UP){
            long currentTime = System.nanoTime();//Prevent double or more click in short time
            if (currentTime - lastClickTime > MIN_CLICK_DELAY_TIME) {
                lastClickTime = currentTime;
                //Get event coordinate
                int x = (int) event.getX();
                int y = (int) event.getY();
                if(Math.abs(downX - x) <= this.radius && Math.abs(downX - x) <= this.radius){
                    //Try to calculate column/row value
                    int[] col_row = this.tryGetColumnRow(x, y);
                    if (col_row != null) {//
                        raiseOnLatticeClick(col_row[0], col_row[1]);
                        //Ensure this ViewGroup don't consume this event
                        //So return false so that Activity can receive event
                        return false;
                    }
                }
            }
        }else if(event.getAction() == MotionEvent.ACTION_DOWN){
            //Consume this event for receive ACTION_UP event by return true
            downX = (int) event.getX();
            downY = (int) event.getY();
            return true;
        }
        return super.onTouchEvent(event);
    }

    private void raiseOnLatticeClick(int col, int row){
        if (clickListener != null) {//
            //Raise Listener
            this.clickListener.onLatticeClick(col, row, this.radius);
            //Debug option
            Logger.logLine("Lattice click at " + col + "," + row);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        for(int i = 0; i < internalViews.length; i++){
            for (int j = 0; j < internalViews[i].length; j++){
                if(internalViews[i][j] == null) continue;
                measureChild(internalViews[i][j],widthMeasureSpec, heightMeasureSpec);
            }
        }
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
        int minAdapt = Math.min(sizeHeight, sizeWidth);
        setMeasuredDimension(minAdapt, minAdapt);
        /*
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int layoutWidth = 0;
        int layoutHeight = 0;
        int cWidth = 0;
        int cHeight = 0;
        if(widthMode == MeasureSpec.EXACTLY){
            layoutWidth = sizeWidth;
        }else{
            for(int i = 0; i < internalViews.length; i++){
                for (int j = 0; j < internalViews[i].length; j++){
                    if(internalViews[i][j] == null) continue;
                    View child = internalViews[i][j];
                    cHeight = child.getMeasuredHeight();
                    layoutHeight = cHeight > layoutHeight ? cHeight : layoutHeight;
                }
            }
        }
        setMeasuredDimension(layoutWidth, layoutHeight);*/
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b){
        int height = 0;
        int count = getChildCount();
        View child;
        for(int i = 0; i < internalViews.length; i++){
            for (int j = 0; j < internalViews[i].length; j++){
                if(internalViews[i][j] == null) continue;
                child = internalViews[i][j];
                //View.layout(left, top, right, bottom);
                int ch = child.getMeasuredHeight();
                int cw = child.getMeasuredWidth();
                int left = (int)(this.pathBound.left + latticeLength * i- cw / 2);//L * Column
                int top = (int)(this.pathBound.top + latticeLength * j - ch / 2);//L * row
                child.layout(left, top, left + cw, top + ch);
                height += child.getMeasuredHeight();
                //ConsoleEx.tryLog("height w/h : "+top + "," + left);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas){
        //super.onDraw(canvas);
        if(drawingPath == null)drawingPath = createPath(this.drawingPoints);
        canvas.drawPath(drawingPath, this.pathPaint);
    }

    @Override
    public void drawChess(final int x, final int y, final boolean host) {
        ActionEx.invoke(new IAction() {
            @Override
            public void onAction() {
                drawEllipseImpl(x, y, host);
            }
        }, invokeHandler);
    }

    @Override
    public void removeChess(final int x, final int y) {
        ActionEx.invoke(new IAction() {
            @Override
            public void onAction() {
                removeEllipseImpl(x, y);
            }
        }, invokeHandler);
    }

    @Override
    public void clearChess(){
        ActionEx.invoke(new IAction() {
            @Override
            public void onAction() {
                newestTipView = null;
                GosViews.this.removeAllViews();
                for(int i = 0; i < internalViews.length; i++){
                    for(int j = 0; j < internalViews[i].length; j++){
                        internalViews[i][j] = null;
                    }
                }
            }
        }, invokeHandler);
    }

    @Override
    public void setLatticeClickListener(core.interfaces.LatticeClickListener listener) {
        this.clickListener = listener;
    }

    /**
     *OnLattice click for listening to lattice click on board
     */
    public static abstract interface OnLatticeClickListener{
        public abstract void onLatticeClick(int column, int row, int clickRadius);
    }

    private Handler invokeHandler = new Handler(){
        public void handleMessage(Message message) {
            IAction action = (IAction)message.obj;
            if(action != null) action.onAction();
        }
    };

    private void drawEllipseImpl(int x, int y, boolean host){
        Circle circle = new Circle(this.getContext());
        circle.setFill(host ? ColorEx.SKYBLUE : Color.GRAY);
        if(newestTipView != null)
            newestTipView.setStrokeFill(Color.WHITE);
        newestTipView = circle;
        newestTipView.setStrokeFill(ColorEx.ACTIVE);
        this.addViewIn(circle, x, y);
        SFXService.play(this.getContext(), R.raw.click2);
        circle.setScaleX(1.5f);
        circle.setScaleY(1.5f);
        circle.setAlpha(0.5f);
        circle.animate().scaleX(1).scaleY(1).alpha(1).setDuration(100).start();
    }

    private void removeEllipseImpl(int x,int y){
        if(x > 0 && y > 0 && x < internalViews.length && y < internalViews[x].length){
            View view = internalViews[x][y];
            if(view != null){
                if(newestTipView == view)
                    newestTipView = null;
                internalViews[x][y] = null;
                this.removeView(view);
            }
        }

    }

    private core.interfaces.LatticeClickListener clickListener;
}
