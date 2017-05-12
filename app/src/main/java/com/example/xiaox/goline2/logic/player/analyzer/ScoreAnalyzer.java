package com.example.xiaox.goline2.logic.player.analyzer;

import com.example.xiaox.goline2.extension.event.EventArgs;
import com.example.xiaox.goline2.extension.event.IEventHandler;
import com.example.xiaox.goline2.extension.helper.Filter;
import com.example.xiaox.goline2.extension.helper.Match;
import com.example.xiaox.goline2.logic.basic.ActionType;
import com.example.xiaox.goline2.logic.basic.MapStruct;
import com.example.xiaox.goline2.logic.basic.Point;
import com.example.xiaox.goline2.logic.basic.SpotDataLogger;
import com.example.xiaox.goline2.logic.basic.XY3Line;
import com.example.xiaox.goline2.logic.interfaces.IChessAnalyzer;
import com.example.xiaox.goline2.logic.player.AIPlayer;
import com.example.xiaox.goline2.logic.service.ChessGameService;
import com.example.xiaox.goline2.logic.service.GameActionEventArgs;
import com.example.xiaox.goline2.logic.service.ServiceProviderChangedEventArgs;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Random;
import java.util.Stack;

/**
 * Created by xiaox on 2/3/2017.
 */
public class ScoreAnalyzer implements IChessAnalyzer<Point> {
    private AIPlayer aiPlayer;
    private int[][] usingScores;
    private Stack<int[][]> historyScore = new Stack<int[][]>();
    private int[][] scores = null;
    private WeakReference<ChessGameService> chessGameServiceRef;

    public ScoreAnalyzer(AIPlayer aiPlayer, ChessGameService chessGameService) {
        this.aiPlayer = aiPlayer;
        chessGameServiceRef = new WeakReference<ChessGameService>(chessGameService);
        if(chessGameService != null){
            chessGameServiceRef.get().onStarting.addHandler(onGameStartingHandler);
            chessGameServiceRef.get().onAction.addHandler(onActionUpdated);
        }
        this.aiPlayer.onServiceProviderChanged.addHandler(providerChangedHandler);
    }

    @Override
    public Point analysis(int[][] board, int deep) {
        return tryFindHighest();
    }

    private IEventHandler<ServiceProviderChangedEventArgs> providerChangedHandler = new IEventHandler<ServiceProviderChangedEventArgs>() {
        @Override
        public void onEvent(Object sender, ServiceProviderChangedEventArgs args) {
            if(args.getOldService() != null){
                args.getOldService().onStarting.removeHandler(onGameStartingHandler);
                args.getOldService().onAction.removeHandler(onActionUpdated);
            }
            if(args.getNewService() != null){
                ScoreAnalyzer.this.chessGameServiceRef = new WeakReference<ChessGameService>(args.getNewService());
                args.getOldService().onStarting.addHandler(onGameStartingHandler);
                args.getOldService().onAction.addHandler(onActionUpdated);
            }
        }
    };

    private int[][] getScores() {
        if (usingScores == null) {
            if(scores == null){
                scores = new int[7][7];
                for (int i = 0; i < scores.length; ++i){
                    for (int j = 0; j < scores[i].length; ++j){
                        scores[i][j] = Filter.filter(MapStruct.GetMapStruct().xy3Lines, filterInLine(i, j)).size();
                    }
                }
            }
            usingScores = new int[scores.length][scores[0].length];
            for (int i = 0; i < scores.length; ++i){
                for (int j = 0; j < scores[i].length; ++j){
                    usingScores[i][j] = scores[i][j];
                }
            }
        }
        return usingScores;
    }
    private void setScores(int[][] scores) {
        usingScores = scores;
    }

    private IEventHandler onGameStartingHandler = new IEventHandler() {
        @Override
        public void onEvent(Object sender, EventArgs args) {
            usingScores = null;
        }
    };

    private IEventHandler<GameActionEventArgs> onActionUpdated = new IEventHandler<GameActionEventArgs>() {
        @Override
        public void onEvent(Object sender, GameActionEventArgs args) {
            if (args.getActionType() == ActionType.Input) {
                Integer[] data = (Integer[]) args.getParams();
                if (data != null && data.length == 2)
                    updateAround(data[0], data[1]);
            } else if (args.getActionType() == ActionType.Undo) {
                recoveryLast();
            }
        }
    };

    private void updateAround(int x, int y) {
        addHistory(getScores());
        List<XY3Line> lines = Filter.filter(MapStruct.GetMapStruct().xy3Lines, filterInLine(x, y));
        for (XY3Line xy3Line : lines) {
            updateLineScore(xy3Line, aiPlayer.mark);
        }
    }

