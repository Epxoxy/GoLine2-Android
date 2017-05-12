package com.example.xiaox.goline2.logic.service;

import com.example.xiaox.goline2.extension.event.EventArgs;
import com.example.xiaox.goline2.logic.player.Player;

/**
 * Created by xiaox on 2/2/2017.
 */
public class ActivatedChangedEventArgs extends EventArgs {
    Player previousPlayer;
    Player activatedPlayer;

    public Player getActivatedPlayer() {
        return this.activatedPlayer;
    }

    public Player getPreviousPlayer() {
        return this.previousPlayer;
    }

    public ActivatedChangedEventArgs() {

    }

    public ActivatedChangedEventArgs(Player activatedPlayer) {
        this.activatedPlayer = activatedPlayer;
    }

    public ActivatedChangedEventArgs(Player previousPlayer, Player activatedPlayer) {
        this.previousPlayer = previousPlayer;
        this.activatedPlayer = activatedPlayer;
    }
}

