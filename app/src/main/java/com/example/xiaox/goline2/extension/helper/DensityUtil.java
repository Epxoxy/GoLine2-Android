package com.example.xiaox.goline2.extension.helper;

import android.content.Context;

/**
 * Created by xiaox on 2/7/2017.
 */
public class DensityUtil {
    /**
     * Convert dp to px
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * Convert px to dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
}
