package per.epxoxy.event;

/**
 * Created by xiaox on 1/31/2017.
 */
public interface IEvent<TEventArgs extends EventArgs>  {

    void addHandler(IEventHandler<TEventArgs> handler);

    void removeHandler(IEventHandler<TEventArgs> handler);

    void clearHandlers();

    void raiseEvent(Object sender, TEventArgs args);
}
