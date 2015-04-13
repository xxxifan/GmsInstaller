package org.coolapk.gmsinstaller.ui.main.presenter;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.coolapk.gmsinstaller.R;
import org.coolapk.gmsinstaller.ui.CardAdapter;
import org.coolapk.gmsinstaller.ui.CardItemDecoration;
import org.coolapk.gmsinstaller.ui.CardLayoutManager;
import org.coolapk.gmsinstaller.ui.UiPresenter;
import org.coolapk.gmsinstaller.ui.main.MainActivity;

import de.greenrobot.event.EventBus;

/**
 * Created by BobPeng on 2015/3/26.
 */
public class StatusPresenter extends UiPresenter {
    public static final int STATUS_DOWNLOADING_FAILED = -13;
    public static final int STATUS_NO_ROOT = -12;
    public static final int STATUS_EXTENSION_NOT_INSTALLED = -2;
    public static final int STATUS_MINIMAL_NOT_INSTALLED = -1;
    public static final int STATUS_INIT = 0;
    public static final int STATUS_MINIMAL_INSTALLED = 1;
    public static final int STATUS_EXTENSION_INSTALLED = 2;
    public static final int STATUS_MINIMAL_INSTALL_INCOMPLETE = 3;
    public static final int STATUS_UPDATE_AVAILABLE = 4;
    public static final int STATUS_DOWNLOAD_CANCELED = 5;
    public static final int STATUS_INSTALL_FINISHED = 6;
    // working status
    public static final int STATUS_INSTALLING = 11;
    public static final int STATUS_CHECKING_ROOT = 13;
    public static final int STATUS_DOWNLOADING = 14;

    private static final int ICON_STATE_WARN = -1;
    private static final int ICON_STATE_LOADING = 0;
    private static final int ICON_STATE_DONE = 1;
    private static final int ICON_STATE_ALERT = 2;

    private ProgressBar mStatusProgress;
    private ImageView mStatusIcon;
    private TextView mStatusText;
    private TextView mSubStatusText;
    private Button mCancelBtn;
    private CardAdapter mAdapter;

    private int mStatus = STATUS_INIT;
    private int mIconState = ICON_STATE_LOADING;

    public StatusPresenter(View root) {
        super(root);
    }

    @Override
    protected void initView(View rootView) {
        mStatusProgress = (ProgressBar) rootView.findViewById(R.id.status_loading);
        mStatusIcon = (ImageView) rootView.findViewById(R.id.status_icon);
        mStatusText = (TextView) rootView.findViewById(R.id.status_text);
        mSubStatusText = (TextView) rootView.findViewById(R.id.status_sub_text);
        mCancelBtn = (Button) rootView.findViewById(R.id.status_cancel_btn);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.main_list);
        recyclerView.addItemDecoration(new CardItemDecoration(getContext()));
        recyclerView.setLayoutManager(new CardLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        mAdapter = new CardAdapter(getContext());
        mAdapter.setOnItemClickListener(new CardAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (position < 2) {
                    MainActivity.PanelDisplayEvent event = new MainActivity.PanelDisplayEvent();
                    event.position = position;
                    postEvent(event);
                } else {
                    onGappsItemClick();
                }
            }
        });
        recyclerView.setAdapter(mAdapter);
    }

    private void onGappsItemClick() {
        new MaterialDialog.Builder(getContext())
                .title(R.string.title_alert)
                .content(R.string.title_gapps_browse)
                .positiveText(R.string.btn_go_market)
                .negativeText(R.string.btn_cancel)
                .callback(new JumpButtonCallback())
                .build()
                .show();
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
                EventBus.getDefault().removeStickyEvent(MainActivity.StatusEvent.class);
                break;
            case STATUS_MINIMAL_INSTALLED:
                setStatusText(R.string.msg_min_gapps_installed);
                setStatusIconState(ICON_STATE_DONE);
                mAdapter.setInstallStatus(0, true);
                EventBus.getDefault().removeStickyEvent(MainActivity.StatusEvent.class);
                break;
            case STATUS_EXTENSION_NOT_INSTALLED:
                mAdapter.setInstallStatus(1, false);
                EventBus.getDefault().removeStickyEvent(MainActivity.StatusEvent.class);
                break;
            case STATUS_EXTENSION_INSTALLED:
                mAdapter.setInstallStatus(1, true);
                EventBus.getDefault().removeStickyEvent(MainActivity.StatusEvent.class);
                break;
            case STATUS_MINIMAL_INSTALL_INCOMPLETE:
                setStatusText(R.string.msg_min_gapps_incomplete);
                setStatusIconState(ICON_STATE_ALERT);
                mAdapter.setInstallStatus(0, false);
                EventBus.getDefault().removeStickyEvent(MainActivity.StatusEvent.class);
                break;
            case STATUS_UPDATE_AVAILABLE:
                setStatusText(R.string.msg_gapps_update_available);
                setStatusIconState(ICON_STATE_ALERT);
                break;
            case STATUS_INSTALLING:
                setStatusText(R.string.msg_installing);
                setStatusIconState(ICON_STATE_LOADING);
                break;
            case STATUS_CHECKING_ROOT:
                setStatusText(R.string.msg_check_root);
                setStatusIconState(ICON_STATE_LOADING);
                break;
            case STATUS_DOWNLOADING:
                setStatusText(R.string.msg_start_download);
                setStatusIconState(ICON_STATE_LOADING);
                break;
            case STATUS_DOWNLOAD_CANCELED:
                setStatusText(R.string.msg_download_canceled);
                setStatusIconState(ICON_STATE_ALERT);
                EventBus.getDefault().removeStickyEvent(MainActivity.StatusEvent.class);
                break;
            case STATUS_DOWNLOADING_FAILED:
                setStatusText(R.string.msg_download_failed);
                setStatusIconState(ICON_STATE_WARN);
                EventBus.getDefault().removeStickyEvent(MainActivity.StatusEvent.class);
                break;
            case STATUS_INSTALL_FINISHED:
                setStatusText(R.string.btn_done);
                setStatusIconState(ICON_STATE_DONE);
                EventBus.getDefault().removeStickyEvent(MainActivity.StatusEvent.class);
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
        setStatusText(getContext().getString(resId));
    }

    public void setStatusText(String statusText) {
        mStatusText.setText(statusText);
        if (mSubStatusText.getVisibility() == View.VISIBLE) {
            mSubStatusText.setVisibility(View.GONE);
        }
    }

    public void setStatusText(int statusTextRes, int subStatusTextRes) {
        setStatusText(getContext().getString(statusTextRes), getContext().getString(subStatusTextRes));
    }

    public void setStatusText(String statusText, String subStatusText) {
        mStatusText.setText(statusText);
        mSubStatusText.setVisibility(View.VISIBLE);
        mSubStatusText.setText(subStatusText);
    }

    public void setupCancelBtn(boolean visible, View.OnClickListener callback) {
        mCancelBtn.setVisibility(visible ? View.VISIBLE : View.GONE);
        mCancelBtn.setOnClickListener(callback);
    }

    private class JumpButtonCallback extends MaterialDialog.ButtonCallback {
        @Override
        public void onPositive(MaterialDialog dialog) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setData(Uri.parse("coolmarket://apklist?developer=Google%20Inc."));
                getContext().startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.coolapk.com"
                        + "/apk/search?q=developer:Google%20Inc."));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(intent);
            }
        }
    }
}
