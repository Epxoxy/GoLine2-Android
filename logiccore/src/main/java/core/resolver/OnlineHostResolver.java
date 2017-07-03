package core.resolver;

import java.util.ArrayList;
import java.util.List;

import core.data.ActionType;
import core.data.IntPoint;
import core.helpers.TokenHelper;
import core.interfaces.IBoard;
import core.interfaces.LatticeClickListener;
import core.networkbase.IConnector;
import core.networkbase.LocalProxy;
import core.networkbase.RemoteProxy;
import core.resolver.base.CoreResolverBase;
import core.resolver.base.ProxyEx;

class OnlineHostResolver extends CoreResolverBase{
    private List<String> tokens;
    private LocalProxy local;
    private RemoteProxy remote;
    private String outip;
    private int outport;

    OnlineHostResolver(IConnector connector, IBoard board, String outip, int outport, int inport){
        this.outip = outip;
        this.outport = outport;
        this.board = board;

        hostToken = TokenHelper.shortToken(10);
        joinToken = TokenHelper.shortToken(10);
        tokens = new ArrayList<String>();
        tokens.add(hostToken);
        tokens.add(joinToken);
        judges.onAttach();
        //Create local PROXY
        local = new LocalProxy(relayListener);
        local.setToken(hostToken);
        //Create remote PROXY
        connector.setToken(joinToken);
        remote = new RemoteProxy(connector);
        remote.setToken(joinToken);
        remote.enableIn("127.0.0.1", inport);
        judges.attachProxy(local);
        judges.attachProxy(remote);
        board.setLatticeClickListener(latticeClickListener);
    }

    private LatticeClickListener latticeClickListener = new LatticeClickListener(){
        @Override
        public void onLatticeClick(int column, int row, int clickRadius) {
            if (!isStarted()) return;
            submit(ActionType.Input, new IntPoint(column, row));
        }
    };

    @Override
    public void ready() {
        judges.enable();
        remote.enableOut(outip, outport);
    }

    @Override
    public void start() {
        submit(ActionType.Join, null);
        submit(ActionType.Ready, null);
    }

    @Override
    public void undo() {
        submit(hostToken, ActionType.Undo, null);
    }

    @Override
    public void submit(String token, ActionType type, Object data){
        ProxyEx.passAction(local, type, data);
    }

    @Override
    public void onDispose(){
        ProxyEx.dispose(local);
        ProxyEx.dispose(remote);
    }

    public void submit(ActionType type, Object data){
        ProxyEx.passAction(local, type, data);
    }
}
