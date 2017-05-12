package com.example.xiaox.goline2.extension.event;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiaox on 2/3/2017.
 */
public class WeakEvent<TEventArgs extends EventArgs> implements IEvent<TEventArgs> {
    public WeakEvent() {
        this.handlers = new ArrayList<>();
    }

    @Override
    public void addHandler(IEventHandler<TEventArgs> handler){
        this.handlers.add(new WeakReference<IEventHandler<TEventArgs>>(handler));
    }

    @Override
    public void removeHandler(IEventHandler<TEventArgs> handler){
        List<WeakReference<IEventHandler<TEventArgs>>> rmRefList = new ArrayList<>();
        for(WeakReference<IEventHandler<TEventArgs>> wefHandler : handlers) {
            if(wefHandler.get() == handler){
                rmRefList.add(wefHandler);
            }else if(wefHandler.get() == null){
                rmRefList.add(wefHandler);
            }
        }
        clean(rmRefList);
    }

    @Override
    public void clearHandlers(){
        this.handlers.clear();
    }

    @Override
    public void raiseEvent(Object sender, TEventArgs args){
        List<WeakReference<IEventHandler<TEventArgs>>> rmRefList = new ArrayList<>();
        for(WeakReference<IEventHandler<TEventArgs>> handler : handlers) {
            if(handler == null || handler.get() == null) {
                rmRefList.add(handler);
                continue;
            };
            handler.get().onEvent(sender, args);
        }
        clean(rmRefList);
    }

    private void clean(List<WeakReference<IEventHandler<TEventArgs>>> rmRefList){
        if(rmRefList.size() < 1) return;
        for(WeakReference<IEventHandler<TEventArgs>> handler : rmRefList){
            this.handlers.remove(handler);
        }
    }

    private List<WeakReference<IEventHandler<TEventArgs>>> handlers;
}
