package com.sty.xxt.jvmmemory;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;


public class ListActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        RecyclerView rv = findViewById(R.id.rv);
        rv.setAdapter(new MyAdapter(this));
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("a","a");
    }
}
