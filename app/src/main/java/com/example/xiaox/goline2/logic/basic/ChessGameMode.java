package com.example.xiaox.goline2.logic.basic;

/**
 * Created by xiaox on 1/29/2017.
 */
public class ChessGameMode {
    public static final int PVE = 0;//Player vs Environment
    public static final int AIvsAI= 1;//For test only
    public static final int PVPLocal= 2;//Player vs Player (local)
    public static final int PVPOnline = 3;//Player vs Player (online)
    public static final String[] modeItems = new String[]{
            "Player vs AI", "AI vs AI", "Player vs player", "Online"
    };
}
