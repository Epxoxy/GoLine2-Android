package core.struct;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import core.data.DataBox;
import core.data.IntPoint;
import core.data.Point3Line;
import core.helpers.ArrayHelper;
import core.helpers.Filter;
import core.interfaces.Match;

public class MapFormation implements core.interfaces.IMap {
    private int allow = 1;
    private int notAllow = 0;
    private int minPlayer = 0;
    private int maxPlayer = 0;
    private int maxStep = 0;
    private int[][] entry = new int[][]{
            { 1,0,0,1,0,0,1 },
            { 0,0,1,1,1,0,0 },
            { 0,1,0,1,0,1,0 },
            { 1,1,1,0,1,1,1 },
            { 0,1,0,1,0,1,0 },
            { 0,0,1,1,1,0,0 },
            { 1,0,0,1,0,0,1 }
    };
    private DataBox dataBox;
    private List<Point3Line> p3Lines;

    public MapFormation(int minPlayer, int maxPlayer, int maxStep){
        this.maxPlayer = maxPlayer;
        this.minPlayer = minPlayer;
        this.maxStep = maxStep;
        int[][] lineArray = new int[][]{
                //Horizontal
                {0, 0, 3, 0, 6, 0}, {0, 3, 1, 3, 2, 3}, {0, 6, 3, 6, 6, 6}, {1, 2, 3, 2, 5, 2},
                {1, 4, 3, 4, 5, 4}, {2, 1, 3, 1, 4, 1}, {2, 5, 3, 5, 4, 5}, {4, 3, 5, 3, 6, 3},
                //Vertical
                {0, 0, 0, 3, 0, 6}, {1, 2, 1, 3, 1, 4}, {3, 0, 3, 1, 3, 2},
                {3, 4, 3, 5, 3, 6}, {5, 2, 5, 3, 5, 4}, {6, 0, 6, 3, 6, 6},
                //Declining
                {1, 2, 2, 3, 3, 4}, {1, 4, 2, 5, 3, 6}, {3, 0, 4, 1, 5, 2}, {3, 2, 4, 3, 5, 4},//-1
                {1, 2, 2, 1, 3, 0}, {1, 4, 2, 3, 3, 2}, {3, 4, 4, 3, 5, 2}, {3, 6, 4, 5, 5, 4},//1
        };
        p3Lines = initLineArray(lineArray);
        dataBox = new DataBox(copyEntry(), allow, notAllow);
    }


    public int getMaxPlayer() {
        return maxPlayer;
    }

    public int getMaxStep() {
        return maxStep;
    }

    public int getMinPlayer() {
        return minPlayer;
    }

    private List<Point3Line> initLineArray(int[][] linesArray){
        List<Point3Line> cache = new ArrayList<>();
        for (int[] aLinesArray : linesArray) {
            if (aLinesArray.length != 6) continue;
            Point3Line xy3Line = new Point3Line(
                    aLinesArray[0],
                    aLinesArray[1],
                    aLinesArray[2],
                    aLinesArray[3],
                    aLinesArray[4],
                    aLinesArray[5]);
            cache.add(xy3Line);
        }
        return cache;
    }

    private int[][] copyEntry(){
        return ArrayHelper.copyMatrix(entry);
    }

    @Override
    public int getAllow() {
        return allow;
    }

    @Override
    public int getNotAllow() {
        return notAllow;
    }

    @Override
    public Iterator<Point3Line> linesOf(IntPoint p) {
        return linesOf(p.getX(), p.getY());
    }

    @Override
    public Iterator<Point3Line> linesOf(int x, int y) {
        return Filter.filter(p3Lines, filterInLine(x, y)).iterator();
    }

    @Override
    public Iterator<Point3Line> lines() {
        return p3Lines.iterator();
    }

    @Override
    public int[][] currentData() {
        return dataBox.copy();
    }

    public DataBox getDataBox(){
        return dataBox;
    }

    private Match<Point3Line> filterInLine(final int x, final int y){
        return new Match<Point3Line>() {
            @Override
            public boolean validate(Point3Line line) {
                return line.isInLine(x, y);
            }
        };
    }
}
