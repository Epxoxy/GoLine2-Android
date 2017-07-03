package core.resolver;

import core.data.ActionType;
import core.data.IntPoint;
import core.data.JudgeCode;
import core.helpers.StringHelper;
import core.helpers.TokenHelper;
import core.interfaces.IBoard;
import core.interfaces.LatticeClickListener;
import core.networkbase.IConnector;
import core.networkbase.Message;
import core.networkbase.MessageType;
import core.networkbase.RemoteProxy;
import core.resolver.base.CoreResolverBase;
import core.resolver.base.ProxyEx;

class OnlineResolver extends CoreResolverBase{
    private RemoteProxy remote;
    private String outip;
    private int outport;

    //Not completely implement
    OnlineResolver(IConnector connector, IBoard board, String outip, int outport, int inport){
        this.outip = outip;
        this.outport = outport;
        this.board = board;

        String token = TokenHelper.shortToken(10);
        connector.setToken(token);
        remote = new RemoteProxy(connector);
        remote.setToken(token);
        judges.attachProxy(remote);

        remote.enableIn("127.0.0.1", inport);
        board.setLatticeClickListener(listener);
    }

    private LatticeClickListener listener = new LatticeClickListener(){
        @Override
        public void onLatticeClick(int column, int row, int clickRadius) {
            if (!isStarted()) return;
            submit(null, ActionType.Input, new IntPoint(column, row));
        }

    };

    @Override
    public void ready(){
        judges.enable();
        remote.enableOut(outip, outport);
        remote.relay(Message.createMessage(remote.getToken(), "", MessageType.Proxy));
    }

    public void start(){
        submit(null,ActionType.Join, null);
        submit(null, ActionType.Ready, null);
    }

    public void Reset(){
        submit(null, ActionType.Reset, new Object());
    }

    @Override
    public void submit(String token, ActionType type, Object data){
        ProxyEx.relayAction(remote, type, data);
    }

    @Override
    public void onDispose() {
        if(remote != null){
            remote.dispose();
            remote = null;
        }
    }

    @Override
    public void onBasicJudge(String token, JudgeCode code){
        if(code == JudgeCode.Joined && !token.equals(hostToken))
            if (StringHelper.isNullOrEmpty(joinToken))
                joinToken = token;
    }

    @Override
    public void undo(){
        submit(null, ActionType.Undo, null);
    }
}