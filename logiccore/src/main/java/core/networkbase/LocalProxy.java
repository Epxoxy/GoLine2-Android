package core.networkbase;

import core.interfaces.RelayListener;

public class LocalProxy extends ProxyBase {
    private RelayListener listener;
    public LocalProxy(RelayListener listener){
        this.listener = listener;
    }

    @Override
    public void relay(Message msg) {
        if(listener != null) listener.onRelay(this, msg);
    }

    @Override
    protected void onEnabled() {
    }

    @Override
    protected void onDispose() {
        listener = null;
    }

    public void send(Message msg){
        super.fakeMessage(msg, getToken());
        bridgeOf(msg);
    }

    public void sendByToken(String token, Message msg){
        super.fakeMessage(msg, token);
        bridgeOf(msg);
    }

}
