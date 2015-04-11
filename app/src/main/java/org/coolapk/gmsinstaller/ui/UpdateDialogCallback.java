package org.coolapk.gmsinstaller.ui;

import com.afollestad.materialdialogs.MaterialDialog;

import org.coolapk.gmsinstaller.app.AppHelper;
import org.coolapk.gmsinstaller.cloud.CloudHelper;
import org.coolapk.gmsinstaller.model.AppInfo;

/**
 * Created by xifan on 15-4-11.
 */
public class UpdateDialogCallback extends MaterialDialog.ButtonCallback {
    private AppInfo appInfo;
    private boolean showIgnore;

    public UpdateDialogCallback(AppInfo appInfo, boolean showIgnore) {
        this.appInfo = appInfo;
        this.showIgnore = showIgnore;
    }

    @Override
    public void onPositive(MaterialDialog dialog) {
        CloudHelper.downloadUpdate(dialog.getContext(), appInfo.installUrl, appInfo.versionShort + "-"
                + appInfo.version);
    }

    @Override
    public void onNegative(MaterialDialog dialog) {
        super.onNegative(dialog);
        if (showIgnore) {
            AppHelper.getPrefs().edit().putLong(AppHelper.KEY_IGNORE_UPDATE,
                    Long.parseLong(AppHelper.getSimpleTimestamp())).apply();
        }
    }
}