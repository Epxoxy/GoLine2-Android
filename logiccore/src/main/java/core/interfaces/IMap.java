package core.interfaces;

import java.util.Iterator;

import core.data.IntPoint;
import core.data.Point3Line;

public interface IMap {
    int getAllow();
    int getNotAllow();
    Iterator<Point3Line> linesOf(IntPoint p);
    Iterator<Point3Line> linesOf(int x, int y);
    Iterator<Point3Line> lines();
    int[][] currentData();
}
