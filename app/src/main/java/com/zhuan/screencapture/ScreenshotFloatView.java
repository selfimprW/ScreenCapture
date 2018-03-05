package com.zhuan.screencapture;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;


public class ScreenshotFloatView implements View.OnClickListener {

    private Activity mActivity;

    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParam;
    private DisplayMetrics dm;

    private View mContentView;

    private int mScreenWidth;
    private int mScreenHeight;

    private boolean isAdded = false;

    private ImageView imageView;

    public ScreenshotFloatView(Activity activity) {
        mActivity = activity;

        dm = activity.getResources().getDisplayMetrics();
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;
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
            captureFloatClickListener.captute(1);
        } else if (v.getId() == R.id.share_page) {
            captureFloatClickListener.captute(2);
        }
    }


    public Activity getActivity() {
        return mActivity;
    }

    public int getScreenWidth() {
        return mScreenWidth;
    }

    public int getScreenHeight() {
        return mScreenHeight;
    }


    public void create() {
        if (!isAdded) {
            WindowManager wm = mActivity.getWindowManager();
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
            mActivity = null;
            isAdded = false;
        }
    }

    public boolean isAdded() {
        return isAdded;
    }

    public int dp2px(float dp) {
        return (int) (dm.density * dp + 0.5f);
    }

    public interface ICaptureFloatClickListener {
        void captute(int type);
    }

    private ICaptureFloatClickListener captureFloatClickListener;

    public void setCaptureFloatClickListener(ICaptureFloatClickListener captureFloatClickListener) {
        this.captureFloatClickListener = captureFloatClickListener;
    }
}
