package core.judge;

import java.util.Iterator;
import java.util.WeakHashMap;

import core.data.DataBox;
import core.data.DataPoint;
import core.data.InputAction;
import core.data.IntPoint;
import core.data.JudgeCode;
import core.data.Point3Line;
import core.helpers.StopWatch;
import core.helpers.StringHelper;
import core.helpers.TokenHelper;
import core.interfaces.IDisposable;
import core.interfaces.IMap;
import core.interfaces.JudgedListener;
import core.struct.MapFormation;

public class JudgeUnit {
    private JudgedListener judgedListener;

    public long time;

    private DataExtra firstJoin;
    private DataExtra lastJoin;
    private DataExtra first;
    private DataExtra active;
    private DataExtra winner;

    private boolean isStarted;
    private boolean isAttached;

    private WeakHashMap<String, DataExtra> extras;
    private MapFormation map;
    private final Object lockHelper = new Object();
    private final Object judgeLockHelper = new Object();

    public JudgeUnit(MapFormation map) {
        this.map = map;
    }

    private DataBox dataBox() {
        return map.getDataBox();
    }

    public IMap getMap() {
        return map;
    }

    public void setJudgedListener(JudgedListener listener) {
        this.judgedListener = listener;
    }

    //When gameMaster is attach,you can join a valid player
    //before game start
    public boolean join(String[] tokenRev) {
        if (!isAttached || isStarted ||
                tokenRev == null || tokenRev.length != 1)
            return false;
        synchronized (lockHelper) {
            if (extras.size() >= map.getMaxPlayer()) return false;
            String token = TokenHelper.shortToken(10);

            DataExtra current = new DataExtra();
            current.token = token;
            current.boxId = extras.size() + 2;
            current.watcher = new Watcher();
            tokenRev[0] = token;
            if (extras.size() > 0) {
                lastJoin.next = current;
                current.front = lastJoin;
                current.next = firstJoin;
            } else {
                firstJoin = current;
            }
            extras.put(token, current);
            lastJoin = current;
            System.out.println("[Judge-Unit] <-- Joined. [Token: " + token + "]-[BoxId: " + current.boxId + "]");
            raiseJudged(token, JudgeCode.Joined);
            if (extras.size() == 1)
                signAsfirst(current.token);
            return true;
        }
    }

    public boolean leave(String token) {
        if (StringHelper.isNullOrEmpty(token) || !isAttached || isStarted || extras.size() < 1)
            return false;
        synchronized (lockHelper) {
            if (extras.containsKey(token)) {
                DataExtra extra = extras.get(token);
                extra.watcher.dispose();
                extras.remove(token);

                DataExtra front = extra.front;
                DataExtra next = extra.next;
                front.next = next;

                if (extra == lastJoin)
                    lastJoin = front;
                if (extra == firstJoin)
                    firstJoin = next;

                System.out.println("token[{extra.Token}] Leave.");
                raiseJudged(token, JudgeCode.Leave);
                return true;
            } else {
                System.out.println("Token[{token}] Valid Fail.");
                return false;
            }
        }
    }

    public boolean giveUp(String token) {
        if (StringHelper.isNullOrEmpty(token) || !isStarted || extras.size() < 1) return false;
        synchronized (lockHelper) {
            if (extras.containsKey(token)) {
                DataExtra extra = extras.get(token);
                extra.watcher.dispose();
                extras.remove(token);

                DataExtra front = extra.front;
                DataExtra next = extra.next;
                front.next = next;

                if (isStarted && extras.size() == 1)
                    onWinnerAppend(front.token);
                else
                    tryActive(next);
                System.out.println("token[{token}] Leave.");
                raiseJudged(token, JudgeCode.GiveUp);
                return true;
            } else {
                System.out.println("Token[{token}] Valid Fail.");
                return false;
            }
        }
    }

    public boolean signAsfirst(String token) {
        if (StringHelper.isNullOrEmpty(token) || !isAttached || isStarted
                || !extras.containsKey(token))
            return false;
        first = extras.get(token);
        raiseJudged(token, JudgeCode.MarkFirst);
        return true;
    }

    public void attach() {
        isAttached = true;
        extras = new WeakHashMap<>();
    }

    public void detach() {
        for (DataExtra pair : extras.values())
            pair.watcher.dispose();
        extras.clear();
        extras = null;
    }

    public boolean ready(String token) {
        if (StringHelper.isNullOrEmpty(token) || !isAttached || isStarted || extras.size() < 1)
            return false;
        if (extras.containsKey(token)) {
            if (!extras.get(token).isReady) {
                extras.get(token).isReady = true;
                raiseJudged(token, JudgeCode.Ready);
                if (extras.size() >= map.getMinPlayer()) {
                    int count = 0;
                    for (DataExtra extra :
                            extras.values()) {
                        if (extra.isReady) ++count;
                    }
                    if (count == extras.size())
                        start();
                }
            }
            return true;
        }
        return false;
    }

    public boolean start() {
        winner = null;
        if (isAttached && !isStarted && extras.size() >= map.getMinPlayer()) {
            int count = 0;
            for (DataExtra extra :
                    extras.values()) {
                if (extra.isReady) ++count;
            }
            if (count == extras.size()) {
                isStarted = true;
                raiseJudged("", JudgeCode.Started);
                tryActive(first);
                return true;
            }
        }
        return false;
    }

