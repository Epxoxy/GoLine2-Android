package core.networkbase;

import java.util.WeakHashMap;

import core.helpers.StringHelper;

public abstract class BridgeBase implements IBridge {
    private WeakHashMap<String,IProxy> proxies;
    private final Object lockHelper = new Object();

    public BridgeBase(){
        this.proxies = new WeakHashMap<String,IProxy>();
    }

    public boolean attachProxy(IProxy proxy){
        if (proxy == null || StringHelper.isNullOrEmpty(proxy.getToken()))
            return false;
        synchronized (lockHelper){
            proxies.put(proxy.getToken(), proxy);
        }
        return true;
    }

    public void detachProxy(String token){
        if (proxies.containsKey(token)){
            synchronized (lockHelper){
                proxies.remove(token);
            }
        }
    }

    public void enable(){
        for (IProxy proxy : proxies.values())
            proxy.enable(this);
    }


    public void handMessage(Message msg){
        onMessage(msg);
    }

    protected abstract void onMessage(Message msg);

    public void unicast(String token, Message msg){
        if (!proxies.containsKey(token)) return;
        try{
            proxies.get(token).relay(msg);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void broadcast(Message msg){
        synchronized (lockHelper){
            for (IProxy proxy : proxies.values()){
                try{
                    proxy.relay(msg);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
}
