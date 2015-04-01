package org.coolapk.gmsinstaller.ui;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState;

import org.coolapk.gmsinstaller.CardAdapter;
import org.coolapk.gmsinstaller.MainActivity;
import org.coolapk.gmsinstaller.R;
import org.coolapk.gmsinstaller.app.AppHelper;
import org.coolapk.gmsinstaller.cloud.CloudHelper;
import org.coolapk.gmsinstaller.model.Gpack;
import org.coolapk.gmsinstaller.model.PackageInfo;
import org.coolapk.gmsinstaller.util.ZipUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by BobPeng on 2015/3/27.
 */
public class PanelPresenter implements View.OnClickListener {
    private Context mContext;

    private SlidingUpPanelLayout mPanel;
    private TextView mSlidingTitle;
    private TextView mUpdateTimeText;
    private TextView mPackageSizeText;
    private TextView mPackageDetailsText;
    private Button mInstallBtn;
    private Button mUninstallBtn;

    private int mDisplayIndex;
    private int mColorDisabled;
    private int mColorAccent;

    private List<PackageInfo> mPackageInfos;

    public PanelPresenter(View rootView) {
        mContext = rootView.getContext();
        mPackageInfos = new ArrayList<>();

        mPanel = (SlidingUpPanelLayout) rootView.findViewById(R.id.sliding_up_panel);
        mSlidingTitle = (TextView) rootView.findViewById(R.id.sliding_title);
        mUpdateTimeText = (TextView) rootView.findViewById(R.id.update_time);
        mPackageSizeText = (TextView) rootView.findViewById(R.id.package_size);
        mPackageDetailsText = (TextView) rootView.findViewById(R.id.package_detail);
        mInstallBtn = (Button) rootView.findViewById(R.id.package_install_btn);
        mUninstallBtn = (Button) rootView.findViewById(R.id.package_uninstall_btn);

        String[] descriptions = mContext.getResources().getStringArray(R.array.gapps_description);
        for (String descriptor : descriptions) {
            PackageInfo info = new PackageInfo();
            info.setPackageDescription(descriptor);
            mPackageInfos.add(info);
        }

        mInstallBtn.setOnClickListener(this);
        mUninstallBtn.setOnClickListener(this);
        mInstallBtn.setTag(1);
        mUninstallBtn.setTag(1);

        mColorDisabled = mContext.getResources().getColor(R.color.diabled_text);
        mColorAccent = mContext.getResources().getColor(R.color.pink);
    }

    public void display(int position) {
        mSlidingTitle.setText(CardAdapter.CARD_ITEMS[position]);
        PackageInfo packageInfo = mPackageInfos.get(position);
        Gpack pack = packageInfo.getGpack();
        if (pack == null) {
            mUpdateTimeText.setText(R.string.title_no_info);
            mPackageSizeText.setText(R.string.title_no_info);
            mPackageDetailsText.setText(R.string.title_no_info);
            mInstallBtn.setEnabled(false);
            EventBus.getDefault().post(new MainActivity.CheckUpdateEvent());
        } else {
            mUpdateTimeText.setText(pack.updateTime);
            mPackageSizeText.setText(ZipUtils.getFormatSize(Long.parseLong(pack.packageSize)));
            mPackageDetailsText.setText(packageInfo.getPackageDescription());
            mInstallBtn.setEnabled(true);
        }

        if (packageInfo.isInstalled()) {
            mUninstallBtn.setEnabled(true);
            mUninstallBtn.setTextColor(mColorAccent);
        } else {
            mUninstallBtn.setEnabled(false);
            mUninstallBtn.setTextColor(mColorDisabled);
        }
        showPanel();
        mDisplayIndex = position;
    }

    public void setGappsDetail(List<Gpack> gpackList) {
        // get proper packages from raw data.
        if (gpackList != null) {
            List<Gpack> list = CloudHelper.getProperPackages(gpackList);
            if (list != null) {
                for (int i = 0; i < list.size(); i++) {
                    mPackageInfos.get(i).setGpack(list.get(i));
                }
            }
        }
    }

    public void setInstallStatus(int position, boolean installed) {
        mPackageInfos.get(position).setInstallState(installed);
    }

    public boolean isPanelExpanded() {
        return mPanel.getPanelState() == PanelState.ANCHORED || mPanel.getPanelState() == PanelState.EXPANDED;
    }

    public void collapsePanel() {
        mPanel.setPanelState(PanelState.COLLAPSED);
    }

    public void showPanel() {
        mPanel.setPanelState(PanelState.ANCHORED);
    }

    private void toggleBtnState(Button btn) {
        if (btn.isEnabled()) {
            btn.setEnabled(false);
            btn.setTextColor(mColorAccent);
        } else {
            btn.setEnabled(true);
            btn.setTextColor(mColorDisabled);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mInstallBtn) {
            toggleBtnState(mInstallBtn);
            onInstallClick(mPackageInfos.get(mDisplayIndex).getGpack());

//            if (mPackageInfos.get(mDisplayIndex).isInstalled()) {
//                // TODO alert already installed
//            } else {
//                // TODO Start install
//                if (!mPackageInfos.get(0).isInstalled()) {
//                    // TODO please framework first!
//                } else {
//                    ZipUtils.flash();
//                }
//            }
        } else if (v == mUninstallBtn) {
            // TODO uninstall confirm
        }
    }

    private void onInstallClick(Gpack gpack) {
        if (mInstallBtn.getTag() == 1) {
            String packageName = gpack.packageName;
            // TODO check download, and install
            File targetFile = new File(AppHelper.getExternalFilePath(), packageName);
            if (targetFile.exists()) {
                Log.e("", "exists");
                // TODO check md5 and length, install
            } else {
                Log.e("", "downloadPackage");
                Intent data = new Intent();
                data.putExtra("path", targetFile.getPath());
                CloudHelper.downloadPackage(packageName, data);
                onDownloadStart();
            }
        } else {
            CloudHelper.cancelDownloads();
        }
    }

    private void onDownloadStart() {
        mInstallBtn.setText(R.string.btn_cancel);
        mInstallBtn.setTag(0);
    }
}