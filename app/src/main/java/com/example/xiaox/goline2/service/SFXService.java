package com.example.xiaox.goline2.service;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;

import java.io.IOException;

/**
 * Created by xiaox on 2/10/2017.
 */
public class SFXService {

    private static MediaPlayer FXPlayer = null;
    public static void play(Context context, int soundID){
        if(FXPlayer != null){
            FXPlayer.stop();
            FXPlayer.release();
        }
        FXPlayer = MediaPlayer.create(context.getApplicationContext(), soundID);
        if(FXPlayer != null){
            FXPlayer.start();
        }/*
        SoundPool soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        soundPool.load(context, soundID, 0);
        soundPool.play(soundID, 1,1,0,0,1);*/
    }
}
