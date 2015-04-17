package org.coolapk.gmsinstaller.ui.main;

import com.afollestad.materialdialogs.MaterialDialog;

import org.coolapk.gmsinstaller.ui.main.presenter.StatusPresenter;

import de.greenrobot.event.EventBus;

/**
 * Created by xifan on 15-4-17.
 */
public class InstallConfirmCallback extends MaterialDialog.ButtonCallback {
    private MainActivity.InstallEvent event;

    public InstallConfirmCallback(MainActivity.InstallEvent event) {
        this.event = event;
    }

    @Override
    public void onPositive(MaterialDialog dialog) {
        EventBus.getDefault().post(event);
    }

    @Override
    public void onNegative(MaterialDialog dialog) {
        EventBus.getDefault().post(new MainActivity.StatusEvent(StatusPresenter.STATUS_INSTALL_CANCELED));
    }
}
