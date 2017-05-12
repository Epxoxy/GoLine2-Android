package com.example.xiaox.goline2.logic.player;

import com.example.xiaox.goline2.logic.basic.ActionType;
import com.example.xiaox.goline2.logic.basic.RequestResult;
import com.example.xiaox.goline2.logic.service.ChessGameService;

/**
 * Created by xiaox on 1/29/2017.
 */
public class OnlinePlayer extends Player {

    @Override
    public RequestResult onRequest(ActionType type, Object[] params){
        return RequestResult.NoResponse;
    }

    @Override
    protected void onServiceProviderChanged(ChessGameService oldProvider, ChessGameService newProvider){

    }
}
