package core.networkbase;

public interface IBridge {
    void handMessage(Message msg);
    boolean attachProxy(IProxy proxy);
    void detachProxy(String token);
    void enable();
}
