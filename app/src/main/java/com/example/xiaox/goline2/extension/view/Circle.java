package com.example.xiaox.goline2.extension.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.example.xiaox.goline2.extension.helper.Bound;

import org.w3c.dom.Text;

/**
 * Created by xiaox on 1/22/2017.
 */
public class Circle extends View {

    public Circle(Context context){
        super(context);
        init();
    }

    public Circle(Context context, AttributeSet attrs){
        super(context,attrs);
        init();
    }

    public Circle(Context context, AttributeSet attrs, int defStyle){
        super(context,attrs,defStyle);
        init();
    }

    private int cx = 0;
    private int cy = 0;
    private int maxRadius = 0;
    private int fillColor;
    private int fillWidth = 0;
    private int strokeColor;
    private int strokeWidth = 10;
    private Paint fillPaint = null;
    private Paint strokePaint = null;
    private TextPaint textPaint = null;
    private boolean scaleOnTouch = true;
    private PaintFlagsDrawFilter pfd;
    private static final int DEFAULT_VIEW_SIZE = 128;
    private static final int DEFAULT_FILL = Color.GRAY;
    private static final int DEFAULT_STROKE_FILL = Color.WHITE;
    private void init(){
        this.fillColor = DEFAULT_FILL;
        this.strokeColor = DEFAULT_STROKE_FILL;
        fillPaint = new Paint();
        strokePaint = new Paint();
        fillPaint.setColor(this.fillColor);
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setAntiAlias(true);
        strokePaint.setStrokeWidth(strokeWidth);
        strokePaint.setColor(this.strokeColor);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        strokePaint.setAntiAlias(true);
        initTextPaint();
        pfd = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    }

    private void initTextPaint(){
        textPaint = new TextPaint();
        textPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setLinearText(true);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(30);
    }

    public void setScaleOnTouch(boolean scaleOnTouch){
        this.scaleOnTouch = scaleOnTouch;
    }
    public boolean getScaleOnTouch(){
        return this.scaleOnTouch;
    }

    public void setStroke(int stroke){
        this.strokeWidth = stroke;
        this.strokePaint.setStrokeWidth(stroke);
        this.invalidate();
    }
    public int getStroke(){
        return this.strokeWidth;
    }

    public void setFill(int color){
        this.fillColor = color;
        fillPaint.setColor(color);
        invalidate();
    }
    private int getFill(){return this.fillColor;}

    public void setStrokeFill(int color){
        this.strokeColor = color;
        this.strokePaint.setColor(color);
        invalidate();
    }
    private int getStrokeFill(){return this.strokeColor;}

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        super.onMeasure(widthMeasureSpec,heightMeasureSpec);
        int width = resolveSize(DEFAULT_VIEW_SIZE, widthMeasureSpec);
        int height = resolveSize(DEFAULT_VIEW_SIZE, heightMeasureSpec);
        updateRadius(width, height);
        setMeasuredDimension(width, height);
    }

    private void updateRadius(int width, int height){
        if(height > width){
            this.maxRadius = width / 2;
            this.cx = this.maxRadius;
            this.cy = (height - width) / 2 + this.maxRadius;
        }else{
            this.maxRadius = height / 2;
            this.cx = (width - height) / 2 + this.maxRadius;
            this.cy = this.maxRadius;
        }
        this.fillWidth = this.maxRadius - this.strokeWidth;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        if(scaleOnTouch){
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                this.bringToFront();
                this.animate().scaleX(3).scaleY(3).alpha(0.9f).setDuration(200).start();
                return true;
            }
            else if(event.getAction() == MotionEvent.ACTION_UP){
                this.animate().scaleX(1).scaleY(1).alpha(1).setDuration(200).start();
                return true;
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.setDrawFilter(pfd);
        //canvas.saveLayerAlpha(0,0, getWidth(), getHeight(), (int)this.alpha, Canvas.HAS_ALPHA_LAYER_SAVE_FLAG);
        canvas.drawCircle(this.cx, this.cy, this.fillWidth, fillPaint);
        canvas.drawCircle(this.cx, this.cy, this.fillWidth, strokePaint);
        //canvas.restore();
    }
}
