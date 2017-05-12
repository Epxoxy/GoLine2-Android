package com.example.xiaox.goline2.extension.event;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiaox on 2/4/2017.
 */
public class Event<TEventArgs extends EventArgs> implements IEvent<TEventArgs> {
    public Event() {
        this.handlers = new ArrayList<>();
    }

    @Override
    public void addHandler(IEventHandler<TEventArgs> handler) {
        this.handlers.add(handler);
    }

    @Override
    public void removeHandler(IEventHandler<TEventArgs> handler){
        this.handlers.remove(handler);
    }

    @Override
    public void clearHandlers(){
        this.handlers.clear();
    }

    @Override
    public void raiseEvent(Object sender, TEventArgs args){
        for(IEventHandler<TEventArgs> handler : handlers) {
            if(handler == null) continue;;
            handler.onEvent(sender, args);
        }
    }

    private List<IEventHandler<TEventArgs>> handlers;
}
