package com.sty.xxt.jvmmemory;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void jump1(View view) {
        startActivity(new Intent(this, SecondActivity.class));
    }

    public void jump2(View view) {
        startActivity(new Intent(this, ListActivity.class));
    }


    public String addStr(String[] values) {
        String result = null;
        for (int i = 0; i < values.length; i++) {
            result += values[i];
        }
        return result;
    }
}
