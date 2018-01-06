package com.developerhaoz.androidotgdemo;

import android.app.Application;
import android.content.Context;

import com.github.mjdev.libaums.fs.UsbFile;

/**
 * @author Haoz
 * @date 2018/1/6.
 */
public class OtgApplication extends Application {

    private static Context sInstance;
    public static UsbFile mRootDirectory;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }

    public static Context getContext(){
        return sInstance;
    }
}
