package core.analyzer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import core.data.IntPoint;
import core.data.Point3Line;
import core.helpers.ArrayHelper;
import core.interfaces.IAnalyzer;
import core.interfaces.IMap;

public class AlphaBetaMaxMinAnalyzer implements IAnalyzer<IMap,IntPoint> {
    private static int MIN_VALUE = -GlobalLevel.SSS * 4;
    private static int MAX_VALUE = GlobalLevel.SSS * 4;
    private int notAllow = 0;
    private int allow = 0;
    private boolean isAnalysing;
    private int manId;
    private int envId;
    private IMap map;

    public AlphaBetaMaxMinAnalyzer() {
    }

    public AlphaBetaMaxMinAnalyzer(int envId, int manId) {
        this.envId = envId;
        this.manId = manId;
    }

    @Override
    public IntPoint analysis(IMap map, int deep) {
        if (isAnalysing)
            return null;
        isAnalysing = true;

        this.map = map;
        this.notAllow = map.getNotAllow();
        this.allow = map.getAllow();
        IntPoint p = findMaxMin(map.currentData(), deep);
        this.map = null;

        isAnalysing = false;
        return p;
    }

    public void setEnvId(int envId) throws Exception {
        if (isAnalysing) throw new Exception("Cannot set EnvId when analysing");
        this.envId = envId;
    }

    public void setManId(int manId) throws Exception {
        if (isAnalysing) throw new Exception("Cannot set ManId when analysing");
        this.manId = manId;
    }

    public IntPoint findMaxMin(int[][] board, int deep) {
        int best = MIN_VALUE;
        List<IntPoint> bestLocations = new ArrayList<>();
        List<IntPoint> points = generateAvailable(board, deep);
        //Init scores
        int[][] scores = new int[board.length][board[0].length];
        fullEvaluate(board, scores);
        //Start max-minValue
        for (int i = 0; i < points.size(); ++i) {
            IntPoint point = points.get(i);
            board[point.x][point.y] = envId;
            int[][] scoresCopy = ArrayHelper.copyMatrix(scores);
            evaluatePoint(board, scoresCopy, point.x, point.y);
            int v = min(board, scoresCopy, deep - 1, (best > MIN_VALUE ? best : MIN_VALUE), MAX_VALUE);
            board[point.x][point.y] = allow;
            if (v < best) continue;
            if (v > best) {
                best = v;
                bestLocations.clear();
            }
            bestLocations.add(point);
        }
        //If best is not found, get best from scores
        if (bestLocations.size() < 1) {
            System.out.println("Not found");
            for (int i = 0; i < scores.length; ++i) {
                for (int j = 0; j < scores[i].length; ++j) {
                    if (board[i][j] == allow) {
                        if (scores[i][j] < best) continue;
                        if (scores[i][j] > best) {
                            best = scores[i][j];
                            bestLocations.clear();
                        }
                        bestLocations.add(new IntPoint(i, j));
                    }
                }
            }
        }
        int index = (new Random()).nextInt(bestLocations.size());
        return bestLocations.get(index);
    }

    private int max(int[][] board, int[][] scores, int deep, int alpha, int beta) {
        int v0 = calculateTotal(scores);
        if (deep <= 0 || isEnded(board)) return v0;
        int best = MIN_VALUE;
        List<IntPoint> points = generateAvailable(board, deep);
        for (int i = 0; i < points.size(); ++i) {
            IntPoint point = points.get(i);
            board[point.x][point.y] = envId;
            int[][] scoresCopy = ArrayHelper.copyMatrix(scores);
            evaluatePoint(board, scoresCopy, point.x, point.y);
            int v = min(board, scoresCopy, deep - 1, alpha, (best > beta ? best : beta));
            board[point.x][point.y] = allow;
            if (v > best) best = v;
            if (v > alpha) break;//AB-cut++;
        }
        return best;
    }

    private int min(int[][] board, int[][] scores, int deep, int alpha, int beta) {
        int v0 = calculateTotal(scores);
        if (deep <= 0 || isEnded(board)) return v0;
        int best = MAX_VALUE;
        List<IntPoint> points = generateAvailable(board, deep);
        for (int i = 0; i < points.size(); ++i) {
            IntPoint point = points.get(i);
            board[point.x][point.y] = manId;

            //Every time to copy scores,
            //so that evaluate next location can evaluate with origin score.
            //Copy scores and Evaluate location
            int[][] scoresCopy = ArrayHelper.copyMatrix(scores);
            evaluatePoint(board, scoresCopy, point.x, point.y);
            int v = max(board, scoresCopy, deep - 1, (best < alpha ? best : alpha), beta);
            board[point.x][point.y] = allow;
            if (v < best) best = v;
            if (v < beta) break;//Ab-cut++;
        }
        return best;
    }

    private int calculateTotal(int[][] scores) {
        int value = 0;
        for (int i = 0; i < scores.length; ++i) {
            for (int j = 0; j < scores[i].length; ++j) {
                value += scores[i][j];
            }
        }
        return value;
    }

    private void fullEvaluate(int[][] board, int[][] scores) {
        int[] evaluateSet = null;
        int evaluate = 0;
        for (int i = 0; i < board.length; ++i) {
            for (int j = 0; j < board[i].length; ++j) {
                if (board[i][j] != allow && board[i][j] != notAllow) {
                    evaluateSet = evaluatePointWithLine(board, scores, i, j);
                    //int lineCount = evaluates[0];
                    //int score = evaluates[1];
                    evaluate = evaluateSet[0] + evaluateSet[1];
                    scores[i][j] += evaluate;
                }
            }
        }
    }

