package core.networkbase;

public class Message {
    private String token;
    private Object content;
    private MessageType type;

    public MessageType getType(){
        return this.type;
    }
    public Object getContent(){
        return this.content;
    }
    public String getToken(){
        return this.token;
    }

    void setToken(String token){
        this.token = token;
    }

    public static Message createMessage(String token, Object content, MessageType type){
        Message msg = new Message();
        msg.type = type;
        msg.content = content;
        msg.token = token;
        return msg;
    }
}
