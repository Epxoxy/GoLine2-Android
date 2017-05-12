package com.example.xiaox.goline2.logic.interfaces;

import com.example.xiaox.goline2.logic.basic.ActionType;
import com.example.xiaox.goline2.logic.player.Player;
import com.example.xiaox.goline2.logic.basic.Point;
import com.example.xiaox.goline2.logic.service.ChessGameState;

import java.util.Stack;

/**
 * Created by xiaox on 1/24/2017.
 */
public interface IChessGameService {
    long getRunningTime();
    long getUsedTimeOf(Player player);
    boolean join(Player player);
    boolean leave(Player player);
    boolean setBeginner(Player player);
    Player getActivatedPlayer();
    Player getNextPlayer();
    Player getFrontPlayer();
    Player getBeginner();
    boolean start();
    boolean stop();
    boolean reset();
    boolean restart();
    void detach();
    ChessGameState getState();
    boolean handAction(Player player, ActionType type);
    boolean handAction(Player player, ActionType type, Object[] params);
    boolean isTurnOf(Player player);
    Stack<Point> getInputsOf(Player player);
}
