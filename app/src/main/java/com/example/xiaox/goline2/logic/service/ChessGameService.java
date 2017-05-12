package com.example.xiaox.goline2.logic.service;

import com.example.xiaox.goline2.extension.event.EventArgs;
import com.example.xiaox.goline2.extension.event.EventWrapper;
import com.example.xiaox.goline2.extension.event.WeakEvent;
import com.example.xiaox.goline2.extension.helper.Filter;
import com.example.xiaox.goline2.extension.helper.Logger;
import com.example.xiaox.goline2.extension.helper.Match;
import com.example.xiaox.goline2.extension.helper.ReadWriteProperty;
import com.example.xiaox.goline2.extension.helper.ReadProperty;
import com.example.xiaox.goline2.extension.helper.StopWatch;
import com.example.xiaox.goline2.extension.helper.Token;
import com.example.xiaox.goline2.logic.interfaces.IChessBoardView;
import com.example.xiaox.goline2.logic.player.AIPlayer;
import com.example.xiaox.goline2.logic.basic.ActionType;
import com.example.xiaox.goline2.logic.player.analyzer.AlphaBetaMaxMinAnalyzer;
import com.example.xiaox.goline2.logic.basic.ChessGameMode;
import com.example.xiaox.goline2.logic.basic.MapStruct;
import com.example.xiaox.goline2.logic.player.Player;
import com.example.xiaox.goline2.logic.basic.Point;
import com.example.xiaox.goline2.logic.basic.RequestResult;
import com.example.xiaox.goline2.logic.basic.Spot;
import com.example.xiaox.goline2.logic.basic.SpotDataLogger;
import com.example.xiaox.goline2.logic.basic.XY3Line;
import com.example.xiaox.goline2.logic.interfaces.IChessGameService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.WeakHashMap;

/**
 * Created by xiaox on 1/24/2017.
 */
public class ChessGameService implements IChessGameService {

    private ReadProperty<WeakHashMap<Player, LoggedInfo>> allLogged;
    private ReadWriteProperty<GameLogicSettings> settings;
    public ReadProperty<SpotDataLogger> spotDataLogger;
    public ReadProperty<List<String>> readyList;
    private ReadWriteProperty<Boolean> isGameStarted;
    private ReadWriteProperty<ChessGameState> state;
    private ReadWriteProperty<Player> startPlayer;
    private ReadWriteProperty<Player> activatedPlayer;
    private ReadWriteProperty<Player> winner;
    private ReadProperty<StopWatch> runStopWatch;
    private ReadWriteProperty<IChessBoardView> boardView;
    private Player firstPlayer;
    private Player lastPlayer;

    public ChessGameService() {
        this.init();
    }

    private void init() {
        this.state = new ReadWriteProperty<>(ChessGameState.Ended);
        this.settings = new ReadWriteProperty<>();
        this.spotDataLogger = new ReadProperty<>(new SpotDataLogger(MapStruct.chessBoard));
        this.allLogged = new ReadProperty<>(new WeakHashMap<Player, LoggedInfo>());
        this.readyList = new ReadProperty<List<String>>(new ArrayList<String>());
        this.isGameStarted = new ReadWriteProperty<>(false);
        this.startPlayer = new ReadWriteProperty<>();
        this.activatedPlayer = new ReadWriteProperty<>();
        this.winner = new ReadWriteProperty<>();
        this.runStopWatch = new ReadProperty<>(new StopWatch());
        this.boardView = new ReadWriteProperty<>();

        //Init property
        this.settings.set(new GameLogicSettings(2, 12, ChessGameMode.PVPLocal, 1000l));
    }

    private void resetData(boolean resetAll){
        this.isGameStarted.set(false);
        this.spotDataLogger.get().reset();
        this.activatedPlayer.set(null);
        this.winner.set(null);
        this.runStopWatch.get().reset();
        if(resetAll){
            this.readyList.get().clear();
            this.allLogged.get().clear();
            this.startPlayer.set(null);
        }else{
            this.start();
        }
    }

    public void generateAnalyzer(AIPlayer aiPlayer){
        if(aiPlayer.front.get() != null){
            int denied = SpotDataLogger.UNREACHABLE;
            int reachable = SpotDataLogger.REACHABLE;
            int uid = this.allLogged.get().get((aiPlayer)).uid.get();
            int human = this.allLogged.get().get(aiPlayer.front.get()).uid.get();
            aiPlayer.analyzer = new AlphaBetaMaxMinAnalyzer(denied, reachable, human, uid);
        }
    }

