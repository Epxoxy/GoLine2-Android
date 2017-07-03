package core.networkbase;
import core.interfaces.IDisposable;

public interface IConnector extends IDisposable{
    void setToken(String token);
    <T> void sendObject(T value);
    void listen(String ip, int port);
    void connect(String ip, int port);
    void setOnMessageListener(OnMessageListener listener);
    void stopListen();
    void disconnect();
    boolean isConnected();
}
