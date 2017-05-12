package com.example.xiaox.goline2.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by xiaox on 1/22/2017.
 */
public class MusicService extends Service {
    public IBinder onBind(Intent intent){
        return null;
    }
    @Override
    public void onCreate(){

    }
    @Override
    public void  onDestroy(){
        super.onDestroy();
    }
}
