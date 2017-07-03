package core.judge;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

import core.data.ActionType;
import core.data.InputAction;
import core.data.JudgeCode;
import core.helpers.StringHelper;
import core.interfaces.JudgedListener;
import core.networkbase.BridgeBase;
import core.networkbase.Message;
import core.networkbase.MessageType;
import core.struct.MapFormation;

public class Judges extends BridgeBase{
    private JudgeUnit judge;
    //Store originToken - judgeToken
    private WeakHashMap<String, String> toJudge;
    private WeakHashMap<String, String> toProxy;
    public JudgeUnit getJudgeUnit(){return judge;}
    private Object lockHelper = new Object();
    public Judges() {
        this(new MapFormation(2, 2, 24));
    }

    public Judges(MapFormation map){
        judge = new JudgeUnit(map);
        toJudge = new WeakHashMap<String, String>();
        toProxy = new WeakHashMap<String, String>();
    }

    public void onAttach(){
        judge.setJudgedListener(judgedListener);
        judge.attach();
    }

    public void onDetach(){
        judge.detach();
        judge.setJudgedListener(null);
    }

    public void reset(){
        judge.reset();
    }

    public int getBoxId(String proxyToken){
        if (!StringHelper.isNullOrEmpty(proxyToken)){
            for(String key : toProxy.keySet()){
                if(toProxy.get(key).equals(proxyToken)){
                    return judge.getBoxId(key);
                }
            }
        }
        return -1;
    }

    private JudgedListener judgedListener = new JudgedListener(){
        @Override
        public void onJudged(String judgeToken, JudgeCode code, Object extra) {
            String proxyToken = findCastToken(judgeToken);
            List<Object> data = new ArrayList<Object>();
            data.add(code);
            if (extra != null) data.add(extra);
            broadcastJudgeMsg(proxyToken, data.toArray());
        }
    };

    @Override
    protected void onMessage(Message msg) {
        if(msg != null && msg.getType() == MessageType.Action){
            synchronized (lockHelper){
                handAction(msg.getToken(), msg.getContent());
            }
        }
    }
    //Hand actions from player
    @SuppressWarnings("unused")
    private void handAction(String proxyToken, Object content){
        InputAction action = null;
        if (content != null && (action = (InputAction)content) != null){
            System.out.println(String.format(".....\n##~ Handing ~##\n[Judge-Bridge] <-- [Token: %s]", proxyToken));
            System.out.println(String.format("[Judge-Bridge] <-- [Type: %s]",action.getType()));

            boolean accepted = false;
            Object fallback = null;
            //Hand join action
            //Create virtual player and hand by judge
            if (action.getType() == ActionType.Join){
                if (!toJudge.containsKey(proxyToken)){
                    toJudge.put(proxyToken, "");
                }
                jToken = new String[1];
                pToken = proxyToken;
                accepted = judge.join(jToken);
                toJudge.put(proxyToken, jToken[0]);
                jToken = null;
                pToken = "";
            }/*else if(action.Type == ActionType.Undo)
             {
                 string active = findCastToken(judge.ActiveToken);
                 //Send FALLBACK to player
                 Broadcast(Message.CreateMessage(active, new object[] {
                     action.Type, proxyToken
                 }, MessageType.Action));
             }*/
            //Hand other actions
            else if (toJudge.containsKey(proxyToken)){
                if(action.getType() == ActionType.Ready){

                }
                accepted = judge.handInput(toJudge.get(proxyToken), action);
            }
            //Send FALLBACK to player
             /*Broadcast(Message.CreateMessage(proxyToken, new object[]{
                 action.Type, FALLBACK, accepted
             }, MessageType.Fallback));*/
        }
    }

    private void broadcastJudgeMsg(String proxyToken, Object[] data){
        super.broadcast(Message.createMessage(proxyToken, data, MessageType.Judge));
    }

    private String findCastToken(String judgeToken){
        if (!StringHelper.isNullOrEmpty(judgeToken)){
            if (!toProxy.containsKey(judgeToken)){
                if(jToken != null && judgeToken.equals(jToken[0]) && !StringHelper.isNullOrEmpty(pToken)){
                    toProxy.put(judgeToken, pToken);
                    return pToken;
                }
                else{
                    for(String key : toJudge.keySet()){
                        if(toJudge.get(key).equals(judgeToken)){
                            toProxy.put(judgeToken, key);
                            return key;
                        }
                    }
                }
            }
            else return toProxy.get(judgeToken);
        }
        return "";
    }

    private String[] jToken;
    private String pToken;
}
