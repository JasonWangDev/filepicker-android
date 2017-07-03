package com.github.jasonwangdev.filepicker.demo;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager fm = getSupportFragmentManager();
        MainFragment fragment = (MainFragment) fm.findFragmentByTag("MainFragment");
        if (null == fragment)
            fragment = new MainFragment();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.layout, fragment, "MainFragment");
        ft.commit();
    }

}
