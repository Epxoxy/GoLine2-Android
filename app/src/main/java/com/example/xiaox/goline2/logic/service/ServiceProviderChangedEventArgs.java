package com.example.xiaox.goline2.logic.service;

import com.example.xiaox.goline2.extension.event.EventArgs;

/**
 * Created by xiaox on 2/3/2017.
 */
public class ServiceProviderChangedEventArgs extends EventArgs{
    private ChessGameService oldService;
    private ChessGameService newService;

    public ChessGameService getOldService(){return this.oldService;}
    public ChessGameService getNewService(){return this.newService;}

    public ServiceProviderChangedEventArgs(){

    }
    public ServiceProviderChangedEventArgs(ChessGameService oldService, ChessGameService newService){
        this.newService = newService;
        this.oldService = oldService;
    }

}
