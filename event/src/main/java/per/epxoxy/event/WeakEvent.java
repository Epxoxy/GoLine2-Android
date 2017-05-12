package per.epxoxy.event;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiaox on 2/3/2017.
 */
public class WeakEvent<TEventArgs extends EventArgs> implements per.epxoxy.event.IEvent<TEventArgs> {
    public WeakEvent() {
        this.handlersRef = new ArrayList<>();
    }

    @Override
    public void addHandler(per.epxoxy.event.IEventHandler<TEventArgs> handler){
        this.handlersRef.add(new WeakReference<per.epxoxy.event.IEventHandler<TEventArgs>>(handler));
    }

    @Override
    public void removeHandler(per.epxoxy.event.IEventHandler<TEventArgs> handler){
        List<WeakReference<per.epxoxy.event.IEventHandler<TEventArgs>>> rmRefList = new ArrayList<>();
        for(WeakReference<per.epxoxy.event.IEventHandler<TEventArgs>> wefHandler : handlersRef) {
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
        this.handlersRef.clear();
    }

    @Override
    public void raiseEvent(Object sender, TEventArgs args){
        List<WeakReference<per.epxoxy.event.IEventHandler<TEventArgs>>> rmRefList = new ArrayList<>();
        for(WeakReference<per.epxoxy.event.IEventHandler<TEventArgs>> handlerRef : handlersRef) {
            if(handlerRef == null ) {
                rmRefList.add(handlerRef);
                continue;
            };
            IEventHandler<TEventArgs> handler = handlerRef.get();
            if(handler == null ) {
                rmRefList.add(handlerRef);
                continue;
            };
            handler.onEvent(sender, args);
        }
        clean(rmRefList);
    }

    private void clean(List<WeakReference<per.epxoxy.event.IEventHandler<TEventArgs>>> rmRefList){
        if(rmRefList.size() < 1) return;
        for(WeakReference<per.epxoxy.event.IEventHandler<TEventArgs>> handlerRef : rmRefList){
            this.handlersRef.remove(handlerRef);
        }
    }

    public int sizeOfHandlers(){
        return this.handlersRef.size();
    }

    private List<WeakReference<per.epxoxy.event.IEventHandler<TEventArgs>>> handlersRef;
}
