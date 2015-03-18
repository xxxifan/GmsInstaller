package org.coolapk.gmsinstaller.app;

import android.content.Context;
import android.widget.TextView;

import org.coolapk.gmsinstaller.model.Gapp;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by BobPeng on 2015/3/18.
 */
public class AppPresenter {

    private TextView mStatusTextView;
    private TextView mCommandTextView;
    private List<Gapp> mGappList;

    private int mStatus = -1;

    public AppPresenter(Context context, TextView statusTextView, TextView commandTextView) {
        mStatusTextView = statusTextView;
        mCommandTextView = commandTextView;
        mGappList = new ArrayList<>();
    }

    public int getStatus() {
        return mStatus;
    }

    public void setStatus(int status) {
        mStatus = status;
        switch (status) {
            case -2:
                break;
            case -1:
                break;
            case 0:
                break;
            case 1:
                break;
            case 2:
                break;
        }
    }
}