    private void updateLineScore(XY3Line xy3Line, int id) {
        if (xy3Line == null) return;
        SpotDataLogger datalogger = chessGameServiceRef.get().spotDataLogger.get();
        if (datalogger == null) return;
        Point[] points = xy3Line.toPoints();
        int reachable = 0, selfOccupy = 0, otherOccupy = 0;
        for (Point point : points) {
            if (datalogger.canLogAt(point.x, point.y)) ++reachable;
            else {
                if (datalogger.isOccupiedBy(point.x, point.y, id)) ++selfOccupy;
                else ++otherOccupy;
                //TODO make unreachable step's score to zero
                getScores()[point.x][point.y] = 0;
            }
        }
        //COMMENT DEFINE => ('POS(condition)[num] : possibilities')possibilities of condition is num
        //COMMENT DEFINE => ('COMB<params>')combination of params
        //POS(Total)[3*3*3] = 27, POS(reachable=3)[1], POS(reachable=0)[2*2*2]=8
        if (reachable == 3 || reachable == 0) return;

        //POS(otherOccupy + selfOccupy)[2] : 1or2
        for (int i = 0; i < points.length; ++i) {
            if (datalogger.canLogAt(points[i].x, points[i].y)) {
                int score = 0;
                switch (selfOccupy) {
                    case 0:
                        //POS(COMB<other-other-empty>)[3]
                        if (otherOccupy == 2) score = Level.OOE_LEVEL2;//  High level
                        //POS(COMB<other-empty-empty>)[3]
                        if (otherOccupy == 1) score = Level.OEE_LEVEL5;//
                        break;
                    case 1:
                        //POS(COMB<other-self-empty>)[6]
                        if (otherOccupy == 1) score = Level.SOE_LEVEL6;//
                        //POS(COMB<self-empty-empty>)[3]
                        if (otherOccupy == 0) score = Level.SEE_LEVEL3;//
                        break;
                    case 2:
                        //POS(COMB<self-self-empty>)[3]
                        //TODO highest
                        score = Level.SSE_LEVEL1;
                        break;
                    default:
                        break;
                }
                getScores()[points[i].x][points[i].y] += score;
            }
        }
    }

    private void addHistory(int[][] newest) {
        int[][] array = new int[newest.length][newest[0].length];
        for (int i = 0; i < array.length; ++i)
            for (int j = 0; j < array[i].length; ++j)
                array[i][j] = newest[i][j];
        historyScore.push(array);
    }

    private void recoveryLast() {
        if (historyScore.isEmpty()) return;
        setScores(historyScore.pop());
    }

    private Match filterInLine(final int x, final int y) {
        return new Match<XY3Line>() {
            @Override
            public boolean validate(XY3Line xy3Line) {
                return xy3Line.isInLine(x, y);
            }
        };
    }

    public Point tryFindHighest(){
        int highestScore = 0, highestCount = 0;
        int[][] scores = getScores();
        for (int i = 0; i < scores.length; ++i)
        {
            for (int j = 0; j < scores[i].length; ++j)
            {
                if (highestScore < scores[i][j])
                {
                    highestScore = scores[i][j];
                    highestCount = 0;
                }
                if (highestScore == scores[i][j])
                {
                    ++highestCount;
                }
            }
        }
        if (highestCount > 0 && highestScore > 0)
        {
            int findIndex = (new Random()).nextInt(highestCount);
            for (int i = 0, index = 0; i < scores.length; ++i)
            {
                for (int j = 0; j < scores[i].length; ++j)
                {
                    if (scores[i][j] == highestScore)
                    {
                        if (index == findIndex)
                        {
                            return new Point(i, j);
                        }
                        ++index;
                    }
                }
            }
        }
        return null;
    }


    /// <summary>
    /// Level for score ai
    /// </summary>
    class Level {
        /// <summary>
        /// From(COMB|self-self-empty|),Target(COMB|self-self-self|)
        /// </summary>
        public static final int SSE_LEVEL1 = 750;
        /// <summary>
        ///From(COMB|other-other-empty|),Target(COMB|self-other-other|)
        /// </summary>
        public static final int OOE_LEVEL2 = 150;
        /// <summary>
        ///From(COMB|self-empty-empty|),Target(COMB|self-self-empty|)
        /// </summary>
        public static final int SEE_LEVEL3 = 25;
        /// <summary>
        ///From(COMB|empty-empty-empty|),Target(COMB|self-empty-empty|)
        /// </summary>
        public static final int EEE_LEVEL4 = 5;
        /// <summary>
        ///From(COMB|other-empty-empty|),Target(COMB|other-self-empty)
        /// </summary>
        public static final int OEE_LEVEL5 = 1;
        /// <summary>
        ///From(COMB|self-other-empty|),Target(COMB|self-self-other|)
        /// </summary>
        public static final int SOE_LEVEL6 = 0;
    }
}
