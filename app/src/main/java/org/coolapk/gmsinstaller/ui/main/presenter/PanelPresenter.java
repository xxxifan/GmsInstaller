package org.coolapk.gmsinstaller.ui.main.presenter;

import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState;

import org.coolapk.gmsinstaller.R;
import org.coolapk.gmsinstaller.app.AppHelper;
import org.coolapk.gmsinstaller.cloud.CloudHelper;
import org.coolapk.gmsinstaller.model.Gpack;
import org.coolapk.gmsinstaller.model.PackageInfo;
import org.coolapk.gmsinstaller.ui.CardAdapter;
import org.coolapk.gmsinstaller.ui.UiPresenter;
import org.coolapk.gmsinstaller.ui.main.MainActivity;
import org.coolapk.gmsinstaller.util.ZipUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


import static org.coolapk.gmsinstaller.ui.main.presenter.StatusPresenter.STATUS_EXTENSION_INSTALLED;
import static org.coolapk.gmsinstaller.ui.main.presenter.StatusPresenter.STATUS_EXTENSION_NOT_INSTALLED;
import static org.coolapk.gmsinstaller.ui.main.presenter.StatusPresenter.STATUS_MINIMAL_INSTALLED;
import static org.coolapk.gmsinstaller.ui.main.presenter.StatusPresenter.STATUS_MINIMAL_NOT_INSTALLED;

/**
 * Created by BobPeng on 2015/3/27.
 */
public class PanelPresenter extends UiPresenter implements View.OnClickListener {
    private SlidingUpPanelLayout mPanel;
    private TextView mSlidingTitle;
    private TextView mUpdateTimeText;
    private TextView mPackageSizeText;
    private TextView mPackageDetailsText;
    private TextView mInstallBtn;

    private int mDisplayIndex;
    private int mColorDisabled;
    private int mColorAccent;

    private int mWorkingIndex = -1;

    private List<PackageInfo> mPackageInfos;

    public PanelPresenter(View rootView) {
        super(rootView);
        mPackageInfos = new ArrayList<>();
        mColorDisabled = getContext().getResources().getColor(R.color.diabled_text);
        mColorAccent = getContext().getResources().getColor(R.color.pink);

        String[] descriptions = getContext().getResources().getStringArray(R.array.gapps_description);
        for (String descriptor : descriptions) {
            PackageInfo info = new PackageInfo();
            info.setPackageDescription(descriptor);
            mPackageInfos.add(info);
        }
    }

    @Override
    protected void initView(View rootView) {
        mPanel = (SlidingUpPanelLayout) rootView.findViewById(R.id.sliding_up_panel);
        mSlidingTitle = (TextView) rootView.findViewById(R.id.sliding_title);
        mUpdateTimeText = (TextView) rootView.findViewById(R.id.update_time);
        mPackageSizeText = (TextView) rootView.findViewById(R.id.package_size);
        mPackageDetailsText = (TextView) rootView.findViewById(R.id.package_detail);
        mInstallBtn = (TextView) rootView.findViewById(R.id.package_install_btn);

        mPanel.setPanelState(PanelState.HIDDEN);
        mInstallBtn.setOnClickListener(this);
        mInstallBtn.setTag(1);
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
            mPackageSizeText.setText(ZipUtils.getFormatSize(pack.packageSize));
            mPackageDetailsText.setText(packageInfo.getPackageDescription());
            toggleBtnState(mInstallBtn, true);
        }

        showPanel();
        mDisplayIndex = position;

        // delay check update
        if (pack == null) {
            postEvent(new MainActivity.CheckUpdateEvent());
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

    public void setInstallStatus(boolean installed) {
        setInstallStatus(mWorkingIndex, installed);
    }

    public void setInstallStatus(int position, boolean installed) {
        if (position > -1) {
            mPackageInfos.get(position).setInstallState(installed);

            int status = position == 0 ?
                    (installed ? STATUS_MINIMAL_INSTALLED : STATUS_MINIMAL_NOT_INSTALLED)
                    : (installed ? STATUS_EXTENSION_INSTALLED : STATUS_EXTENSION_NOT_INSTALLED);

            postStickyEvent(new MainActivity.StatusEvent(status));
        }
    }

    /**
     * @param type package type
     * @return package position in ui list with this type
     */
    public int getTypePosition(int type) {
        return type == 1 ? 0 : 1;
    }

    /**
     * @param position package position in ui list
     * @return package type in ui list with this position.
     */
    public int getPositionType(int position) {
        return position + 1;
    }

    public boolean isPanelExpanded() {
        return mPanel.getPanelState() == PanelState.ANCHORED || mPanel.getPanelState() == PanelState.EXPANDED;
    }

    public void collapsePanel() {
        mPanel.setPanelState(PanelState.COLLAPSED);
    }

    public void showPanel() {
        mPanel.setPanelState(PanelState.EXPANDED);
    }

    private void toggleBtnState(TextView btn, boolean on) {
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
            collapsePanel();
        }
    }

    private void onInstallClick() {
        if (mPackageInfos.get(mDisplayIndex).isInstalled()) {
            MaterialDialog.ButtonCallback buttonCallback = new MaterialDialog.ButtonCallback() {
                @Override
                public void onPositive(MaterialDialog dialog) {
                    startInstallTask();
                }
            };

            new MaterialDialog.Builder(getContext())
                    .content(R.string.msg_already_installed)
                    .positiveText(R.string.btn_ok)
                    .negativeText(R.string.btn_cancel)
                    .callback(buttonCallback)
                    .build()
                    .show();
        } else {
            if (mDisplayIndex == 1 && !mPackageInfos.get(0).isInstalled()) {
                new MaterialDialog.Builder(getContext())
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
                    Toast.makeText(getContext(), R.string.msg_error_interrupt, Toast.LENGTH_SHORT).show();
                    return null;
                }
                mWorkingIndex = mDisplayIndex;

                Gpack gpack = mPackageInfos.get(mWorkingIndex).getGpack();
                String packageName = gpack.packageName;
                File targetFile = new File(AppHelper.getAppExternalPath(), packageName);
                if (targetFile.exists() && checkDownload(gpack, targetFile)) {
                    postEvent(new MainActivity.InstallEvent(packageName));
                    mInstallBtn.setTag(0);
                } else {
                    // start download
                    Intent data = new Intent();
                    data.putExtra("path", targetFile.getPath());
                    CloudHelper.downloadPackage(packageName, data);
                    mInstallBtn.setTag(0);
                }
                return null;
            }
        }.execute();
    }

    private boolean checkDownload(Gpack gpack, File file) {
        boolean lengthMatch = file.length() == gpack.packageSize;
        if (lengthMatch && ZipUtils.getFileMd5(file).equals(gpack.md5)) {
            return true;
        }

        // clear unexpected file
        if (AppHelper.getPrefs(AppHelper.PREFERENCE_DOWNLOAD_FILES).getLong(gpack
                .packageName, 0l) == 0l) {
            file.delete();
        }
        return false;
    }

    public void onInstallFinished() {
        mInstallBtn.setTag(1);
        mWorkingIndex = -1;
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
