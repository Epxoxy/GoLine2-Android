package com.example.xiaox.goline2.extension.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

import java.util.ArrayList;


public class TextCircle extends android.support.v7.widget.AppCompatTextView {

    public TextCircle(Context context){
        super(context);
        init();
    }

    public TextCircle(Context context, AttributeSet attrs){
        super(context, attrs);
        init();
    }

    public TextCircle(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
        init();
    }

    private int cx = 0;
    private int cy = 0;
    private int maxRadius = 0;
    private int fillWidth = 0;
    private int strokeWidth = 0;
    private int fillColor;
    private int strokeColor;
    private Paint fillPaint = null;
    private Paint strokePaint = null;
    private static final int DEFAULT_FILL = Color.GRAY;
    private static final int DEFAULT_STROKE_FILL = Color.WHITE;
    private static final int DEFAULT_VIEW_SIZE = 128;
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
    }

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
    protected void onDraw(Canvas canvas){
        canvas.drawCircle(cx, cy, this.fillWidth, fillPaint);
        canvas.drawCircle(cx, cy, this.fillWidth, strokePaint);
        super.onDraw(canvas);
    }
}
