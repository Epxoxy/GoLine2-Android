package core.resolver;

import java.util.ArrayList;
import java.util.List;

import core.data.ActionType;
import core.data.IntPoint;
import core.helpers.TokenHelper;
import core.interfaces.IBoard;
import core.interfaces.LatticeClickListener;
import core.networkbase.LocalProxy;
import core.resolver.base.CoreResolverBase;
import core.resolver.base.ProxyEx;

class PVPResolver extends CoreResolverBase{
    PVPResolver(IBoard board){
        System.out.println("Build PVPResolver....");
        this.board = board;
        judges.onAttach();
        hostToken=TokenHelper.shortToken(10);
        joinToken=TokenHelper.shortToken(10);
        tokens = new ArrayList<String>();
        tokens.add(hostToken);
        tokens.add(joinToken);
        shareProxy = new LocalProxy(relayListener);
        shareProxy.setToken(getHostToken());
        judges.attachProxy(shareProxy);
        this. board.setLatticeClickListener(latticeClickListener);
    }

    @Override
    public void ready() {
        judges.enable();
        for (String token : tokens)
            submit(token, ActionType.Join, null);
    }

    @Override
    public void start(){
        for (String token : tokens)
            submit(token, ActionType.Ready, null);
    }

    private LatticeClickListener latticeClickListener = new LatticeClickListener(){
        @Override
        public void onLatticeClick(int column, int row, int clickRadius) {
            if (!isStarted()) return;
            submit(getActiveToken(), ActionType.Input, new IntPoint(column, row));
        }

    };

    @Override
    public void submit(String token, ActionType type, Object data){
        ProxyEx.passActionByToken(shareProxy, token, type, data);
    }

    @Override
    public void onDispose(){
        ProxyEx.dispose(shareProxy);
    }

    @Override
    public void undo(){
        submit(getActiveToken(), ActionType.Undo, null);
    }

    private LocalProxy shareProxy;
    private List<String> tokens;
}
