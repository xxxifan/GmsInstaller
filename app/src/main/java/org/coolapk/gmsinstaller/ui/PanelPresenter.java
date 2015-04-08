package org.coolapk.gmsinstaller.ui;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
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
            toggleBtnState(mInstallBtn, false);
        } else {
            mUpdateTimeText.setText(pack.updateTime);
            mPackageSizeText.setText(ZipUtils.getFormatSize(Long.parseLong(pack.packageSize)));
            mPackageDetailsText.setText(packageInfo.getPackageDescription());
            toggleBtnState(mInstallBtn, true);
        }

        toggleBtnState(mUninstallBtn, packageInfo.isInstalled());

        showPanel();
        mDisplayIndex = position;

        // delay check update
        if (pack == null) {
            EventBus.getDefault().post(new MainActivity.CheckUpdateEvent());
        }
    }

    public void setGappsDetail(List<Gpack> gpackList) {
        // get proper packages from raw data.
        if (gpackList != null) {
            List<Gpack> list = CloudHelper.getProperPackages(gpackList);
            if (list != null) {
                for (int i = 0; i < list.size(); i++) {
                    mPackageInfos.get(i).setGpack(list.get(i));
                }

                if (isPanelExpanded()) {
                    display(mDisplayIndex);
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

    private void toggleBtnState(Button btn, boolean on) {
        if (on) {
            btn.setEnabled(true);
            btn.setTextColor(mColorAccent);
        } else {
            btn.setEnabled(false);
            btn.setTextColor(mColorDisabled);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mInstallBtn) {
            if (mInstallBtn.getTag() == 1) {
                onInstallClick();
            }
        } else if (v == mUninstallBtn) {
            if (mUninstallBtn.getTag() == 1) {

            }
            // TODO uninstall confirm
        }
        collapsePanel();
    }

    private void onInstallClick() {
        if (mPackageInfos.get(mDisplayIndex).isInstalled()) {
            MaterialDialog.ButtonCallback buttonCallback = new MaterialDialog.ButtonCallback() {
                @Override
                public void onPositive(MaterialDialog dialog) {
                    startInstallTask();
                }
            };

            new MaterialDialog.Builder(mContext)
                    .content(R.string.msg_already_installed)
                    .positiveText(R.string.btn_ok)
                    .negativeText(R.string.btn_cancel)
                    .callback(buttonCallback)
                    .build()
                    .show();
        } else {
            if (mDisplayIndex == 1 && !mPackageInfos.get(0).isInstalled()) {
                new MaterialDialog.Builder(mContext)
                        .content(R.string.msg_framework_need)
                        .positiveText(R.string.btn_ok)
                        .build()
                        .show();
            } else {
                // start to work :)
                startInstallTask();
            }
        }
    }

    private void startInstallTask() {
        new AsyncTask<Void, Void, Void>() {
            protected Void doInBackground(Void... params) {
                if (mDisplayIndex < 0) {
                    Toast.makeText(mContext, R.string.msg_error_interrupt, Toast.LENGTH_SHORT).show();
                    return null;
                }

                Gpack gpack = mPackageInfos.get(mDisplayIndex).getGpack();
                String packageName = gpack.packageName;
                File targetFile = new File(AppHelper.getExternalFilePath(), packageName);
                if (targetFile.exists() && checkDownload(gpack, targetFile)) {
                    Log.e("", "INSTALL");
                    EventBus.getDefault().post(new MainActivity.InstallEvent(packageName));
                    mInstallBtn.setTag(0);
                } else {
                    // start download
                    Intent data = new Intent();
                    data.putExtra("path", targetFile.getPath());
                    CloudHelper.downloadPackage(packageName, data);
                    Log.e("", "downloadPackage");
                    mInstallBtn.setTag(0);
                }
                return null;
            }
        }.execute();
    }

    private boolean checkDownload(Gpack gpack, File file) {
        if (ZipUtils.getFileMd5(file).equals(gpack.md5) && file.length() == Long.parseLong(gpack
                .packageSize)) {
            return true;
        } else if (AppHelper.getPrefs(AppHelper.PREFERENCE_DOWNLOAD_FILES).getLong(gpack
                .packageName, 0l) == 0l) {
            // clear unexpected file
            file.delete();
            Log.e("", "clear unexpected file");
        }
        return false;
    }

    public void onInstallFinished() {
        mInstallBtn.setTag(1);
    }

    public void onUninstallFinished() {
        mUninstallBtn.setTag(1);
    }

    public Gpack getGpack(String packageName) {
        Gpack gpack;
        for (PackageInfo info : mPackageInfos) {
            gpack = info.getGpack();
            if (info.getGpack().packageName.equals(packageName)) {
                return gpack;
            }
        }
        return null;
    }
}
