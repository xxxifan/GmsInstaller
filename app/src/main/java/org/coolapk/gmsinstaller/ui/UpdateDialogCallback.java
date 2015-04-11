package org.coolapk.gmsinstaller.ui;

import com.afollestad.materialdialogs.MaterialDialog;

import org.coolapk.gmsinstaller.cloud.CloudHelper;
import org.coolapk.gmsinstaller.model.AppInfo;

/**
 * Created by xifan on 15-4-11.
 */
public class UpdateDialogCallback extends MaterialDialog.ButtonCallback {
    private AppInfo appInfo;

    public UpdateDialogCallback(AppInfo appInfo) {
        this.appInfo = appInfo;
    }

    @Override
    public void onPositive(MaterialDialog dialog) {
        CloudHelper.downloadUpdate(dialog.getContext(), appInfo.installUrl, appInfo.versionShort + "-"
                + appInfo.version);
    }
}