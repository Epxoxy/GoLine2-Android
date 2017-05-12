package com.example.xiaox.goline2.logic.interfaces;

/**
 * Created by xiaox on 2/2/2017.
 */
public interface IChessAnalyzer<TResult> {
    TResult analysis(int[][] board, int deep);
}
