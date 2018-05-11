package com.zhuan.screencapture;

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

    private FragmentActivity activity;

    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParam;
    private DisplayMetrics displayMetrics;

    private View mContentView;

    private int mScreenWidth;
    private int mScreenHeight;

    private boolean isAdded = false;

    private ImageView imageView;

    public ScreenshotFloatView(FragmentActivity activity) {
        this.activity = activity;

        displayMetrics = activity.getResources().getDisplayMetrics();
        mScreenWidth = displayMetrics.widthPixels;
        mScreenHeight = displayMetrics.heightPixels;
        mContentView = onCreateView();
        if (mContentView == null) {
            throw new IllegalArgumentException("No content view was found!");
        }
    }

    protected View onCreateView() {
        View contentView = LayoutInflater.from(getActivity()).inflate(R.layout.layout_float_view, null);
        imageView = contentView.findViewById(R.id.image);
        contentView.findViewById(R.id.feed_back).setOnClickListener(this);
        contentView.findViewById(R.id.share_page).setOnClickListener(this);
        return contentView;
    }

    public void applyData(String data) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8;
        Bitmap bitmap = BitmapFactory.decodeFile(data);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        }
    }

//    public void applyData(Bitmap source) {
//        if (source != null) {
//            int[] size = generateWindowSize();
//            Bitmap bitmap = Bitmap.createScaledBitmap(source, size[0], size[1], false);
//            imageView.setImageBitmap(bitmap);
//        }
//    }

    private WindowManager.LayoutParams generateWindowLayoutParam() {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.width = dp2px(81);
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.type = WindowManager.LayoutParams.TYPE_APPLICATION;
        lp.format = PixelFormat.RGBA_8888;
        lp.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        lp.gravity = Gravity.LEFT | Gravity.TOP;
        lp.x = getScreenWidth() - dp2px(81) - dp2px(10);
        lp.y = getScreenHeight() / 4;
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

    public FragmentActivity getActivity() {
        return activity;
    }

    public int getScreenWidth() {
        return mScreenWidth;
    }

    public int getScreenHeight() {
        return mScreenHeight;
    }


    public void create() {
        if (!isAdded) {
            WindowManager wm = activity.getWindowManager();
            WindowManager.LayoutParams lp = generateWindowLayoutParam();
            mWindowManager = wm;
            mLayoutParam = lp;
            mWindowManager.addView(mContentView, mLayoutParam);
            isAdded = true;
        }
    }

    public void destroy() {
        if (isAdded) {
            mWindowManager.removeView(mContentView);
            mWindowManager = null;
            mLayoutParam = null;
            activity = null;
            isAdded = false;
        }
    }

    public boolean isAdded() {
        return isAdded;
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
