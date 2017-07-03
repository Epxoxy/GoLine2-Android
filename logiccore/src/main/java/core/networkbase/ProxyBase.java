package core.networkbase;

public abstract class ProxyBase implements IProxy{
    private String token;
    private IBridge bridge;


    @Override
    public String getToken() {
        return this.token;
    }

    @Override
    public void setToken(String token) {
        this.token = token;

    }
    //Enable proxy
    @Override
    public void enable(IBridge center){
        this.bridge = center;
        onEnabled();
    }

    //Do something on enable proxy
    protected void onEnabled(){}
    //Relay message from message center
    @Override
    public abstract void relay(Message msg);

    //Relay message to message center
    //Let message hand message
    protected void bridgeOf(Message msg){
        if (this.bridge == null) return;
        System.out.println("[ProxyBase]Remote <-- " + msg.getType());
        this.bridge.handMessage(msg);
    }

    @Override
    public void dispose(){
        if (this.bridge != null)
            this.bridge.detachProxy(token);
        this.bridge = null;
        onDispose();
    }

    protected void onDispose(){

    }

    protected void fakeMessage(Message msg, String fakeToken){
        msg.setToken(fakeToken);
    }
}
