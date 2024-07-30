package com.android.iplayer.widget.util;

import android.app.Application;
import android.content.Context;

/**
 * created by lz
 * 2022/12/21
 * Desc:
 */
public class ScreenUtils extends BaseScreenUtil {

    private volatile static ScreenUtils mInstance;
    private volatile Context context;


    public static synchronized ScreenUtils getInstance(Context context) {
        synchronized (ScreenUtils.class) {
            if (null == mInstance) {
                mInstance = new ScreenUtils(context);
            }
        }
        return mInstance;
    }

    public ScreenUtils(){

    }

    public ScreenUtils(Context context){
        this.context = context;
    }


    @Override
    public Context getApplicationContext() {
        if(context == null){
            throw new IllegalStateException("Context is not initialized");
        }
        return context;
//        return App.getInstance().getContext();
    }
}