    /******** Method group start ***********/
    /*********************
     * Player operation **
     *********************/

    @Override
    public boolean join(Player player) {
        if (isGameStarted.get() == true) return false;
        Set<Player> players = this.allLogged.get().keySet();
        if (players.size() >= this.settings.get().playerLimits.get()) {
            //Out of player limits
            return false;
        }
        SpotDataLogger logger = this.spotDataLogger.get();
        //Add player
        int uid = logger.generateMark(players.size());
        String token = Token.shortToken(4);
        player.token.set(token);
        //Update link for first and last
        //Link new join
        if(firstPlayer == null) firstPlayer = player;
        if(lastPlayer == null) lastPlayer = player;
        firstPlayer.front.set(player);
        lastPlayer.next.set(player);
        player.front.set(lastPlayer);
        player.next.set(firstPlayer);
        lastPlayer = player;
        //Register
        //Add to all logged
        player.registerService(this);
        WeakHashMap<Player, LoggedInfo> allLogged = this.allLogged.get();
        allLogged.put(player, new LoggedInfo(token, uid));
        return true;
    }

    @Override
    public boolean leave(Player player) {
        if (isGameStarted.get() == true) return false;
        if (this.allLogged.get().containsKey(player)) {
            this.allLogged.get().remove(player);
            //Adjust front and next link
            Player frontPlayer = player.front.get();
            Player nextPlayer = player.next.get();
            frontPlayer.next.set(nextPlayer);
            nextPlayer.front.set(frontPlayer);
            //Clean link for leaved player
            player.front.set(null);
            player.next.set(null);
            //Unregister
            player.unRegisterService();
            return true;
        }
        return false;
    }

    @Override
    public boolean setBeginner(Player player) {
        if (isGameStarted.get() == true) return false;
        this.startPlayer.set(player);
        return true;
    }

    @Override
    public Player getBeginner() {
        return this.startPlayer.get();
    }

    @Override
    public Player getActivatedPlayer() {
        return this.activatedPlayer.get();
    }

    @Override
    public Player getNextPlayer() {
        Player player = this.activatedPlayer.get();
        if (player != null) return player.next.get();
        return null;
    }

    @Override
    public Player getFrontPlayer() {
        Player player = this.activatedPlayer.get();
        if (player != null) return player.front.get();
        return null;
    }

    @Override
    public boolean isTurnOf(Player player) {
        if (isGameStarted.get() == false) return false;
        return this.activatedPlayer.get() == player;
    }

    @Override
    public Stack<Point> getInputsOf(Player player) {
        WeakHashMap<Player, LoggedInfo> allLogged = this.allLogged.get();
        if (allLogged.containsKey(player)) {
            int uid = allLogged.get(player).uid.get();
            return this.spotDataLogger.get().getInputsOf(uid);
        }
        return null;
    }

    /******** Method group end **************/


    /******** Method group start ***********/
    /***************
     * Run control *
     ***************/
    @Override
    public boolean start() {
        if (this.isGameStarted.get() == true) return false;
        if(this.readyList.get().size() != this.allLogged.get().size()) return false;
        this.isGameStarted.set(true);
        this.raiseGameStartingEvent();
        Collection<LoggedInfo> allLoggedValues = this.allLogged.get().values();
        for (LoggedInfo logged : allLoggedValues) {
            logged.stopwatch.set(new StopWatch());
        }
        this.runStopWatch.get().start();
        this.raiseGameStartedEvent();
        this.active(this.startPlayer.get());
        return true;
    }

    private boolean stopInternal(boolean isInterrupt) {
        if (this.isGameStarted.get() == false) return false;
        this.runStopWatch.get().stop();
        this.isGameStarted.set(false);
        Collection<LoggedInfo> allLoggedValues = this.allLogged.get().values();
        for (LoggedInfo logged : allLoggedValues) {
            logged.stopwatch.get().stop();
        }
        if(isInterrupt)this.raiseGameInterruptEndedEvent(isInterrupt);
        return true;
    }

    @Override
    public boolean stop() {
        return stopInternal(true);
    }

    @Override
    public boolean restart() {
        if (isGameStarted.get() == false) return false;
        this.spotDataLogger.get().reset();
        if (this.startPlayer.get() == null) return false;//Wrong operation case
        //Active start player
        this.isGameStarted.set(true);
        this.raiseGameStartingEvent();
        Collection<LoggedInfo> allLoggedValues = this.allLogged.get().values();
        for (LoggedInfo logged : allLoggedValues) {
            logged.stopwatch.get().reset();
        }
        this.runStopWatch.get().reset();
        this.runStopWatch.get().start();
        this.raiseGameStartedEvent();
        this.active(this.startPlayer.get());
        return true;
    }

