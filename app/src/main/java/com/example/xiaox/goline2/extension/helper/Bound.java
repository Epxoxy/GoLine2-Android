package com.example.xiaox.goline2.extension.helper;

/**
 * Created by xiaox on 1/22/2017.
 * Extension class for clearly know surrounding rectangle
 */
public class Bound{
    public int left;
    public int right;
    public int top;
    public int bottom;

    public Bound(){}

    public Bound(int value){
        this.update(value,value,value,value );
    }

    public Bound(int left, int top, int right, int bottom){
        this.update(left,top,right,bottom );
    }

    public void update(int left, int top, int right, int bottom){
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }
    public void updateBy(int left, int top, int height, int width){
        this.left = left;
        this.top = top;
        this.right =  left + height;
        this.bottom = top + width;
    }

    public boolean hasInsideOf(int x, int y){
        return x >= this.left && x <= this.right && y >= this.top && y <= this.bottom;
    }

    public int relativeX(int originX){
        return originX - this.left;
    }

    public int relativeY(int originY){
        return originY - this.top;
    }

    public Bound recreateWithPadding(int left, int top, int right, int bottom){
        Bound bound = new Bound();
        bound.update(this.left + left, this.top + top, this.right + right, this.bottom + bottom);
        return bound;
    }
    public Bound recreateWithPadding(int value){
        return this.recreateWithPadding(value, value,value,value);
    }

    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append(this.left + ",");
        builder.append(this.top + ",");
        builder.append(this.right + ",");
        builder.append(this.bottom + ",");
        return  builder.toString();
    }
}