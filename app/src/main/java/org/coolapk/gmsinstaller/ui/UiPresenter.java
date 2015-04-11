package org.coolapk.gmsinstaller.ui;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import de.greenrobot.event.EventBus;

/**
 * Created by xifan on 15-4-9.
 */
public abstract class UiPresenter {
    private Context mContext;

    public UiPresenter(View rootView) {
        mContext = rootView.getContext();
        initView(rootView);
    }

    protected Context getContext() {
        return mContext;
    }

    protected Activity getActivity() {
        return (Activity) mContext;
    }

    protected void postEvent(Object event) {
        EventBus.getDefault().post(event);
    }

    protected void postStickyEvent(Object event) {
        EventBus.getDefault().postSticky(event);
    }

    protected abstract void initView(View rootView);

}
