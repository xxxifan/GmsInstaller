package org.coolapk.gmsinstaller.app;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.apmem.tools.layouts.FlowLayout;
import org.coolapk.gmsinstaller.R;
import org.coolapk.gmsinstaller.cloud.CloudHelper;
import org.coolapk.gmsinstaller.model.Gapp;
import org.coolapk.gmsinstaller.util.ViewUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by BobPeng on 2015/3/18.
 */
public class AppPresenter implements SlidingUpPanelLayout.PanelSlideListener {
    public static final int STATUS_INIT = -1;
    public static final int STATUS_NO_ROOT = -2;
    public static final int STATUS_NOT_INSTALLED = 0;
    public static final int STATUS_INSTALLED = 1;
    public static final int STATUS_INSTALL_INCOMPLETE = 2;
    public static final int STATUS_UPDATE_AVAILABLE = 3;
    public static final int STATUS_INSTALLING = 10;

    private Context mContext;

//    private SlidingUpPanelLayout mSlidingPanel;
//    private TextView mStatusTextView;
//    private TextView mCommandTextView;
//    private CheckBox mBasePkgCheck;
//    private CheckBox mExtendCheck;
//    private Button mInstallBtn;
    //    private Button mMoreAppsBtn;
//    private FlowLayout mAppsLayout;

    private List<Gapp> mGappList;
    private String mStatusStr;
    private int mStatus = -1;

    public AppPresenter(View view) {
        mContext = view.getContext();
        mGappList = new ArrayList<>();

//        mStatusTextView = (TextView) view.findViewById(R.id.main_install_status);
//        mCommandTextView = (TextView) view.findViewById(R.id.main_command_status);
//        mBasePkgCheck = (CheckBox) view.findViewById(R.id.main_option_base_pkg);
//        mExtendCheck = (CheckBox) view.findViewById(R.id.main_option_extension);
//        mAppsLayout = (FlowLayout) view.findViewById(R.id.main_option_container);

        OnCheckBoxChangeListener changeListener = new OnCheckBoxChangeListener();
//        mBasePkgCheck.setOnCheckedChangeListener(changeListener);
//        mExtendCheck.setOnCheckedChangeListener(changeListener);

//        mMoreAppsBtn = (Button) view.findViewById(R.id.main_apps_btn);
//        mMoreAppsBtn.setTag(0);
//        mMoreAppsBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                togglePanel();
//            }
//        });

        setStatus(STATUS_INIT);
        initOptionalApps();
    }

    private void initOptionalApps() {
        List<Gapp> gapps = CloudHelper.getOptionalGapps();
        if (gapps.size() == 0) {
            TextView textView = new TextView(mContext);
            textView.setText(R.string.msg_no_more_gapps);
//            mAppsLayout.addView(textView);
        } else {
            int defaultMargin = ViewUtils.dp2px(8);
            int extraMargin = ViewUtils.dp2px(10);
            TextView textView = new TextView(mContext);
            FlowLayout.LayoutParams textParams = new FlowLayout.LayoutParams(-1, -2);
            textParams.leftMargin = extraMargin;
            textView.setLayoutParams(textParams);
//            mAppsLayout.addView(textView);
            for (Gapp gapp : gapps) {
                CheckBox checkBox = new CheckBox(mContext);
                FlowLayout.LayoutParams cbParams = new FlowLayout.LayoutParams(-2, -2);
                cbParams.topMargin = extraMargin;
                cbParams.leftMargin = defaultMargin;
                checkBox.setLayoutParams(cbParams);
                checkBox.setText(gapp.displayName);
//                mAppsLayout.addView(checkBox);
            }
        }
    }

    private void togglePanel() {
    }

    public int getStatus() {
        return mStatus;
    }

    public void setStatus(int status) {
        mStatus = status;
        switch (status) {
            case STATUS_NO_ROOT:
                mStatusStr += mContext.getString(R.string.msg_no_root);
                break;
            case STATUS_INIT:
                mStatusStr = mContext.getString(R.string.msg_check_root);
                break;
            case STATUS_NOT_INSTALLED:
                mStatusStr += mContext.getString(R.string.msg_min_gapps_not_installed);
                break;
            case STATUS_INSTALLED:
                mStatusStr += mContext.getString(R.string.msg_min_gapps_installed);
//                mBasePkgCheck.setEnabled(false);
//                mBasePkgCheck.setChecked(true);
                break;
            case STATUS_INSTALL_INCOMPLETE:
                mStatusStr += mContext.getString(R.string.msg_min_gapps_incomplete);
//                mBasePkgCheck.setChecked(true);
                break;
            case STATUS_UPDATE_AVAILABLE:
                mStatusStr += mContext.getString(R.string.msg_gapps_update_available);
//                mBasePkgCheck.setChecked(true);
                break;
            case STATUS_INSTALLING:
                mStatusStr += mContext.getString(R.string.msg_installing);
                break;
        }

        mStatusStr += "\n";
//        mCommandTextView.setText(mStatusStr);
    }

    public void setOnInstallClickListener(View.OnClickListener listener) {
    }

    @Override
    public void onPanelSlide(View view, float v) {
    }

    @Override
    public void onPanelCollapsed(View view) {
    }

    @Override
    public void onPanelExpanded(View view) {
    }

    @Override
    public void onPanelAnchored(View view) {
    }

    @Override
    public void onPanelHidden(View view) {
    }

    private class OnCheckBoxChangeListener implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (buttonView.isEnabled()) {
//                if (buttonView == mBasePkgCheck) {
//                    if (isChecked) {
//
//                    }
//                } else if (buttonView == mExtendCheck) {
//
//                }
            }
        }
    }
}
