package com.example.xiaox.goline2.logic.basic;

/**
 * Created by xiaox on 1/29/2017.
 */
public class XY3Line implements PlaceableLine {
    public Point point1;
    public Point point2;
    public Point point3;

    public XY3Line(){
        this.init();
    }

    public XY3Line(int x1, int y1, int x2, int y2, int x3, int y3){
        this();
        this.point1.x = x1;
        this.point1.y = y1;
        this.point2.x = x2;
        this.point2.y = y2;
        this.point3.x = x3;
        this.point3.y = y3;
    }

    public XY3Line(Point point1, Point point2, Point point3){
        this(point1.x,point1.y, point2.x,  point2.y, point3.x, point3.y);
    }

    private void init(){
        point1 = new Point();
        point2 = new Point();
        point3 = new Point();
    }

    @Override
    public boolean isInLine(Point point) {
        return (this.point1.x == point.x && this.point1.y == point.y)||
                (this.point2.x == point.x && this.point2.y == point.y)||
                (this.point3.x == point.x && this.point3.y == point.y);
    }

    public boolean isInLine(int x, int y){
        return (this.point1.x == x && this.point1.y == y)||
                (this.point2.x == x && this.point2.y == y)||
                (this.point3.x == x && this.point3.y == y);
    }

    @Override
    public Point[] toPoints() {
        return new Point[]{point1, point2, point3};
    }
}
