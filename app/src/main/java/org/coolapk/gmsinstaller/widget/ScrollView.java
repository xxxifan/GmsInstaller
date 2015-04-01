package org.coolapk.gmsinstaller.widget;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by xifan on 15-3-31.
 */
public class ScrollView extends android.widget.ScrollView {
    private OnScrollListener mListener;

    public ScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
        if (mListener != null) {
            mListener.onScroll(scrollX, scrollY, clampedX, clampedY);
        }
    }

    public void setOnScrollListener(OnScrollListener listener) {
        mListener = listener;
    }

    public interface OnScrollListener {
        void onScroll(int scrollX, int scrollY, boolean clampedX, boolean clampedY);
    }
}
