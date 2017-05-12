package com.example.xiaox.goline2.logic.player.analyzer;

import com.example.xiaox.goline2.logic.basic.Point;
import com.example.xiaox.goline2.logic.interfaces.IChessAnalyzer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by xiaox on 2/3/2017.
 */
public class RandomAnalyzer implements IChessAnalyzer<Point> {
    int reachableValue;
    public RandomAnalyzer(int reachableValue){
        this.reachableValue = reachableValue;
    }

    @Override
    public Point analysis(int[][] board, int deep) {
        List<Point> available = new ArrayList<>();
        for(int i = 0; i < board.length; i++){
            for(int j = 0; j < board[i].length; j++){
                if(board[i][j] == this.reachableValue){
                    available.add(new Point(i, j));
                }
            }
        }
        if (available.size() > 0)
        {
            int randomIndex = new Random().nextInt(available.size());
            return available.get(randomIndex);
        }
        return null;
    }
}
