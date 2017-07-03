package com.example.xiaox.goline2.extension.helper;

import android.os.Handler;
import android.os.Message;

public class ActionEx {
    public static void invoke(IAction action, Handler handler){
        Message msg = new Message();
        msg.obj = action;
        handler.sendMessage(msg);
    }
}
