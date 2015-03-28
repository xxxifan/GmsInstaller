package org.coolapk.gmsinstaller.ui;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.coolapk.gmsinstaller.R;

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
    public static final int STATUS_INSTALLING = 10;

    private ImageView mStatusIcon;
    private TextView mStatusText;
    private TextView mSubStatusText;
    private Context mContext;

    private int mStatus = STATUS_INIT;

    public StatusPresenter(View root) {
        mContext = root.getContext();

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
                mStatusText.setText(R.string.msg_no_root);
                break;
            case STATUS_INIT:
                mStatusText.setText(R.string.msg_check_root);
                break;
            case STATUS_NOT_INSTALLED:
                mStatusText.setText(R.string.msg_min_gapps_not_installed);
                break;
            case STATUS_INSTALLED:
                mStatusText.setText(R.string.msg_min_gapps_installed);
                break;
            case STATUS_INSTALL_INCOMPLETE:
                mStatusText.setText(R.string.msg_min_gapps_incomplete);
                break;
            case STATUS_UPDATE_AVAILABLE:
                mStatusText.setText(R.string.msg_gapps_update_available);
                break;
            case STATUS_INSTALLING:
                mStatusText.setText(R.string.msg_installing);
                break;
        }

    }
}
