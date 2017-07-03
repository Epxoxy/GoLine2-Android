package core.resolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import core.analyzer.AlphaBetaMaxMinAnalyzer;
import core.data.AILevel;
import core.data.ActionType;
import core.data.IntPoint;
import core.data.JudgeCode;
import core.helpers.StopWatch;
import core.helpers.TokenHelper;
import core.interfaces.IAnalyzer;
import core.interfaces.IBoard;
import core.interfaces.IMap;
import core.interfaces.LatticeClickListener;
import core.networkbase.LocalProxy;
import core.resolver.base.CoreResolverBase;
import core.resolver.base.ProxyEx;

class EVPResolver extends CoreResolverBase{
    private IAnalyzer<IMap, IntPoint> analyzer;
    private List<String> tokens;
    private LocalProxy shareProxy;
    private int deep = 20;

    EVPResolver(IBoard board, AILevel level){
        this.board = board;
        judges.onAttach();
        //Attach PROXY
        joinToken = TokenHelper.shortToken(10);
        hostToken = TokenHelper.shortToken(10);
        shareProxy = new LocalProxy(relayListener);
        shareProxy.setToken(TokenHelper.shortToken(10));
        judges.attachProxy(shareProxy);
        //Subscribe event
        this.board.setLatticeClickListener(latticeClickListener);
        //Generate token and store it.
        tokens = new ArrayList<String>(2);
        tokens.add(joinToken);
        tokens.add(hostToken);
        deep = level == AILevel.Intermediate ? 10
                : (level == AILevel.Elementary ? 1 : 20);
    }

    private LatticeClickListener latticeClickListener = new LatticeClickListener(){
        @Override
        public void onLatticeClick(int column, int row, int clickRadius) {
            if (!isStarted()) return;
            submit(hostToken, ActionType.Input, new IntPoint(column, row));
        }
    };

    @Override
    public void ready(){
        judges.enable();
        for (String token : tokens){
            submit(token, ActionType.Join, null);
        }
    }

    @Override
    public void start() {
        for (String token : tokens){
            submit(token, ActionType.Ready, null);
        }
    }


    @Override
    protected void afterBasicJudge(String token, JudgeCode code){
        if (code == JudgeCode.Active) {
            analysis(getActiveToken());
        }
    }

    @Override
    protected void submit(String token, ActionType type, Object data){
        ProxyEx.passActionByToken(shareProxy, token, type, data);
    }

    @Override
    public void onDispose(){
        ProxyEx.dispose(shareProxy);
    }

    @Override
    public void undo(){
        submit(getHostToken(), ActionType.Undo, null);
    }

    private void analysis(String active){
        if (active.equals(getJoinToken())){
            final IMap map = judges.getJudgeUnit().getMap();
            if (analyzer == null){
                analyzer = new AlphaBetaMaxMinAnalyzer(getJoinBoxId(), getHostBoxId());
            }
            Thread thread = new Thread(){
                @Override
                public void run(){
                    System.out.println("## Analysing ---<<<<<<<");
                    long delay = (new Random()).nextInt(500) + 200;
                    StopWatch sw = new StopWatch();
                    IntPoint p = null;
                    sw.start();
                    p = analyzer.analysis(map, deep);
                    sw.stop();
                    long used = sw.getTime();
                    if (used < delay){
                        try {
                            Thread.sleep(delay - used);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    System.out.println("Analysis result --> "+p);
                    submit(getJoinToken(), ActionType.Input, p);
                }
            };
            thread.start();
        }
    }


}