package com.example.xiaox.goline2.logic.service;

import com.example.xiaox.goline2.extension.event.EventArgs;
import com.example.xiaox.goline2.logic.player.Player;

/**
 * Created by xiaox on 2/2/2017.
 */
public class GameEndedEventArgs extends EventArgs {
    private boolean isInterrupt;
    private boolean hasWinner;
    private Player winner;

    public boolean isInterrupt() {
        return this.isInterrupt;
    }

    public boolean hasWinner() {
        return this.hasWinner;
    }

    public Player winner() {
        return this.winner;
    }

    public GameEndedEventArgs() {
    }

    public GameEndedEventArgs(boolean isInterrupt) {
        this.isInterrupt = isInterrupt;
    }

    public GameEndedEventArgs(Player player) {
        this.winner = player;
        this.hasWinner = true;
    }
}
