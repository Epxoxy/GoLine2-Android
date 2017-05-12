package com.example.xiaox.goline2.logic.player.analyzer;

import com.example.xiaox.goline2.extension.helper.ArrayHelper;
import com.example.xiaox.goline2.extension.helper.Filter;
import com.example.xiaox.goline2.extension.helper.Logger;
import com.example.xiaox.goline2.extension.helper.Match;
import com.example.xiaox.goline2.logic.basic.MapStruct;
import com.example.xiaox.goline2.logic.basic.Point;
import com.example.xiaox.goline2.logic.basic.XY3Line;
import com.example.xiaox.goline2.logic.interfaces.IChessAnalyzer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Created by xiaox on 1/29/2017.
 * Alpha-beta cut max-min search algorithm analyzer
 */
public class AlphaBetaMaxMinAnalyzer implements IChessAnalyzer<Point> {
    private static int MIN_VALUE = -GlobalLevel.SSS * 4;
    private static int MAX_VALUE = GlobalLevel.SSS * 4;
    private int _denied = 0;
    private int _available = 1;
    private int _humanValue;
    private int _AIValue;

    public AlphaBetaMaxMinAnalyzer(int denied, int available, int humanValue, int AIValue){
        this._denied = denied;
        this._AIValue = AIValue;
        this._humanValue = humanValue;
        this._available = available;
    }

    @Override
    public Point analysis(int[][] board, int deep) {
        return this.findMaxMin(board, deep);
    }

    public Point findMaxMin(int[][] board, int deep){
        int best = MIN_VALUE;
        List<Point> bestLocations = new ArrayList<>();
        List<Point> points = generateAvailable(board, deep);
        //Init scores
        int[][] scores = new int[board.length][board[0].length];
        fullEvaluate(board, scores);
        //Start max-minValue
        for (int i = 0; i < points.size(); ++i)
        {
            Point point = points.get(i);
            board[point.x][point.y] = _AIValue;
            int[][] scoresCopy = ArrayHelper.copyMatrix(scores);
            evaluatePoint(board, scoresCopy, point.x, point.y);
            int v = min( board, scoresCopy, deep - 1, (best > MIN_VALUE ? best : MIN_VALUE), MAX_VALUE);
            if (v == best) bestLocations.add(point);
            if (v > best)
            {
                best = v;
                bestLocations.clear();
                bestLocations.add(point);
            }
            board[point.x][point.y] = _available;
        }
        //If best is not found, get best from scores
        if (bestLocations.size() < 1)
        {
            for(int i = 0; i < scores.length; ++i)
            {
                for (int j = 0; j < scores[i].length; ++j)
                {
                    if(board[i][j] == _available)
                    {
                        if (scores[i][j] == best) bestLocations.add(new Point(i, j));
                        if (scores[i][j] > best)
                        {
                            best = scores[i][j];
                            bestLocations.clear();
                            bestLocations.add(new Point(i, j));
                        }
                    }
                }
            }
        }
        int index = (new Random()).nextInt(bestLocations.size());
        return bestLocations.get(index);
    }

    private int max(int[][] board, int[][] scores, int deep, int alpha, int beta){
        int v0 = evaluateScores(scores);
        if (deep <= 0 || ended(board)) return v0;
        int best = MIN_VALUE;
        List<Point> points = generateAvailable(board, deep);
        for (int i = 0; i < points.size(); ++i)
        {
            Point point = points.get(i);
            board[point.x][point.y] = _AIValue;
            int[][] scoresCopy = ArrayHelper.copyMatrix(scores);
            evaluatePoint(board, scoresCopy, point.x, point.y);
            int v = min(board, scoresCopy, deep - 1, alpha, (best > beta ? best : beta));
            board[point.x][point.y] = _available;
            if (v > best) best = v;
            if (v > alpha) break;//AB-cut++;
        }
        return best;
    }

