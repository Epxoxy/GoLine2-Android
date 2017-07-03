package core.networkbase;

import core.helpers.StringHelper;

public class RemoteProxy extends ProxyBase{
    private IConnector connector;
    private String remoteToken;

    public RemoteProxy(IConnector connector){
        this.connector = connector;
        this.connector.setOnMessageListener(listener);
    }

    public boolean isConnected(){
        return connector.isConnected();
    }

    private OnMessageListener listener = new OnMessageListener(){
        @Override
        public void onMessage(Message msg) {
            onMessageReceived(msg);
        }
    };

    public RemoteProxy enableOut(String ip, int port){
        connector.connect(ip, port);
        return this;
    }

    public RemoteProxy enableIn(String ip, int port){
        connector.listen(ip, port);
        return this;
    }

    private void onMessageReceived(Message msg){
        if (msg.getType() == MessageType.Proxy)
            remoteToken = msg.getToken();
        if (!StringHelper.isNullOrEmpty(remoteToken) && msg.getToken().equals(remoteToken))
            super.fakeMessage(msg, this.getToken());
        bridgeOf(msg);
    }

    @Override
    protected void onEnabled() {
    }

    @Override
    public void relay(Message msg) {
        if(!StringHelper.isNullOrEmpty(remoteToken) && msg.getToken().equals(getToken()))
            super.fakeMessage(msg, remoteToken);
        connector.sendObject(msg);
    }

    @Override
    protected void onDispose() {
        if(connector != null){
            connector.stopListen();
            connector.disconnect();
        }
        connector = null;
    }
}
