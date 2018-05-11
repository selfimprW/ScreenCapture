package com.selfimpr.screencapture;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;


public class ScreenshotFloatView implements View.OnClickListener {

    /**
     * 悬浮框宽度
     */
    private static final int FLOAT_WIDTH_DP = 90;
    /**
     * 图片高度
     */
    private static final int IMAGE_HEIGHT_DP = 65;
    /**
     * 按钮高度
     */
    private static final int BUTTON_HEIGHT_DP = 35;
    /**
     * 悬浮框右间距
     */
    private static final int FLOAT_MARGIN_RIGHT = 20;

    private FragmentActivity activity;

    private WindowManager mWindowManager;
    private DisplayMetrics displayMetrics;

    private View mContentView;

    private int screenWidth;
    private int screenHeight;

    private boolean isShow = false;

    private ImageView imageView;
    private Bitmap thumpBitmap;

    public ScreenshotFloatView(FragmentActivity activity) {
        this.activity = activity;

        displayMetrics = activity.getResources().getDisplayMetrics();
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;
        mContentView = onCreateView();
        if (mContentView == null) {
            throw new IllegalArgumentException("No content view was found!");
        }
    }

    protected View onCreateView() {
        View contentView = LayoutInflater.from(activity).inflate(R.layout.layout_float_view, null);
        imageView = contentView.findViewById(R.id.image);
        contentView.findViewById(R.id.feed_back).setOnClickListener(this);
        contentView.findViewById(R.id.share_page).setOnClickListener(this);
        return contentView;
    }


    public void applyData(String data) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        options.inPreferredConfig = Bitmap.Config.RGB_565;    // 默认是Bitmap.Config.ARGB_8888
//        options.inJustDecodeBounds = true;//这个参数设置为true才有效后，获取的bitmap是null
        Bitmap bitmap = BitmapFactory.decodeFile(data, options);
        if (bitmap != null) {
            int bitmapWidth = bitmap.getWidth();
            int bitmapHeight = (int) (bitmapWidth * dp2px(IMAGE_HEIGHT_DP) / dp2px(FLOAT_WIDTH_DP) * 1.0f);
            thumpBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmapWidth, bitmapHeight);
            imageView.setImageBitmap(thumpBitmap);
            mContentView.setVisibility(View.VISIBLE);
        } else {
            mContentView.setVisibility(View.GONE);
        }
    }

    private WindowManager.LayoutParams generateWindowLayoutParam() {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.type = WindowManager.LayoutParams.TYPE_APPLICATION;
        lp.format = PixelFormat.RGBA_8888;
        lp.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        lp.gravity = Gravity.LEFT | Gravity.TOP;
        lp.x = screenWidth - dp2px(FLOAT_WIDTH_DP) - dp2px(FLOAT_MARGIN_RIGHT);
        lp.y = screenHeight / 4;
        return lp;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.feed_back) {
            captureFloatClickListener.capture(1);
        } else if (v.getId() == R.id.share_page) {
            captureFloatClickListener.capture(2);
        }
    }

    public void create() {
        if (!isShow) {
            WindowManager wm = activity.getWindowManager();
            WindowManager.LayoutParams lp = generateWindowLayoutParam();
            mWindowManager = wm;
            mWindowManager.addView(mContentView, lp);
            isShow = true;
        }
    }

    public void destroy() {
        if (isShow) {
            imageView.setImageBitmap(null);
            mWindowManager.removeView(mContentView);
            mWindowManager = null;
            activity = null;
            if (thumpBitmap != null) {
                thumpBitmap.recycle();
                thumpBitmap = null;
            }
            isShow = false;
        }
    }

    public boolean isShow() {
        return isShow;
    }

    public int dp2px(float dp) {
        return (int) (displayMetrics.density * dp + 0.5f);
    }

    public interface ICaptureFloatClickListener {
        void capture(int type);
    }

    private ICaptureFloatClickListener captureFloatClickListener;

    public void setCaptureFloatClickListener(ICaptureFloatClickListener captureFloatClickListener) {
        this.captureFloatClickListener = captureFloatClickListener;
    }
}
