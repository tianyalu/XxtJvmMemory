package com.sty.xxt.jvmmemory;

import android.content.Context;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

public class Manager {
    //GC ROOT
    private static final Manager ourInstance = new Manager();

    static String i = "aa";

    private Context mContext;

    public static Manager getInstance() {
        return ourInstance;
    }

    private Manager() {
    }

    public void init(Context context){
       mContext = context;
    }
}