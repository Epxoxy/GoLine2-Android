package com.example.xiaox.goline2.logic.service;

import com.example.xiaox.goline2.extension.event.EventArgs;
import com.example.xiaox.goline2.logic.basic.ActionType;
import com.example.xiaox.goline2.logic.player.Player;

/**
 * Created by xiaox on 2/2/2017.
 */
public class GameActionEventArgs extends EventArgs {
    private ActionType type;
    private Player player;
    private Object[] params;

    public ActionType getActionType() {
        return this.type;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Object[] getParams() {
        return this.params;
    }
    public GameActionEventArgs() {
    }

    public GameActionEventArgs(Player player) {
        this.player = player;
    }

    public GameActionEventArgs(ActionType type, Player player) {
        this.type = type;
        this.player = player;
    }

    public GameActionEventArgs(ActionType type, Player player, Object[] params) {
        this.type = type;
        this.player = player;
        this.params = params;
    }
}

