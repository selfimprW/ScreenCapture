package com.selfimpr.screencapture;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
//三星：/storage/emulated/0/DCIM/Screenshots/Screenshot_20180302-120442.png

/**
 * description：   <br/>
 * ===============================<br/>
 * creator：Jiacheng<br/>
 * create time：2018/3/2 上午10:58<br/>
 * ===============================<br/>
 * reasons for modification：  <br/>
 * Modifier：  <br/>
 * Modify time：  <br/>
 */
public class ScreenCaptureManager {

    private static final String TAG = "ScreenCaptureManager";

    /**
     * 读取媒体数据库时需要读取的列, 其中 WIDTH 和 HEIGHT 字段在 API 16 以后才有
     */
    private static final String[] MEDIA_PROJECTIONS_API = {
            MediaStore.Images.ImageColumns.DATA, //Path to the file on disk.
            MediaStore.Images.ImageColumns.DATE_TAKEN, // The date & time that the image was taken in units of milliseconds since jan 1, 1970.
            MediaStore.Images.ImageColumns.WIDTH, //The width of the image/video in pixels.
            MediaStore.Images.ImageColumns.HEIGHT, //The height of the image/video in pixels.
            MediaStore.Images.Media.DISPLAY_NAME, //The display name of the file
//            MediaStore.Images.Media.DATE_ADDED //The time the file was added to the media provider  Units are seconds since 1970.
    };

    /**
     * 截屏依据中的路径判断关键字
     */
    private static final String[] KEYWORDS = {
            "screenshot", "screen_shot", "screen-shot",
            "screencapture", "screen_capture", "screen-capture", "截屏", "截图"
    };

    private static Point sScreenRealSize;

    /**
     * 已回调过的路径
     */
    private final List<String> sHasCallbackPaths = new ArrayList<>();

    private Context mContext;

    private OnScreenShotListener screenShotListener;

    private long mStartListenTime;

    /**
     * 内部存储器内容观察者
     */
    private MediaContentObserver mediaContentObserver;

    /**
     * 运行在 UI 线程的 Handler, 用于运行监听器回调
     */
    private final Handler mUiHandler = new Handler(Looper.getMainLooper());

    public static ScreenCaptureManager newInstance(Context context) {
        assertInMainThread();
        return new ScreenCaptureManager(context);
    }

    private ScreenCaptureManager(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("The context must not be null.");
        }
        mContext = context;

