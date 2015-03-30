package org.coolapk.gmsinstaller.ui;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.coolapk.gmsinstaller.R;

import java.util.Calendar;

/**
 * Created by BobPeng on 2015/3/26.
 */
public class StatusPresenter {
    public static final int STATUS_NO_ROOT = -2;
    public static final int STATUS_NOT_INSTALLED = -1;
    public static final int STATUS_INIT = 0;
    public static final int STATUS_INSTALLED = 1;
    public static final int STATUS_INSTALL_INCOMPLETE = 2;
    public static final int STATUS_UPDATE_AVAILABLE = 3;
    public static final int STATUS_CHECKING_UPDATES = 11;
    public static final int STATUS_CHECKING_ROOT = 12;
    public static final int STATUS_INSTALLING = 10;

    private static final int ICON_STATE_WARN = -1;
    private static final int ICON_STATE_LOADING = 0;
    private static final int ICON_STATE_DONE = 1;
    private static final int ICON_STATE_ALERT = 2;

    private ProgressBar mStatusProgress;
    private ImageView mStatusIcon;
    private TextView mStatusText;
    private TextView mSubStatusText;
    private Context mContext;

    private int mStatus = STATUS_INIT;
    private int mIconState = ICON_STATE_LOADING;

    public StatusPresenter(View root) {
        mContext = root.getContext();

        mStatusProgress = (ProgressBar) root.findViewById(R.id.status_loading);
        mStatusIcon = (ImageView) root.findViewById(R.id.status_icon);
        mStatusText = (TextView) root.findViewById(R.id.status_text);
        mSubStatusText = (TextView) root.findViewById(R.id.status_sub_text);

        setStatus(STATUS_INIT);
    }

    public int getStatus() {
        return mStatus;
    }

    public void setStatus(int status) {
        mStatus = status;
        switch (status) {
            case STATUS_NO_ROOT:
                setStatusText(R.string.msg_no_root);
                setStatusIconState(ICON_STATE_WARN);
                break;
            case STATUS_INIT:
                setStatusText(R.string.msg_loading);
                setStatusIconState(ICON_STATE_LOADING);
                break;
            case STATUS_NOT_INSTALLED:
                setStatusText(R.string.msg_min_gapps_not_installed);
                setStatusIconState(ICON_STATE_WARN);
                break;
            case STATUS_INSTALLED:
                setStatusText(R.string.msg_min_gapps_installed);
                setStatusIconState(ICON_STATE_DONE);
                break;
            case STATUS_INSTALL_INCOMPLETE:
                setStatusText(R.string.msg_min_gapps_incomplete);
                setStatusIconState(ICON_STATE_ALERT);
                break;
            case STATUS_UPDATE_AVAILABLE:
                setStatusText(R.string.msg_gapps_update_available);
                setStatusIconState(ICON_STATE_ALERT);
                break;
            case STATUS_INSTALLING:
                setStatusText(R.string.msg_installing);
                setStatusIconState(ICON_STATE_LOADING);
                break;
            case STATUS_CHECKING_UPDATES:
                setStatusText(R.string.msg_checking_updates);
                setStatusIconState(ICON_STATE_LOADING);
                break;
            case STATUS_CHECKING_ROOT:
                setStatusText(R.string.msg_check_root);
                setStatusIconState(ICON_STATE_LOADING);
                break;
        }
        Log.e("","on status " + status + " at "+ Calendar.getInstance().getTime().toString());
    }

    public void setStatusIconState(int state, int progress) {
        setStatusIconState(state);
    }

    public void setStatusIconState(int state) {
        if (state == ICON_STATE_LOADING) {
            mStatusProgress.setVisibility(View.VISIBLE);
            mStatusIcon.setVisibility(View.GONE);
        } else {
            mStatusProgress.setVisibility(View.GONE);
            mStatusIcon.setVisibility(View.VISIBLE);
            if (mIconState != state) {
                switch (state) {
                    case ICON_STATE_WARN:
                        // TODO warning icon
                        break;
                    case ICON_STATE_ALERT:
                        break;
                    case ICON_STATE_DONE:
                        break;
                }
            }
            mIconState = state;
        }
    }

    public void setStatusText(int resId) {
        setStatusText(mContext.getString(resId));
    }

    public void setStatusText(String statusText) {
        mStatusText.setText(statusText);
        if (mSubStatusText.getVisibility() == View.VISIBLE) {
            mSubStatusText.setVisibility(View.GONE);
        }
    }

    public void setStatusText(int statusTextRes, int subStatusTextRes) {
        setStatusText(mContext.getString(statusTextRes), mContext.getString(subStatusTextRes));
    }

    public void setStatusText(String statusText, String subStatusText) {
        mStatusText.setText(statusText);
        mSubStatusText.setVisibility(View.VISIBLE);
        mSubStatusText.setText(subStatusText);
    }
}