    private int min(int[][] board, int[][] scores, int deep, int alpha, int beta){
        int v0 = evaluateScores( scores);
        if (deep <= 0 || ended(board)) return v0;
        int best = MAX_VALUE;
        List<Point> points = generateAvailable(board, deep);
        for (int i = 0; i < points.size(); ++i)
        {
            Point point = points.get(i);
            board[point.x][point.y] = _humanValue;

            //Every time to copy scores,
            //so that evaluate next location can evaluate with origin score.
            //Copy scores and Evaluate location
            int[][] scoresCopy = ArrayHelper.copyMatrix( scores);
            evaluatePoint(board, scoresCopy, point.x, point.y);
            int v = max( board,  scoresCopy, deep - 1, (best < alpha ? best : alpha), beta);
            board[point.x][point.y] = _available;
            if (v < best) best = v;
            if (v < beta) break;//Ab-cut++;
        }
        return best;
    }

    private int evaluateScores(int[][] scores){
        int value = 0;
        for (int i = 0; i < scores.length; ++i)
        {
            for (int j = 0; j < scores[i].length; ++j)
            {
                value += scores[i][j];
            }
        }
        return value;
    }

    private void fullEvaluate(int[][] board, int[][] scores){
        int[] evaluateParams = null;
        int evaluate = 0;
        for (int i = 0; i < board.length; ++i)
        {
            for (int j = 0; j < board[i].length; ++j)
            {
                if (board[i][j] != _available && board[i][j] != _denied)
                {
                    evaluateParams = evaluatePointWithLine(board, scores, i, j);
                    //int lineCount = evaluates[0];
                    //int score = evaluates[1];
                    evaluate = evaluateParams[0] + evaluateParams[1];
                    scores[i][j] += evaluate;
                }
            }
        }
    }

    /**
     *
     *<return>Integer array contains Score&LineCount</return>
     **/
    private int[] evaluatePointWithLine(int[][] board, int[][] scores, int x, int y){
        return getPointScore(board, x, y);
    }

    private int evaluatePoint(int[][] board, int[][] scores, int x, int y){
        scores[x][y] = getPointScore(board, x, y)[0];
        return scores[x][y];
    }

    private int[] getPointScore(int[][] board, final int x, final int y){
        int lineCount = 0;
        List<XY3Line> lines = Filter.filter(MapStruct.GetMapStruct().xy3Lines, filterInLine(x, y));
        int totalScore = 0, sseCount = 0, ooeCount = 0;
        for (XY3Line line : lines)
        {
            ++lineCount;

            int reachable = 0, selfOccupy = 0, otherOccupy = 0;
            int score = 0;

            Point[] points = line.toPoints();
            for (Point point : points)
            {
                if (board[point.x][point.y] == _available) ++reachable;
                else
                {
                    if (board[point.x][point.y] == _AIValue) ++selfOccupy;
                    else ++otherOccupy;
                }
            }

            if (reachable == 3) score = GlobalLevel.EEE;
            if (reachable == 0)
            {
                if (selfOccupy == 0) score = GlobalLevel.OOO;
                //May be it has error, method shouldn't go here
                return new int[]{GlobalLevel.SSS, lineCount};
            }

            switch (selfOccupy)
            {
                case 0:
                    if (otherOccupy == 2) ++ooeCount;
                    if (otherOccupy == 1) score = GlobalLevel.OEE;
                    break;
                case 1:
                    if (otherOccupy == 1) score = GlobalLevel.SOE;
                    if (otherOccupy == 0) score = GlobalLevel.SEE;
                    break;
                case 2:
                    ++sseCount;
                    break;
                default: break;
            }
            totalScore += score;
        }

        if (sseCount > 1) totalScore += GlobalLevel.DoubleSSE * (sseCount * (sseCount + 1) / 2);
        else totalScore += GlobalLevel.SSE;

        if (ooeCount > 1) totalScore += GlobalLevel.DoubleOOE * (ooeCount * (ooeCount + 1) / 2);
        else totalScore += GlobalLevel.OOE;

        return new int[]{totalScore, lineCount};
    }

    private boolean ended(int[][] board){
        int available = 0;
        List<XY3Line> lines = MapStruct.GetMapStruct().xy3Lines;
        for (XY3Line line : lines){
            Point[] points = line.toPoints();
            int humanOccupy = 0, aiOccupy = 0;
            for (Point point : points)
            {
                if (board[point.x][point.y] == _humanValue) ++humanOccupy;
                else if (board[point.x][point.y] == _AIValue) ++aiOccupy;
                else if (board[point.x][point.y] == _available) ++available;
            }
            if (humanOccupy == 3 || aiOccupy == 3) return true;
        }
        if (available == 0) return true;
        return false;
    }