    @Override
    public boolean reset() {
        this.spotDataLogger.get().reset();
        this.settings.get().mode.set(ChessGameMode.PVPLocal);
        this.runStopWatch.get().reset();
        this.isGameStarted.set(false);
        this.readyList.get().clear();
        this.allLogged.get().clear();
        this.activatedPlayer.set(null);
        this.startPlayer.set(null);
        this.winner.set(null);
        this.raiseResetEvent();
        return true;
    }

    /********
     * Method group end
     **************/

    private boolean active(Player player) {
        if (isGameStarted.get() == false || player == null) return false;
        Player oldPlayer = this.getActivatedPlayer();
        WeakHashMap<Player, LoggedInfo> allLoggedValue = this.allLogged.get();
        //Suspend old stopwatch
        if(oldPlayer != null){
            LoggedInfo info = allLoggedValue.get(oldPlayer);
            if(info != null){
                StopWatch sw = info.stopwatch.get();
                if(sw.isStarted() && sw.isSuspended() == false ){
                    info.stopwatch.get().suspend();
                }
            }
        }
        this.activatedPlayer.set(player);
        //Start new stopwatch
        LoggedInfo newActiveLogged = allLoggedValue.get(player);
        if(newActiveLogged != null){
            StopWatch stopWatch = newActiveLogged.stopwatch.get();
            if(stopWatch.isStopped()){
                stopWatch.start();
            }else if(stopWatch.isSuspended()){
                    stopWatch.resume();
            }
        }
        //Raise event
        this.raiseActivatePlayerChangedEvent(oldPlayer, player);
        return true;
    }

    private boolean hasNewWinner(Point inputPoint, int value) {
        List<XY3Line> lines = Filter.filter(MapStruct.GetMapStruct().xy3Lines, filterInLine(inputPoint.x, inputPoint.y));
        for (XY3Line line : lines) {
            Point[] points = line.toPoints();
            int count = this.spotDataLogger.get().countOccupiedOf(value, points);
            System.out.println("Check new winner count " + count);
            if (count == 3) return true;
        }
        return false;
    }

    private Match filterInLine(final int x, final int y) {
        return new Match<XY3Line>() {
            @Override
            public boolean validate(XY3Line xy3Line) {
                return xy3Line.isInLine(x, y);
            }
        };
    }

    /******** Method group start ***********/
    /************************
     * handAction override **
     ************************/

    @Override
    public boolean handAction(Player player, ActionType type) {
        return this.handAction(player, type, null);
    }

    @Override
    public boolean handAction(Player player, ActionType type, Object[] params) {
        if(player == null && this.allLogged.get().containsKey(player) == false) return false;
        boolean actionSuccess = false;
        switch (type) {
            case UnReady: actionSuccess = this.setUnReadyOf(player);break;
            case Ready: actionSuccess = this.setReadyOf(player); break;
            case Input: actionSuccess = this.handInput(player, params);break;
            case Undo: actionSuccess = this.handUndo(player); break;
            case Redo: {
                //TODO Add redo support if in need
                //Notice This chess game service is not support redo action now
            } break;
            default: break;
        }
        if(actionSuccess){
            this.raiseGameActionEvent(type, player, params);
        }
        return actionSuccess;
    }

    private boolean setUnReadyOf(Player player){
        if(this.isGameStarted.get() == false){
            String token = this.allLogged.get().get(player).token.get();
            if(readyList.get().contains(token)){
                readyList.get().remove(token);
                return true;
            }
        }
        return false;
    }

    private boolean setReadyOf(Player player){
        if (this.isGameStarted.get() == false) {
            String token = this.allLogged.get().get(player).token.get();
            if (this.readyList.get().contains(token) == false) {
                this.readyList.get().add(token);
                if (this.readyList.get().size() == this.allLogged.get().size()) {
                    this.start();
                }
                return true;
            }
        }
        return false;
    }

