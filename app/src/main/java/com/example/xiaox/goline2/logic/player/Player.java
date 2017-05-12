package com.example.xiaox.goline2.logic.player;

import com.example.xiaox.goline2.extension.event.Event;
import com.example.xiaox.goline2.extension.event.IEvent;
import com.example.xiaox.goline2.extension.event.EventWrapper;
import com.example.xiaox.goline2.extension.helper.ReadProperty;
import com.example.xiaox.goline2.extension.helper.ReadWriteProperty;
import com.example.xiaox.goline2.logic.basic.ActionType;
import com.example.xiaox.goline2.logic.basic.Point;
import com.example.xiaox.goline2.logic.basic.RequestResult;
import com.example.xiaox.goline2.logic.interfaces.IPlayer;
import com.example.xiaox.goline2.logic.service.ChessGameService;
import com.example.xiaox.goline2.logic.service.GameActionEventArgs;
import com.example.xiaox.goline2.logic.service.RequestEventArgs;

import java.util.Stack;

/**
 * Created by xiaox on 1/24/2017.
 */
public class Player implements IPlayer{
    public ReadWriteProperty<String> token;
    public ReadWriteProperty<String> name;
    public ReadWriteProperty<Player> next;
    public ReadWriteProperty<Player> front;
    public ReadWriteProperty<Integer> color;
    protected ChessGameService serviceProvider;
    private boolean isAI = false;
    private Event<RequestEventArgs> onRequestEvent = new Event<>();
    public EventWrapper<RequestEventArgs> onRequest = EventWrapper.wrap(onRequestEvent);

    public Player(){
        init();
    }

    public Player(String name){
        this();
        this.name.set(name);
    }

    public Player(ChessGameService gameService){
        this();
        registerService(gameService);
    }

    public Player(ChessGameService gameService, String name){
        this(gameService);
        this.name.set(name);
    }

    private void init(){
        this.token = new ReadWriteProperty<>();
        this.name = new ReadWriteProperty<>();
        this.next = new ReadWriteProperty<>();
        this.front = new ReadWriteProperty<>();
        this.color = new ReadWriteProperty<>();
    }

    public boolean isAI(){
        return this.isAI;
    }

    public void registerService(ChessGameService gameService){
        ChessGameService oldProvider = this.serviceProvider;
        if(oldProvider == gameService) return;
        this.serviceProvider = gameService;
        this.onServiceProviderChanged(oldProvider, gameService);
    }

    public void unRegisterService(){
        ChessGameService oldProvider = this.serviceProvider;
        this.serviceProvider = null;
        this.onServiceProviderChanged(oldProvider, null);
    }

    public boolean isMyTurn(){
        if(serviceProvider == null) return false;
        return serviceProvider.isTurnOf(this);
    }

    public Stack<Point> getInputs(){
        if(serviceProvider == null) return null;
        return serviceProvider.getInputsOf(this);
    }

    protected void onServiceProviderChanged(ChessGameService oldProvider, ChessGameService newProvider){

    }

    public boolean input(int x, int y){
        if(this.serviceProvider == null) return false;
        return this.serviceProvider.handAction(this, ActionType.Input, new Integer[]{x, y});
    }

    public boolean sendAction(ActionType action){
        if(this.serviceProvider == null) return false;
        return this.serviceProvider.handAction(this, action);
    }

    public RequestResult receiveRequest(ActionType type, Object[] params){
        return onRequest(type, params);
    }

    protected RequestResult onRequest(ActionType type, Object[] params){
        RequestEventArgs args = new RequestEventArgs(type, this);
        onRequestEvent.raiseEvent(this, args);
        return args.getHandResult();
    }
}