    /**
     * <return>Integer array contains Score&LineCount</return>
     **/
    private int[] evaluatePointWithLine(int[][] board, int[][] scores, int x, int y) {
        return getPointScore(board, x, y);
    }

    private int evaluatePoint(int[][] board, int[][] scores, int x, int y) {
        scores[x][y] = getPointScore(board, x, y)[0];
        return scores[x][y];
    }

    private int[] getPointScore(int[][] board, final int x, final int y) {
        int lineCount = 0;
        int totalScore = 0, sseCount = 0, ooeCount = 0;
        Iterator<Point3Line> lines = map.linesOf(x, y);
        for (; lines.hasNext(); ) {
            Point3Line line = lines.next();
            ++lineCount;

            int reachable = 0, selfOccupy = 0, otherOccupy = 0;
            int score = 0;

            IntPoint[] points = line.toPoints();
            for (IntPoint point : points) {
                if (board[point.x][point.y] == allow) ++reachable;
                else {
                    if (board[point.x][point.y] == envId) ++selfOccupy;
                    else ++otherOccupy;
                }
            }

            if (reachable == 3) score = GlobalLevel.EEE;
            if (reachable == 0) {
                if (selfOccupy == 0) score = GlobalLevel.OOO;
                //May be it has error, method shouldn't go here
            }

            switch (selfOccupy) {
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
                case 3:
                    score = GlobalLevel.SSS;
                    break;
                default:
                    break;
            }
            totalScore += score;
        }

        if (sseCount > 1) totalScore += GlobalLevel.DoubleSSE * (sseCount * (sseCount + 1) / 2);
        else if (sseCount == 1) totalScore += GlobalLevel.SSE;

        if (ooeCount > 1)
            totalScore += GlobalLevel.DoubleOOE * (ooeCount * (ooeCount + 1) / 2);
        else if (ooeCount == 1) totalScore += GlobalLevel.OOE;

        return new int[]{totalScore, lineCount};
    }

    private boolean isEnded(int[][] board) {
        int available = 0;
        Iterator<Point3Line> lines = map.lines();
        for (; lines.hasNext(); ) {
            Point3Line line = lines.next();
            IntPoint[] points = line.toPoints();
            int humanOccupy = 0, aiOccupy = 0;
            for (IntPoint point : points) {
                if (board[point.x][point.y] == manId) ++humanOccupy;
                else if (board[point.x][point.y] == envId) ++aiOccupy;
                else if (board[point.x][point.y] == allow) ++available;
            }
            if (humanOccupy == 3 || aiOccupy == 3) return true;
        }
        if (available == 0) return true;
        return false;
    }

    private List<IntPoint> generateAvailable(int[][] board, int deep) {
        List<IntPoint> three = new ArrayList<>();
        List<IntPoint> doubleTwo = new ArrayList<>();
        List<IntPoint> twos = new ArrayList<>();
        List<IntPoint> remainAvailable = new ArrayList<>();
        for (int i = 0; i < board.length; ++i) {
            for (int j = 0; j < board[i].length; ++j) {
                if (board[i][j] == allow) {
                    int scoreCom = evaluateIfInput(board, i, j, envId);
                    if (scoreCom == 0) {
                        //If current score equal to zero
                        //Means current location don't have chess around it.
                        //Skip it.
                        continue;
                    }
                    int scoreHum = evaluateIfInput(board, i, j, manId);
                    IntPoint point = new IntPoint(i, j);
                    if (scoreCom >= OneSidedLevel.SSS) {
                        List<IntPoint> result = new ArrayList<IntPoint>();
                        result.add(point);
                        return result;
                    } else if (scoreHum >= OneSidedLevel.SSS) {
                        three.add(point);
                    } else if (scoreCom >= 2 * OneSidedLevel.SSE) {
                        doubleTwo.add(0, point);
                    } else if (scoreHum >= 2 * OneSidedLevel.SSE) {
                        doubleTwo.add(point);
                    } else if (scoreCom >= OneSidedLevel.SSE) {
                        twos.add(0, point);
                    } else if (scoreHum >= OneSidedLevel.SSE) {
                        twos.add(point);
                    } else {
                        remainAvailable.add(point);
                    }
                }
            }
        }
        if (three.size() > 0)
            return three;
        if (doubleTwo.size() > 0)
            return doubleTwo;
        if (twos.size() > 0)
            return twos;
        return remainAvailable;
    }

    private int evaluateIfInput(int[][] board, int x, int y, int tryValue) {
        int score = 0;
        Iterator<Point3Line> lines = map.linesOf(x, y);
        for (; lines.hasNext(); ) {
            Point3Line line = lines.next();
            IntPoint[] points = line.toPoints();
            int reachable = 0, selfOccupy = 0;//, otherOccupy = 0
            for (IntPoint point : points) {
                int value = board[point.x][point.y];
                if (value == allow) ++reachable;
                else {
                    if (value == tryValue) ++selfOccupy;
                    //else ++otherOccupy;
                }
            }
            //self-self-empty to self-self-self
            if (selfOccupy == 2 && reachable == 1) score += OneSidedLevel.SSS;
                //self-empty-empty to self-self-empty
            else if (selfOccupy == 1 && reachable == 2) score += OneSidedLevel.SSE;
                //empty-empty-empty to self-empty-empty
            else if (reachable == 3) score += OneSidedLevel.SEE;
            else if (reachable == 1) score += OneSidedLevel.EHas;
        }
        return score;
    }


    class GlobalLevel {
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

    class OneSidedLevel {
        public static final int SSS = 10000;
        public static final int SSE = 100;
        public static final int SEE = 10;
        public static final int EHas = 1;
    }
}
