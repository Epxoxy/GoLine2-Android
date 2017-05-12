package com.example.xiaox.goline2.extension.event;

import com.example.xiaox.goline2.extension.event.EventArgs;

/**
 * Created by xiaox on 1/31/2017.
 */
public interface IEventHandler<TEventArgs extends EventArgs> {
    void onEvent(Object sender, TEventArgs args);
}
