package com.zhuan.screencapture;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Point;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.lang.reflect.Method;

/**
 * description：   <br/>
 * ===============================<br/>
 * creator：Jiacheng<br/>
 * create time：2018/3/2 上午11:31<br/>
 * ===============================<br/>
 * reasons for modification：  <br/>
 * Modifier：  <br/>
 * Modify time：  <br/>
 */
public class Util {
    /**
     * 获取屏幕分辨率
     */
    public static Point getRealScreenSize(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager == null) {
            return null;
        }
        Point screenSize = new Point();
        Display defaultDisplay = windowManager.getDefaultDisplay();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            defaultDisplay.getRealSize(screenSize);
        } else {
            try {
                Method mGetRawW = Display.class.getMethod("getRawWidth");
                Method mGetRawH = Display.class.getMethod("getRawHeight");
                screenSize.set((Integer) mGetRawW.invoke(defaultDisplay), (Integer) mGetRawH.invoke(defaultDisplay));
            } catch (Exception e) {
                screenSize.set(defaultDisplay.getWidth(), defaultDisplay.getHeight());
                e.printStackTrace();
            }
        }
        return screenSize;
    }
}
