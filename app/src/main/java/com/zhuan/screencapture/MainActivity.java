package com.zhuan.screencapture;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.security.Permission;
import java.security.Permissions;

import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;

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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PERMISSION_GRANTED) { //有权限

        } else { //没有权限
            // 没有权限。
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // 用户拒绝过这个权限了，应该提示用户，为什么需要这个权限。

                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            } else {
                // 申请授权。
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 111);
            }
            //requestCode:会在回调onRequestPermissionsResult()时返回，用来判断是哪个授权申请的回调
            //String[] permissions，权限数组，你需要申请的的权限的数组。
            //由于该方法是异步的，所以无返回值，当用户处理完授权操作时，会回调Activity或者Fragment的onRequestPermissionsResult()方法。
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 111);
        }
    }

    // int requestCode，在调用requestPermissions()时的第一个参数。
    // String[] permissions，权限数组，在调用requestPermissions()时的第二个参数。
    // int[] grantResults，授权结果数组，对应permissions，具体值和上方提到的PackageManager中的两个常量做比较。
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.e("wjc", "onRequestPermissionsResult-->requestCode:" + requestCode + ",permissions:" + permissions + "grantResults:" + grantResults);
    }

    @Override
    protected void onPause() {
        super.onPause();
        manager.stopListen();
    }

    private ScreenshotFloatView view;

    @Override
    public void onShowScreenShot(String imagePath) {
        Log.e("wjc", imagePath);
        view = new ScreenshotFloatView(this);
        view.setCaptureFloatClickListener(new ScreenshotFloatView.ICaptureFloatClickListener() {
            @Override
            public void capture(int type) {
                Toast.makeText(MainActivity.this, "" + type, Toast.LENGTH_SHORT).show();
                view.destroy();
            }
        });
        view.applyData(imagePath);
        view.create();
        new Handler(getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (view != null) {
                    view.destroy();
                }
            }
        }, 3000);
    }
}