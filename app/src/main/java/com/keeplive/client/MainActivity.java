package com.keeplive.client;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onDestroy() {
        super.onDestroy();
//        unbindService(connection);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);

//        findViewById(R.id.btn_start_local_service).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
        Intent intent = new Intent(MainActivity.this, LocalService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
//        bindRemoteService();
//            }
//        });

    }

}