        // 获取屏幕真实的分辨率
        if (sScreenRealSize == null) {
            sScreenRealSize = ScreenUtil.getRealScreenSize(mContext);
            if (sScreenRealSize != null) {
                Log.d(TAG, "Screen Real Size: " + sScreenRealSize.x + " * " + sScreenRealSize.y);
            } else {
                Log.w(TAG, "Get screen real size failed.");
            }
        }
    }

    private static final String EXTERNAL_CONTENT_URI_MATCHER =
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString();
    private static final String INTERNAL_CONTENT_URI_MATCHER =
            MediaStore.Images.Media.INTERNAL_CONTENT_URI.toString();

    /**
     * 启动监听
     */
    public void startListen() {
        assertInMainThread();

        sHasCallbackPaths.clear();

        // 记录开始监听的时间戳
        mStartListenTime = System.currentTimeMillis();

        // 创建内容观察者
        mediaContentObserver = new MediaContentObserver(mUiHandler);//MediaStore.Images.Media.INTERNAL_CONTENT_URI,
//        mExternalObserver = new MediaContentObserver( mUiHandler); //MediaStore.Images.Media.EXTERNAL_CONTENT_URI,

        // 注册内容观察者
        mContext.getContentResolver().registerContentObserver(
                MediaStore.Images.Media.INTERNAL_CONTENT_URI,
                false,
                mediaContentObserver
        );
        mContext.getContentResolver().registerContentObserver(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                false,
                mediaContentObserver
        );
    }

    /**
     * 停止监听
     */
    public void stopListen() {
        assertInMainThread();

        if (mediaContentObserver != null) {  // 注销内容观察者
            try {
                mContext.getContentResolver().unregisterContentObserver(mediaContentObserver);
            } catch (Exception e) {
                e.printStackTrace();
            }
            mediaContentObserver = null;
        }

        // 清空数据
        mStartListenTime = 0;
        sHasCallbackPaths.clear();
    }


    private Point getImageSize(String imagePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);
        return new Point(options.outWidth, options.outHeight);
    }

    /**
     * 处理获取到的一行数据
     */
    private void handleMediaRowData(String data, long dateTaken, int width, int height) {
        if (checkScreenShot(data, dateTaken, width, height)) {
            Log.d(TAG, "ScreenShot: path = " + data + "; size = " + width + " * " + height + "; date = " + dateTaken);
            if (screenShotListener != null && !checkCallback(data)) {
                screenShotListener.onShowScreenShot(data);
            }
        } else {
            // 如果在观察区间媒体数据库有数据改变，又不符合截屏规则，则输出到 log 待分析
            Log.w(TAG, "Media content changed, but not screenshot: path = " + data + "; size = " + width + " * " + height + "; date = " + dateTaken);
        }
    }

    /**
     * 判断指定的数据行是否符合截屏条件
     */
    private boolean checkScreenShot(String data, long dateTaken, int width, int height) {
        //todo 判断依据一: 时间判断
        // 如果加入数据库的时间在开始监听之前, 或者与当前时间相差大于10秒, 则认为当前没有截屏
        if (dateTaken < mStartListenTime || (System.currentTimeMillis() - dateTaken) > 10 * 1000) {
            return false;
        }

        //todo 判断依据二: 尺寸判断
        if (sScreenRealSize != null) {// 如果图片尺寸超出屏幕, 则认为当前没有截屏
            if (!((width <= sScreenRealSize.x && height <= sScreenRealSize.y) || (height <= sScreenRealSize.x && width <= sScreenRealSize.y))) {
                return false;
            }
        }

        //todo 判断依据三: 路径判断
        if (TextUtils.isEmpty(data)) {
            return false;
        }
        data = data.toLowerCase();
        // 判断图片路径是否含有指定的关键字之一, 如果有, 则认为当前截屏了
        for (String keyWork : KEYWORDS) {
            if (data.toLowerCase().contains(keyWork)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否已回调过, 某些手机ROM截屏一次会发出多次内容改变的通知; <br/>
     * 删除一个图片也会发通知, 同时防止删除图片时误将上一张符合截屏规则的图片当做是当前截屏.
     */
    private boolean checkCallback(String imagePath) {
        if (sHasCallbackPaths.contains(imagePath)) {
            return true;
        }
        // 大概缓存15~20条记录便可
        if (sHasCallbackPaths.size() >= 20) {
            for (int i = 0; i < 5; i++) {
                sHasCallbackPaths.remove(0);
            }
        }
        sHasCallbackPaths.add(imagePath);
        return false;
    }

    /**
     * 设置截屏监听器
     */
    public void setScreenShotListener(OnScreenShotListener listener) {
        this.screenShotListener = listener;
    }

    public interface OnScreenShotListener {
        void onShowScreenShot(String imagePath);
    }

    private static void assertInMainThread() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            StackTraceElement[] elements = Thread.currentThread().getStackTrace();
            String methodMsg = null;
            if (elements != null && elements.length >= 4) {
                methodMsg = elements[3].toString();
            }
            throw new IllegalStateException("Call the method must be in main thread: " + methodMsg);
        }
    }

    private static final String SORT_ORDER = MediaStore.Images.Media.DATE_ADDED + " DESC";

    /**
     * 处理媒体数据库的内容改变
     */
    private void handleMediaContentChange(Uri contentUri) {
        Cursor cursor = null;
        try {
            // 数据改变时查询数据库中最后加入的一条数据
            cursor = mContext.getContentResolver().query(
                    contentUri,
                    MEDIA_PROJECTIONS_API,
                    null,
                    null,
                    MediaStore.Images.ImageColumns.DATE_ADDED + " desc limit 1"
            );

            if (cursor == null) {
                Log.e(TAG, "Deviant logic.");
                return;
            }
            if (!cursor.moveToFirst()) {
                Log.d(TAG, "Cursor no data.");
                return;
            }

            // 获取各列的索引
            int dataIndex = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            int dateTakenIndex = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_TAKEN);
            int widthIndex = -1;
            int heightIndex = -1;
            if (Build.VERSION.SDK_INT >= 16) {
                widthIndex = cursor.getColumnIndex(MediaStore.Images.ImageColumns.WIDTH);
                heightIndex = cursor.getColumnIndex(MediaStore.Images.ImageColumns.HEIGHT);
            }

//            long dateAdded = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED));
            // 获取行数据
            String data = cursor.getString(dataIndex);
            long dateTaken = cursor.getLong(dateTakenIndex);
            int width = 0;
            int height = 0;
            if (widthIndex >= 0 && heightIndex >= 0) {
                width = cursor.getInt(widthIndex);
                height = cursor.getInt(heightIndex);
            } else {// API 16 之前, 宽高要手动获取
                Point size = getImageSize(data);
                width = size.x;
                height = size.y;
            }

            // 处理获取到的第一行数据
            handleMediaRowData(data, dateTaken, width, height);
        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    /**
     * 媒体内容观察者(观察媒体数据库的改变)
     */
    private class MediaContentObserver extends ContentObserver {


        public MediaContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            Log.d(TAG, "onChange: " + selfChange + ", " + uri.toString());
            if (uri.toString().startsWith(EXTERNAL_CONTENT_URI_MATCHER) || uri.toString().startsWith(INTERNAL_CONTENT_URI_MATCHER)) {
                handleMediaContentChange(uri);
            }
            super.onChange(selfChange, uri);
        }
    }
}