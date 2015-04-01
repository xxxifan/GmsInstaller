package org.coolapk.gmsinstaller.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.coolapk.gmsinstaller.CardAdapter;
import org.coolapk.gmsinstaller.CardItemDecoration;
import org.coolapk.gmsinstaller.CardLayoutManager;
import org.coolapk.gmsinstaller.MainActivity;
import org.coolapk.gmsinstaller.R;

import de.greenrobot.event.EventBus;

/**
 * Created by BobPeng on 2015/3/26.
 */
public class StatusPresenter {
    public static final int STATUS_NO_ROOT = -12;
    public static final int STATUS_EXTENSION_NOT_INSTALLED = -2;
    public static final int STATUS_MINIMAL_NOT_INSTALLED = -1;
    public static final int STATUS_INIT = 0;
    public static final int STATUS_MINIMAL_INSTALLED = 1;
    public static final int STATUS_EXTENSION_INSTALLED = 2;
    public static final int STATUS_MINIMAL_INSTALL_INCOMPLETE = 3;
    public static final int STATUS_UPDATE_AVAILABLE = 4;
    public static final int STATUS_INSTALLING = 10;
    public static final int STATUS_CHECKING_UPDATES = 11;
    public static final int STATUS_CHECKING_ROOT = 12;


    private static final int ICON_STATE_WARN = -1;
    private static final int ICON_STATE_LOADING = 0;
    private static final int ICON_STATE_DONE = 1;
    private static final int ICON_STATE_ALERT = 2;

    private ProgressBar mStatusProgress;
    private ImageView mStatusIcon;
    private TextView mStatusText;
    private TextView mSubStatusText;
    private CardAdapter mAdapter;
    private Context mContext;

    private int mStatus = STATUS_INIT;
    private int mIconState = ICON_STATE_LOADING;

    public StatusPresenter(View root) {
        mContext = root.getContext();

        mStatusProgress = (ProgressBar) root.findViewById(R.id.status_loading);
        mStatusIcon = (ImageView) root.findViewById(R.id.status_icon);
        mStatusText = (TextView) root.findViewById(R.id.status_text);
        mSubStatusText = (TextView) root.findViewById(R.id.status_sub_text);

        RecyclerView recyclerView = (RecyclerView) root.findViewById(R.id.main_list);
        recyclerView.addItemDecoration(new CardItemDecoration(mContext));
        recyclerView.setLayoutManager(new CardLayoutManager(mContext));
        recyclerView.setHasFixedSize(true);
        mAdapter = new CardAdapter(mContext);
        mAdapter.setOnItemClickListener(new CardAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (position < 2) {
                    MainActivity.PanelDisplayEvent event = new MainActivity.PanelDisplayEvent();
                    event.position = position;
                    EventBus.getDefault().post(event);
                } else {
                    // view gapps in coolapk
                    Toast.makeText(mContext, "Check it in coolmarket", Toast.LENGTH_SHORT).show();
                }
            }
        });
        recyclerView.setAdapter(mAdapter);

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
            case STATUS_MINIMAL_NOT_INSTALLED:
                setStatusText(R.string.msg_min_gapps_not_installed);
                setStatusIconState(ICON_STATE_WARN);
                mAdapter.setInstallStatus(0, false);
                break;
            case STATUS_MINIMAL_INSTALLED:
                setStatusText(R.string.msg_min_gapps_installed);
                setStatusIconState(ICON_STATE_DONE);
                mAdapter.setInstallStatus(0, true);
                break;
            case STATUS_EXTENSION_NOT_INSTALLED:
                mAdapter.setInstallStatus(1, false);
                break;
            case STATUS_EXTENSION_INSTALLED:
                mAdapter.setInstallStatus(1, true);
                break;
            case STATUS_MINIMAL_INSTALL_INCOMPLETE:
                setStatusText(R.string.msg_min_gapps_incomplete);
                setStatusIconState(ICON_STATE_ALERT);
                mAdapter.setInstallStatus(0, false);
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
