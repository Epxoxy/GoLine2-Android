package com.example.xiaox.goline2.logic.basic;

/**
 * Created by xiaox on 1/29/2017.
 */
public interface PlaceableLine {
    boolean isInLine(Point point);
    Point[] toPoints();
}