    public void end() {
        if (isStarted) {
            isStarted = false;
            for (DataExtra extra : extras.values())
                extra.watcher.dispose();
            raiseJudged("", JudgeCode.Ended);
        }
    }

    public void reset() {
        dataBox().resetData();
        for (DataExtra extra : extras.values()) {
            extra.watcher.reset();
        }
        raiseJudged("", JudgeCode.Reset);
        start();
    }

    public boolean handInput(String token, InputAction action) {
        boolean isOk = false;
        synchronized (lockHelper) {
            if (!StringHelper.isNullOrEmpty(token) && extras.containsKey(token)) {
                switch (action.getType()) {
                    case First:
                        isOk = signAsfirst(token);
                        break;
                    case Leave:
                        isOk = leave(token);
                        break;
                    case GiveUp:
                        isOk = giveUp(token);
                        break;
                    case Ready:
                        isOk = ready(token);
                        break;
                    case Input:
                        isOk = recordInput(token, action);
                        break;
                    case Undo:
                        if (!token.equals(active.token)) break;
                        isOk = undoRecord(token, action);
                        break;
                    case AllowUndo:
                        break;
                    default:
                        break;
                }
            }
        }
        return isOk;
    }

    public int getBoxId(String token) {
        if (extras.containsKey(token)) {
            return extras.get(token).boxId;
        }
        return -1;
    }

    private boolean isEnded() {
        return dataBox().getReachableCount() == 0;
    }

    private void raiseJudged(String token, JudgeCode code, Object extra) {
        synchronized (judgeLockHelper) {
            if (judgedListener != null)
                judgedListener.onJudged(token, code, extra);
        }
    }

    private void raiseJudged(String token, JudgeCode code) {
        synchronized (judgeLockHelper) {
            if (judgedListener != null)
                judgedListener.onJudged(token, code, null);
        }
    }

    private boolean undoRecord(String token, InputAction action) {
        int boxId = getBoxId(token);
        if (boxId > 0) {
            DataPoint p;
            do {
                p = dataBox().undo();
                if (p != null) {
                    action.setData(new IntPoint(p.getX(), p.getY()));
                    raiseJudged(token, JudgeCode.Undo, action);
                }
            } while (p != null && p.getData() != boxId);
            if (p != null && p.getData() == boxId)
                return true;
        }
        return false;
    }

    private boolean recordInput(String token, InputAction action) {
        boolean isOk = false;
        if (isStarted && active.token.equals(token)) {
            isOk = recordToBox(token, action.getData(), extras.get(token).boxId);
            if (isOk) {
                raiseJudged(token, JudgeCode.Input, action);
                if (isNewWinnerAppend()) {
                    raiseJudged(token, JudgeCode.NewWinner);
                    end();
                } else {
                    if (isEnded()) end();
                    else tryActive(extras.get(token).next);
                }
            }
            System.out.println("handInput");
        }
        return isOk;
    }

    private boolean recordToBox(String token, Object data, int mark) {
        IntPoint p = (IntPoint) data;
        if (p != null) {
            boolean isOk = dataBox().record(p.getX(), p.getY(), mark);
            if (isOk) {
                Iterator<Point3Line> lines = getMap().linesOf(p);
                for (; lines.hasNext(); ) {
                    Point3Line line = lines.next();
                    IntPoint[] points = line.toPoints();
                    int count = 0;
                    for (IntPoint point : points) {
                        if (dataBox().isDataIn(point.getX(), point.getY(), mark))
                            ++count;
                    }
                    if (count == 3) {
                        winner = extras.get(token);
                        break;
                    }
                }
            }
            return isOk;
        }
        return false;
    }

    private void onWinnerAppend(String token) {
        winner = extras.get(token);
        if (isStarted) end();
        raiseJudged(token, JudgeCode.NewWinner);
    }

    private void tryActive(DataExtra extra) {
        if (active != null) {
            active.isActive = false;
            extras.get(active.token).watcher.pause();
        }
        active = extra;
        active.isActive = true;
        extras.get(active.token).watcher.ensure();
        raiseJudged(extra.token, JudgeCode.Active);
    }

    private boolean isNewWinnerAppend() {
        return winner != null;
    }

    private class DataExtra {
        int boxId;
        String token;
        @SuppressWarnings("unused")
        boolean isActive;
        boolean isReady;
        DataExtra next;
        DataExtra front;
        Watcher watcher;
    }

    private class Watcher implements IDisposable {
        private StopWatch stopwatch;

        @SuppressWarnings("unused")
        public long getTime() {
            return stopwatch.getTime();
        }

        void ensure() {
            if (stopwatch == null) {
                stopwatch = new StopWatch();
                stopwatch.start();
            }
            if (stopwatch.isStopped())
                stopwatch.reset();
            else if (stopwatch.isSuspended())
                stopwatch.resume();
        }

        void pause() {
            if (stopwatch != null)
                stopwatch.suspend();
        }

        void reset() {
            if (stopwatch != null) {
                if (stopwatch.isStarted())
                    stopwatch.stop();
                stopwatch.reset();
            }
        }

        void clear() {
            if (stopwatch != null)
                stopwatch.stop();
            stopwatch = null;
        }

        @Override
        public void dispose() {
            clear();
        }
    }
}
