package com.zhuan.screencapture;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements ScreenCaptureManager.OnScreenShotListener {
    ScreenCaptureManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        manager = ScreenCaptureManager.newInstance(this.getApplicationContext());
        manager.setScreenShotListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        manager.startListen();
    }

    @Override
    protected void onPause() {
        super.onPause();
        manager.stopListen();
    }

    @Override
    public void onShot(String imagePath) {
        Log.e("wjc", imagePath);
        final ScreenshotFloatView view = new ScreenshotFloatView(MainActivity.this);
//        view.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(MainActivity.this, "click drag float view", Toast.LENGTH_SHORT).show();
//                view.destroy();
//            }
//        });
        view.setCaptureFloatClickListener(new ScreenshotFloatView.ICaptureFloatClickListener() {
            @Override
            public void captute(int type) {
                Toast.makeText(MainActivity.this, "" + type, Toast.LENGTH_SHORT).show();
            }
        });
        view.applyData(imagePath);
        view.create();
    }
}