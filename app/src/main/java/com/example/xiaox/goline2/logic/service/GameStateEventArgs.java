package com.example.xiaox.goline2.logic.service;

import com.example.xiaox.goline2.extension.event.EventArgs;

/**
 * Created by xiaox on 2/2/2017.
 */
public class GameStateEventArgs extends EventArgs {
    private ChessGameState oldChessGameState;
    private ChessGameState newChessGameState;

    public ChessGameState getOldState() {
        return this.oldChessGameState;
    }

    public ChessGameState getNewState() {
        return this.newChessGameState;
    }

    public GameStateEventArgs() {
        super();
    }

    public GameStateEventArgs(ChessGameState oldChessGameState, ChessGameState newChessGameState) {
        this.oldChessGameState = oldChessGameState;
        this.newChessGameState = newChessGameState;
    }
}