    private boolean hasNewWinner(int[][] board, Point inputPoint, int value){
        List<XY3Line> lines = Filter.filter(MapStruct.GetMapStruct().xy3Lines, filterInLine(inputPoint.x, inputPoint.y));
        for (XY3Line line : lines)
        {
            int count = 0;
            Point[] points = line.toPoints();
            for (Point point : points)
            {
                if (board[point.x][point.y] == value) ++count;
            }
            if (count == 3) return true;
        }
        return false;
    }

    private List<Point> generateAvailable(int[][] board, int deep){
        List<Point> three = new ArrayList<>();
        List<Point> doubleTwo = new ArrayList<>();
        List<Point> twos = new ArrayList<>();
        List<Point> remainAvailable = new ArrayList<>();
        for (int i = 0; i < board.length; ++i)
        {
            for (int j = 0; j < board[i].length; ++j)
            {
                if (board[i][j] == _available)
                {
                    int scoreCom = evaluateIfInput(board, i, j, _AIValue);
                    if (scoreCom == 0)
                    {
                        //If current score equal to zero
                        //Means current location don't have chess around it.
                        //Skip it.
                        continue;
                    }
                    int scoreHum = evaluateIfInput(board, i, j, _humanValue);
                    Point point = new Point(i, j);
                    if (scoreCom >= OneSidedLevel.SSS){
                        List<Point> result = new ArrayList<Point>();
                        result.add(point);
                        return result;
                    }
                    else if (scoreHum >= OneSidedLevel.SSS){
                        three.add(point);
                    }
                    else if (scoreCom >= 2 * OneSidedLevel.SSE){
                        doubleTwo.add(0, point);
                    }
                    else if (scoreHum >= 2 * OneSidedLevel.SSE){
                        doubleTwo.add(point);
                    }
                    else if (scoreCom >= OneSidedLevel.SSE){
                        twos.add(0, point);
                    }
                    else if (scoreHum >= OneSidedLevel.SSE){
                        twos.add(point);
                    }
                    else{
                        remainAvailable.add(point);
                    }
                }
            }
        }
        if (three.size() > 0) {
            return three;
        }
        if (doubleTwo.size() > 0) {
            return doubleTwo;
        }
        if (twos.size() > 0) {
            return twos;
        }
        return remainAvailable;
    }

    private int evaluateIfInput(int[][] board, int x, int y, int tryValue){
        int score = 0;
        List<XY3Line> xy3LineList = Filter.filter(MapStruct.GetMapStruct().xy3Lines, filterInLine(x, y));
        for (XY3Line xy3Line : xy3LineList)
        {
            Point[] points = xy3Line.toPoints();
            int reachable = 0, selfOccupy = 0, otherOccupy = 0;
            for (Point point : points)
            {
                int value = board[point.x][ point.y];
                if (value == _available) ++reachable;
                else
                {
                    if (value == tryValue) ++selfOccupy;
                    else ++otherOccupy;
                }
            }
            //self-self-empty to self-self-self
            if (selfOccupy == 2 && reachable == 1) score += OneSidedLevel.SSS;
            //self-empty-empty to self-self-empty
            else if (selfOccupy == 1 && reachable == 2) score += OneSidedLevel.SSE;
            //empty-empty-empty to self-empty-empty
            else if (reachable != 3) score += OneSidedLevel.SEE;
        }
        return score;
    }


    class GlobalLevel{
        public static final int SSS = 12000;
        public static final int DoubleSSE = 2500;
        public static final int SSE = 500;
        public static final int SEE = 100;
        public static final int EEE = 0;
        public static final int SOE = 0;
        public static final int OEE = -100;
        public static final int OOE = -500;
        public static final int DoubleOOE = -2500;
        public static final int OOO = -12000;

    }

    class OneSidedLevel
    {
        public static final int SSS = 10000;
        public static final int SSE = 100;
        public static final int SEE = 1;
    }

    private Match filterInLine(final int x, final int y){
        return new Match<XY3Line>() {
            @Override
            public boolean validate(XY3Line xy3Line) {
                return xy3Line.isInLine(x, y);
            }
        };
    }

}
