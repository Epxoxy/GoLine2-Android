package com.example.xiaox.goline2.logic.service;

import com.example.xiaox.goline2.extension.event.EventArgs;
import com.example.xiaox.goline2.logic.basic.ActionType;
import com.example.xiaox.goline2.logic.basic.RequestResult;
import com.example.xiaox.goline2.logic.player.Player;

/**
 * Created by xiaox on 2/4/2017.
 */
public class RequestEventArgs extends EventArgs {
    private ActionType type;
    private Player player;
    private Object[] params;
    public RequestResult handResult;

    public ActionType getActionType() {
        return this.type;
    }
    public RequestResult getHandResult(){return this.handResult;}
    public Player getPlayer() {
        return this.player;
    }
    public Object[] getParams() {
        return this.params;
    }

    public RequestEventArgs() {
        handResult = RequestResult.NoResponse;
    }

    public RequestEventArgs(Player player) {
        this();
        this.player = player;
    }

    public RequestEventArgs(ActionType type, Player player) {
        this();
        this.type = type;
        this.player = player;
    }

    public RequestEventArgs(ActionType type, Player player, Object[] params) {
        this();
        this.type = type;
        this.player = player;
        this.params = params;
    }
}
