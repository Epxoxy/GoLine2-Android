package per.epxoxy.event;

import java.lang.ref.WeakReference;

/**
 * Created by xiaox on 2/3/2017.
 */
public class EventWrapper<TEventArgs extends EventArgs> {

    private IEvent<TEventArgs> event;

    public static <TEventArgs extends EventArgs> EventWrapper<TEventArgs> wrap(IEvent<TEventArgs> event){
        return new EventWrapper<TEventArgs>(event);
    }

    private EventWrapper(IEvent<TEventArgs> event){
        this.event = event;
    }

    public void addHandler(IEventHandler<TEventArgs> handler){
        if(this.event != null)
            this.event.addHandler(handler);
    }

    public void removeHandler(IEventHandler<TEventArgs> handler){
        if(this.event != null)
            this.event.removeHandler(handler);
    }

    public void clearHandlers(){
        if(this.event != null)
            this.event.clearHandlers();
    }
}