    private boolean handInput(Player player, Object[] params){
        //Check if game start and data logger can log data
        System.out.println("*************************************************");
        Logger.logLine("*************************************************");
        System.out.println("Check if game start and data logger can log data");
        Logger.logLine("Check if game start and data logger can log data");
        if (this.isGameStarted.get() && !this.spotDataLogger.get().isFill()) {
            //Check player and parameters
            Logger.logLine("Check player and parameters" );
            if (isTurnOf(player) && params != null && params.length == 2) {
                //Get stored player from token
                Logger.logLine("Get stored player from token");
                LoggedInfo loggedInfo = this.allLogged.get().get(player);
                if (loggedInfo != null) {
                    Logger.logLine("Get data to log");
                    //Get data to log
                    int uid = loggedInfo.uid.get();
                    Integer[] data = (Integer[]) params;
                    Point point = new Point(data[0], data[1]);
                    //Try to log value
                    Logger.logLine("Try to log value");
                    if (this.spotDataLogger.get().log(point.x, point.y, uid)) {
                        Logger.logLine("Logged of " + uid + "(" + point.x + "," + point.y + ")");
                        if (hasNewWinner(point, uid)) {
                            //Check if new winner append
                            Logger.logLine("New winner appended");
                            this.winner.set(player);
                            this.stopInternal(false);
                            this.raiseGameEndedWinnerEvent(player);
                        } else {
                            //Check if game ended
                            Logger.logLine("Check if game ended");
                            if (this.spotDataLogger.get().isFill()) {
                                this.stop();
                            } else {
                                //Active next player
                                Logger.logLine("Game not end, active next player");
                                active(player.next.get());
                            }
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean handUndo(Player player){
        SpotDataLogger logger = this.spotDataLogger.get();
        if (isGameStarted.get() && logger.canUndo()) {
            boolean notFound = true;
            boolean canContinue = true;
            LoggedInfo loggedPlayer = this.allLogged.get().get(player);
            int uid = loggedPlayer.uid.get();
            while (notFound && canContinue){
                Spot spot = logger.getUndoTop();
                if( uid == spot.data){
                    logger.undo();
                    notFound = false;
                }else{
                    Set<Player> players = this.allLogged.get().keySet();
                    Player canUndoPlayer = null;
                    for(Player _player : players){
                        if(this.allLogged.get().get(_player).uid.get() == spot.data){
                            canUndoPlayer = _player;
                            break;
                        }
                    }
                    if(canUndoPlayer != null){
                        //Ask for current player which can undo
                        //To undo when player accepted
                        RequestResult result = canUndoPlayer.receiveRequest(ActionType.Undo, new Object[]{spot.x, spot.y});
                        if(result == RequestResult.Accept){
                            logger.undo();
                        }else{
                            canContinue = false;
                        }
                    }else canContinue = false;
                }
            }
            if(notFound == false){
                active(player);
            }
            //If canContinue == false means other player refuse request
            return !notFound;
        }
        return false;
    }

    /******** Method group end **************/


    /******** Method group start ***********/
    /********************
     * Time operation ***
     ********************/

    @Override
    public long getRunningTime() {
        return this.runStopWatch.get().getTime();
    }

    @Override
    public long getUsedTimeOf(Player player) {
        LoggedInfo loggedInfo = this.allLogged.get().get(player);
        if(loggedInfo != null){
            return loggedInfo.stopwatch.get().getTime();
        }
        return 0l;
    }

    /******** Method group end **************/

    @Override
    public void detach(){
        this.clearHandlers();
    }

    @Override
    public ChessGameState getState() {
        return this.state.get();
    }

    /**************** Method group start ****************/

    /*****************************
     * Game event operations  ****
     *****************************/

    private WeakEvent<EventArgs> startingEvent = new WeakEvent<>();
    private WeakEvent<EventArgs> startedEvent = new WeakEvent<>();
    private WeakEvent<EventArgs> onResetEvent = new WeakEvent<>();
    private WeakEvent<GameStateEventArgs> stateChangedEvent = new WeakEvent<>();
    private WeakEvent<GameActionEventArgs> actionEvent = new WeakEvent<>();
    private WeakEvent<GameEndedEventArgs> endedEvent = new WeakEvent<>();
    private WeakEvent<ActivatedChangedEventArgs> activatedChangedEvent = new WeakEvent<>();

    public EventWrapper<EventArgs> onStarting = EventWrapper.wrap(startingEvent);
    public EventWrapper<EventArgs> started = EventWrapper.wrap(startedEvent);
    public EventWrapper<EventArgs> onReset = EventWrapper.wrap(onResetEvent);
    public EventWrapper<GameStateEventArgs> onStateChanged = EventWrapper.wrap(stateChangedEvent);
    public EventWrapper<GameActionEventArgs> onAction = EventWrapper.wrap(actionEvent);
    public EventWrapper<GameEndedEventArgs> onEnded = EventWrapper.wrap(endedEvent);
    public EventWrapper<ActivatedChangedEventArgs> onActivatedChanged = EventWrapper.wrap(activatedChangedEvent);

    private void raiseStateChangedEvent(ChessGameState oldChessGameState, ChessGameState newChessGameState) {
        this.stateChangedEvent.raiseEvent(this, new GameStateEventArgs(oldChessGameState, newChessGameState));
    }

    private void raiseResetEvent(){
        this.onResetEvent.raiseEvent(this, EventArgs.empty);
    }

    private void raiseGameStartingEvent() {
        this.startingEvent.raiseEvent(this, EventArgs.empty);
    }

    private void raiseGameStartedEvent() {
        ChessGameState oldState = this.state.get();
        this.state.set(ChessGameState.Started);
        raiseStateChangedEvent(oldState, this.state.get());
        this.startedEvent.raiseEvent(this, EventArgs.empty);
    }

    private void raiseGameInterruptEndedEvent(boolean isInterrupt) {
        ChessGameState oldState = this.state.get();
        this.state.set(ChessGameState.Ended);
        this.raiseStateChangedEvent(oldState, this.state.get());
        this.endedEvent.raiseEvent(this, new GameEndedEventArgs(isInterrupt));
    }

    private void raiseGameEndedWinnerEvent(Player player) {
        ChessGameState oldState = this.state.get();
        this.state.set(ChessGameState.Ended);
        this.raiseStateChangedEvent(oldState, this.state.get());
        this.endedEvent.raiseEvent(this, new GameEndedEventArgs(player));
    }

    private void raiseGameActionEvent(ActionType type, Player player, Object[] params) {
        this.actionEvent.raiseEvent(this, new GameActionEventArgs(type, player, params));
    }

    private void raiseActivatePlayerChangedEvent(Player previousPlayer, Player activatedPlayer) {
        this.activatedChangedEvent.raiseEvent(this,
                new ActivatedChangedEventArgs(previousPlayer, activatedPlayer));
    }

    public void clearHandlers(){
        startingEvent.clearHandlers();
        stateChangedEvent.clearHandlers();
        actionEvent.clearHandlers();
        endedEvent.clearHandlers();
        activatedChangedEvent.clearHandlers();
    }

    /**************** Method group end ****************/


    /**
     * <p/>
     * This class for store logic settings of service
     * </P>
     */
    class GameLogicSettings {
        public ReadWriteProperty<Integer> playerLimits;
        public ReadWriteProperty<Integer> maxSteps;
        public ReadWriteProperty<Integer> mode;
        public ReadWriteProperty<Long> turnTimeLimit;

        public GameLogicSettings() {
            this.init();
        }

        public GameLogicSettings(int playerLimits, int maxSteps, int mode, long turnTimeLimit) {
            this();
            this.turnTimeLimit.set(turnTimeLimit);
            this.playerLimits.set(playerLimits);
            this.maxSteps.set(maxSteps);
            this.mode.set(mode);
        }

        private void init() {
            this.playerLimits = new ReadWriteProperty<>();
            this.maxSteps = new ReadWriteProperty<>();
            this.mode = new ReadWriteProperty<>();
            this.turnTimeLimit = new ReadWriteProperty<>();
        }
    }

    /**
     * <p/>
     * This class for log player information who is logged in.
     * </P>
     */
    class LoggedInfo {
        public ReadWriteProperty<String> token;
        public ReadWriteProperty<Integer> uid;
        public ReadWriteProperty<Date> tokenFrom;
        public ReadWriteProperty<Date> tokenEnd;
        public ReadWriteProperty<Long> beginTime;
        public ReadWriteProperty<Long> usedTime;
        public ReadWriteProperty<StopWatch> stopwatch;

        public LoggedInfo() {
            this.init();
        }

        public LoggedInfo(String token, int uid) {
            this();
            this.token.set(token);
            this.uid.set(uid);
        }

        public LoggedInfo(String token, int uid, Date begin, java.util.Date end) {
            this(token, uid);
            this.tokenFrom.set(begin);
            this.tokenEnd.set(end);
        }

        private void init() {
            this.token = new ReadWriteProperty<>();
            this.uid = new ReadWriteProperty<>();
            this.tokenFrom = new ReadWriteProperty<>();
            this.tokenEnd = new ReadWriteProperty<>();
            this.beginTime = new ReadWriteProperty<>();
            this.usedTime = new ReadWriteProperty<>();
            this.stopwatch = new ReadWriteProperty<>();
        }

    }

    /************************************************/
}
