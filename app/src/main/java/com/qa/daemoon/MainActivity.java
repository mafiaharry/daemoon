package com.qa.daemoon;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        System.out.println("zzzzzzzzzzzzzzzzzzzzCreate");

        Intent intent = new Intent(MainActivity.this, MyService.class);
        startService(intent);
    }
}