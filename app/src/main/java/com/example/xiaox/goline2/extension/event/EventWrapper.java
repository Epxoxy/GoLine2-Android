package com.example.xiaox.goline2.extension.event;

import java.lang.ref.WeakReference;

/**
 * Created by xiaox on 2/3/2017.
 */
public class EventWrapper<TEventArgs extends EventArgs> {

    private WeakReference<IEvent<TEventArgs>> eventRef;

    public static <TEventArgs extends  EventArgs> EventWrapper<TEventArgs> wrap(IEvent<TEventArgs> event){
        return new EventWrapper<TEventArgs>(event);
    }

    private EventWrapper(IEvent<TEventArgs> event){
        this.eventRef = new WeakReference<IEvent<TEventArgs>>(event);
    }

    public void addHandler(IEventHandler<TEventArgs> handler){
        if(this.eventRef.get() != null)
            this.eventRef.get().addHandler(handler);
    }

    public void removeHandler(IEventHandler<TEventArgs> handler){
        if(this.eventRef.get() != null)
            this.eventRef.get().removeHandler(handler);
    }

    public void clearHandlers(){
        if(this.eventRef.get() != null)
            this.eventRef.get().clearHandlers();
    }
}
