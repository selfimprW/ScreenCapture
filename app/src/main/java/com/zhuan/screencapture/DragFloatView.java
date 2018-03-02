package com.zhuan.screencapture;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

/**
 * 抽象悬浮窗<br>
 * 需要根据不同的数据源，并复写相应方法定制不同的事件处理方案。</p>
 */
public abstract class DragFloatView<Data> {

    private Activity mActivity;

    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParam;
    private DisplayMetrics dm;

    private View mContentView;

    private int mScreenWidth;
    private int mScreenHeight;

    private boolean isAdded = false;

    public DragFloatView(Activity activity) {
        mActivity = activity;

        dm = activity.getResources().getDisplayMetrics();
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;

        mContentView = onCreateView();
        if (mContentView == null) {
            throw new IllegalArgumentException("No content view was found!");
        }
    }

    protected abstract View onCreateView();

    public abstract void applyData(Data data);

    public View getContentView() {
        return mContentView;
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

    protected abstract WindowManager.LayoutParams generateWindowLayoutParam();

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
}
