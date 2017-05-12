package per.epxoxy.event;

import java.lang.ref.WeakReference;

/**
 * Created by xiaox on 2/20/2017.
 */
public class EventWeakWrapper <TEventArgs extends EventArgs> {

    private WeakReference<IEvent<TEventArgs>> eventRef;

    public static <TEventArgs extends EventArgs> EventWeakWrapper<TEventArgs> wrap(IEvent<TEventArgs> event){
        return new EventWeakWrapper<TEventArgs>(event);
    }

    private EventWeakWrapper(IEvent<TEventArgs> event){
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

