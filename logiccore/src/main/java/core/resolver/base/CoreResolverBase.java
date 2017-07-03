package core.resolver.base;

import core.analyzer.AlphaBetaMaxMinAnalyzer;
import core.data.ActionType;
import core.data.IntPoint;
import core.data.JudgeCode;
import core.interfaces.IAnalyzer;
import core.interfaces.IBoard;
import core.interfaces.IDisposable;
import core.interfaces.IGameCoreResolver;
import core.interfaces.IMap;
import core.interfaces.JudgedLiteListener;
import core.interfaces.RelayListener;
import core.judge.Judges;
import core.networkbase.Message;

public abstract class CoreResolverBase implements IGameCoreResolver, IJudgeResolver, IDisposable{
    private JudgedLiteListener stateListener;
    private JudgeCodeFilter filter;
    private boolean isUseful;
    private int hostBoxId;
    private int joinBoxId;
    protected Judges judges;
    protected IBoard board;
    protected String hostToken;
    protected String joinToken;

    public CoreResolverBase(){
        judges = new Judges();
        filter = new JudgeCodeFilter(this);
    }

    @Override
    public void reset(){
        judges.reset();
    }

    @Override
    public void setFirst(String token) {
        submit(token, ActionType.First, null);
    }

    @Override
    public void onBasicJudge(String token, JudgeCode code){
        if (code == JudgeCode.Joined){
            int boxId = judges.getBoxId(token);
            if (hostToken!=null && token.equals(hostToken))
                hostBoxId = boxId;
            else if(joinToken!=null && token.equals(joinToken))
                joinBoxId = boxId;
        }
        afterBasicJudge(token, code);
    }

    @Override
    public void onAction(ActionType type, Object[] content){
    }


    @Override
    public void onJudgeInput(String token, JudgeCode code, IntPoint p){
        if(code == JudgeCode.Input){
            if(board!=null)
                board.drawChess(p.x, p.y, isHostActive());
        }
        else if(code == JudgeCode.Undo){
            if(board!=null)
                board.removeChess(p.x, p.y);
        }
    }

    @Override
    public void onStateChanged(JudgeCode code){
        switch (code){
            case Reset:
                isUseful = true;
                break;
            case Started:
                isUseful = true;
                if(board != null)
                    board.clearChess();
                break;
            case Ended:
                isUseful = false;
                break;
            default:break;
        }
        if(stateListener != null)
            stateListener.onJudged(code);
    }

    protected RelayListener relayListener = new RelayListener(){
        @Override
        public void onRelay(Object obj, Message msg) {
            CoreResolverBase.this.requireRelay(obj, msg);
        }
    };

    protected void afterBasicJudge(String token, JudgeCode code){

    }

    protected void submit(String token, ActionType type, Object data) {

    }

    protected void requireRelay(Object sender, Message msg){
        filter.handRelay(sender, msg);
    }

    protected void onDispose(){

    }

    protected int getHostBoxId(){
        return this.hostBoxId;
    }

    protected int getJoinBoxId(){
        return this.joinBoxId;
    }

    public IntPoint tips(){
        boolean isHostActive = isHostActive();
        int enemyId = isHostActive ? joinBoxId : hostBoxId;
        int activeId = isHostActive ? hostBoxId : joinBoxId;
        return analysisTips(activeId, enemyId);
    }

    private IntPoint analysisTips(int userId, int enemyId){
        IMap map = judges.getJudgeUnit().getMap();
        //User as environment, other part as user
        //Find a good result
        IAnalyzer<IMap, IntPoint> tipsAnalyzer = new AlphaBetaMaxMinAnalyzer(userId, enemyId);
        IntPoint p = null;
        int deep = 10;
        p = tipsAnalyzer.analysis(map, deep);
        return p;
    }

    @Override
    public void dispose() {
        onDispose();
    }

    @Override
    public boolean isUseful() {
        return this.isUseful;
    }

    @Override
    public boolean isStarted() {
        return filter.isStarted();
    }

    @Override
    public boolean isHostActive() {
        return hostToken!=null && filter.active.equals(hostToken);
    }

    @Override
    public String getHostToken() {
        return hostToken;
    }

    @Override
    public String getJoinToken() {
        return joinToken;
    }

    @Override
    public String getWinnerToken() {
        return filter.winner;
    }

    @Override
    public String getActiveToken() {
        return filter.active;
    }

    @Override
    public String getFirstToken(){return filter.first;}

    @Override
    public void setJudgedListener(JudgedLiteListener listener) {
        this.stateListener = listener;
    }
}
