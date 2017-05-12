package com.example.xiaox.goline2.logic.basic;

import com.example.xiaox.goline2.logic.interfaces.IChessMapStruct;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiaox on 1/29/2017.
 */
public class MapStruct{
    private static final int REACHABLE = 1;
    public static final int[][] chessBoard = new int[][]{
            { 1,0,0,1,0,0,1 },
            { 0,0,1,1,1,0,0 },
            { 0,1,0,1,0,1,0 },
            { 1,1,1,0,1,1,1 },
            { 0,1,0,1,0,1,0 },
            { 0,0,1,1,1,0,0 },
            { 1,0,0,1,0,0,1 }
    };
    private static final  int[][] lineArray = new int[][]{
            //Horizontal
        {0,0, 3,0, 6,0 },{0,3, 1,3, 2,3 },{0,6, 3,6, 6,6 },{1,2, 3,2, 5,2 },
        {1,4, 3,4, 5,4 },{2,1, 3,1, 4,1 },{2,5, 3,5, 4,5 },{4,3, 5,3, 6,3 },
        //Vertical
        {0,0, 0,3, 0,6 },{1,2, 1,3, 1,4 },{3,0, 3,1, 3,2 },
        {3,4, 3,5, 3,6 },{5,2, 5,3, 5,4 },{6,0, 6,3, 6,6 },
        //Declining
        {1,2, 2,3, 3,4 },{1,4, 2,5, 3,6 },{3,0, 4,1, 5,2 },{3,2, 4,3, 5,4 },//-1
        {1,2, 2,1, 3,0 },{1,4, 2,3, 3,2 },{3,4, 4,3, 5,2 },{3,6, 4,5, 5,4 },//1
    };

    private static MapStruct mapStruct = null;
    public static MapStruct GetMapStruct(){
        if(mapStruct == null) createMapStruct(lineArray);
        return mapStruct;
    }

    public static void createMapStruct(int[][] lineArray){
        mapStruct = new MapStruct(lineArray);
    }

    private MapStruct(int[][] linesArray){
        initLineArray(linesArray);
    }

    private void initLineArray(int[][] linesArray){
        List<XY3Line> cache = new ArrayList<>();
        for(int i = 0; i < linesArray.length; i++){
            if(linesArray[i].length != 6) continue;
            XY3Line xy3Line = new XY3Line(
                    linesArray[i][0],
                    linesArray[i][1],
                    linesArray[i][2],
                    linesArray[i][3],
                    linesArray[i][4],
                    linesArray[i][5]);
            cache.add(xy3Line);
        }
        xy3Lines = cache;
    }

    public int[][] defEntry(){
        return null;
    }
    public List<XY3Line> xy3Lines;
}
