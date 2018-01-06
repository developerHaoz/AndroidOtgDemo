package com.developerhaoz.androidotgdemo;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.lang.ref.WeakReference;

/**
 * @author Haoz
 * @date 2018/1/6.
 */
public class MainActivity extends AppCompatActivity {

    private static WeakReference<Activity> mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = new WeakReference<Activity>(this);
    }

    public static WeakReference<Activity> getContext(){
        if(mContext != null){
            return mContext;
        }
        return null;
    }
}
