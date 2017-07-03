package com.example.xiaox.goline2.extension.helper;

import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;

public class AnimationEX {
    public static ScaleAnimation CreateScale(float fromX, float toX, float fromY, float toY){
        return new ScaleAnimation(fromX, toX, fromY, toY, Animation.RELATIVE_TO_SELF,0.5f, Animation.RELATIVE_TO_SELF,0.5f);
    }
}
