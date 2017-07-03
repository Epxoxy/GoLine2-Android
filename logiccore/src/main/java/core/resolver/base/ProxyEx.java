package core.resolver.base;

import core.data.ActionType;
import core.data.InputAction;
import core.networkbase.IProxy;
import core.networkbase.LocalProxy;
import core.networkbase.Message;
import core.networkbase.MessageType;
import core.networkbase.RemoteProxy;

public class ProxyEx {
    public static int getValidSize(Object[] objs)
    {
        if (objs != null)
        {
            return objs.length;
        }
        else return 0;
    }

    public static void passAction(LocalProxy proxy, ActionType type, Object data){
        proxy.send(action(proxy.getToken(), type, data));
    }

    public static void passActionByToken(LocalProxy proxy, String token, ActionType type, Object data){
        proxy.sendByToken(token, action(token, type, data));
    }

    public static void relayAction(RemoteProxy proxy, ActionType type, Object data){
        proxy.relay(action(proxy.getToken(), type, data));
    }

    private static Message action(String token, ActionType type, Object data){
        return Message.createMessage(token, new InputAction(type, data), MessageType.Action);
    }

    public static void dispose(IProxy proxy){
        if(proxy != null){
            proxy.dispose();
            proxy = null;
        }
    }
}
