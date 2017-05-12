package com.example.xiaox.goline2.logic.player;

import android.os.Handler;
import android.os.Message;

import com.example.xiaox.goline2.extension.event.Event;
import com.example.xiaox.goline2.extension.event.IEvent;
import com.example.xiaox.goline2.extension.event.EventWrapper;
import com.example.xiaox.goline2.extension.event.IEventHandler;
import com.example.xiaox.goline2.extension.helper.Logger;
import com.example.xiaox.goline2.extension.helper.StopWatch;
import com.example.xiaox.goline2.logic.basic.ActionType;
import com.example.xiaox.goline2.logic.basic.Point;
import com.example.xiaox.goline2.logic.basic.RequestResult;
import com.example.xiaox.goline2.logic.interfaces.IChessAnalyzer;
import com.example.xiaox.goline2.logic.service.ActivatedChangedEventArgs;
import com.example.xiaox.goline2.logic.service.ChessGameService;
import com.example.xiaox.goline2.logic.service.ServiceProviderChangedEventArgs;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by xiaox on 1/29/2017.
 */
public class AIPlayer extends Player {
    private int deep = 6;
    public int mark;
    private boolean isAI = true;
    private long shortUseTime = 1000;
    public IChessAnalyzer<Point> analyzer;
    private IEvent<ServiceProviderChangedEventArgs> onServiceProviderChangedEvent = new Event<>();
    public EventWrapper<ServiceProviderChangedEventArgs> onServiceProviderChanged = EventWrapper.wrap(onServiceProviderChangedEvent);

    public void setDeep(int deep){
        this.deep = deep;
    }

    @Override
    public boolean isAI(){
        return this.isAI;
    }

    @Override
    public RequestResult onRequest(ActionType type, Object[] params){
        return RequestResult.Accept;
    }

    @Override
    protected void onServiceProviderChanged(ChessGameService oldProvider, ChessGameService newProvider){
        if(oldProvider != null) {
            oldProvider.onActivatedChanged.removeHandler(onActivatedChanged);
        }
        if(onActivatedChanged == null){
            onActivatedChanged = new IEventHandler<ActivatedChangedEventArgs>() {
                @Override
                public void onEvent(Object sender, ActivatedChangedEventArgs args) {
                        if(args.getActivatedPlayer() == AIPlayer.this){
                            //TODO get next step
                            if(AIPlayer.this.analyzer == null){
                                AIPlayer.this.serviceProvider.generateAnalyzer(AIPlayer.this);
                            }
                            new Thread(){
                                @Override
                                public void run(){
                                    StopWatch stopWatch = StopWatch.createStarted();
                                    int[][] board  = AIPlayer.this.serviceProvider.spotDataLogger.get().copyEntry();
                                    AIPlayer.this.foundPoint = analyzer.analysis(board, AIPlayer.this.deep);
                                    stopWatch.stop();
                                    final long delayTime = shortUseTime - stopWatch.getTime();
                                    if(delayTime > 0){
                                        Logger.logLine("Already found and delay input with -> " + delayTime + "millis");
                                        inputHandler.sendEmptyMessageDelayed(ANALYZER_FOUND, delayTime);
                                    }else{
                                        Logger.logLine("Out of short time with -> " + stopWatch.getTime() + "millis");
                                        inputHandler.sendEmptyMessage(ANALYZER_FOUND);
                                    }
                                }
                            }.start();
                    }
                }
            };
        }
        newProvider.onActivatedChanged.addHandler(onActivatedChanged);
        onServiceProviderChangedEvent.raiseEvent(this, new ServiceProviderChangedEventArgs(oldProvider, newProvider));
    }

    private Point foundPoint = null;
    private static final int ANALYZER_FOUND = 0;
    private Handler inputHandler = new Handler(){
        public void handleMessage(Message message){
            switch (message.what){
                case ANALYZER_FOUND:
                    if(foundPoint != null){
                        AIPlayer.this.input(foundPoint.x, foundPoint.y);
                        AIPlayer.this.foundPoint = null;
                    }
                default:break;
            }
        }
    };

    private IEventHandler<ActivatedChangedEventArgs> onActivatedChanged;
}
