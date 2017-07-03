package core.interfaces;

import core.networkbase.Message;

public abstract interface RelayListener {
    abstract void onRelay(Object obj, Message msg);
}
