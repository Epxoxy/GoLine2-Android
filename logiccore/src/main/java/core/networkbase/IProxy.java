package core.networkbase;

import core.interfaces.IDisposable;

public interface IProxy extends IDisposable {
    String getToken();
    void setToken(String token);
    void relay(Message msg);
    void enable(IBridge center);
}
