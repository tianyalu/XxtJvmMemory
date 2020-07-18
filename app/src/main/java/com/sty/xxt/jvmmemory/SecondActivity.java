package com.sty.xxt.jvmmemory;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

import java.lang.ref.WeakReference;


public class SecondActivity extends Activity {

    byte[] buffer;
    Handler handler = new Handler();
    int i;

//    static class R1 implements Runnable {
//        WeakReference<> secondActivity;
//
//        public R1(SecondActivity secondActivity) {
//            this.secondActivity = new WeakReference<>(secondActivity);
//        }
//
//        @Override
//        public void run() {
//                //更新UI
//        }
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sencond);


//        handler.postDelayed(new R1(this), 5_000);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
//        mHandler.removeCallbacksAndMessages(null);


        // 系统BUG：断掉 gc root
//        InputMethodManager im = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
//        try {
//            Field mCurRootView = InputMethodManager.class.getDeclaredField("mCurRootView");
//            mCurRootView.setAccessible(true);
//            mCurRootView.set(im, null);
//
//            Field mNextServedView = InputMethodManager.class.getDeclaredField("mNextServedView");
//            mNextServedView.setAccessible(true);
//            mNextServedView.set(im, null);
//
//            Field mServedView = InputMethodManager.class.getDeclaredField("mServedView");
//            mServedView.setAccessible(true);
//            mServedView.set(im, null);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}
