package com.example.xiaox.goline2.extension.helper;

/**
 * Created by xiaox on 1/29/2017.
 */
public class ArrayHelper {
    public static int[][] copyMatrix(int[][] source){
        int[][] copy = new int[source.length][source[0].length];
        for(int i = 0; i < source.length; i++){
            for(int j = 0; j < source.length; j++){
                copy[i][j] = source[i][j];
            }
        }
        return copy;
    }
}
