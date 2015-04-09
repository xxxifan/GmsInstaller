package org.coolapk.gmsinstaller;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.coolapk.gmsinstaller.app.AppHelper;
import org.coolapk.gmsinstaller.cloud.CloudHelper;
import org.coolapk.gmsinstaller.cloud.DownloadEvent;
import org.coolapk.gmsinstaller.cloud.DownloadService;
import org.coolapk.gmsinstaller.model.Gpack;
import org.coolapk.gmsinstaller.ui.ChooserPresenter;
import org.coolapk.gmsinstaller.ui.PanelPresenter;
import org.coolapk.gmsinstaller.ui.StatusPresenter;
import org.coolapk.gmsinstaller.util.CommandUtils;
import org.coolapk.gmsinstaller.util.ViewUtils;
import org.coolapk.gmsinstaller.util.ZipUtils;
import org.coolapk.gmsinstaller.widget.ScrollView;

import java.util.List;

import de.greenrobot.event.EventBus;


import static org.coolapk.gmsinstaller.ui.StatusPresenter.STATUS_CHECKING_ROOT;
import static org.coolapk.gmsinstaller.ui.StatusPresenter.STATUS_DOWNLOADING;
import static org.coolapk.gmsinstaller.ui.StatusPresenter.STATUS_DOWNLOADING_FAILED;
import static org.coolapk.gmsinstaller.ui.StatusPresenter.STATUS_DOWNLOAD_CANCELED;
import static org.coolapk.gmsinstaller.ui.StatusPresenter.STATUS_INIT;
import static org.coolapk.gmsinstaller.ui.StatusPresenter.STATUS_INSTALLING;
import static org.coolapk.gmsinstaller.ui.StatusPresenter.STATUS_INSTALL_FINISHED;
import static org.coolapk.gmsinstaller.ui.StatusPresenter.STATUS_NO_ROOT;

public class MainActivity extends ActionBarActivity {

    private StatusPresenter mStatusUi;
    private PanelPresenter mPanelUi;
    private ChooserPresenter mChooserUi;

    private boolean mIsServiceRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        initView();
        setTitle(R.string.app_mark);
        mIsServiceRunning = AppHelper.isServiceRunning(DownloadService.class.getName());
    }

    private void initView() {
        setupToolbar();
        View root = getWindow().getDecorView();
        mStatusUi = new StatusPresenter(root);
        mPanelUi = new PanelPresenter(root);
        mChooserUi = new ChooserPresenter(root);
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.primary_text_disabled_material_light));
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mChooserUi.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mIsServiceRunning && mStatusUi.getStatus() == STATUS_INIT) {
            postEvent(new CheckDataEvent());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // correct bottom height to scroll more smoother
        correctBottomHeight();
        return super.onCreateOptionsMenu(menu);
    }

    private void correctBottomHeight() {
        ScrollView scrollView = (ScrollView) findViewById(R.id.main_scroller);
        int count = scrollView.getChildCount();
        int childHeight = 0;
        for (int i = 0; i < count; i++) {
            childHeight += scrollView.getChildAt(i).getMeasuredHeight();
        }

        int toolbarHeight = ViewUtils.dp2px(56);
        int diff = childHeight - scrollView.getMeasuredHeight();
        if (diff > 0 && diff < toolbarHeight) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                diff += ViewUtils.dp2px(8);
            }
            findViewById(R.id.sliding_main).setPadding(0, 0, 0, diff);
        }
    }

    public void postEvent(Object event) {
        EventBus.getDefault().post(event);
    }

    /**
     * CheckData
     */
    public void onEventBackgroundThread(CheckDataEvent event) {
        postEvent(new StatusEvent(STATUS_INIT));
        CommandUtils.initEnvironment();

        // checking updates state
        postEvent(new CheckUpdateEvent());

        postEvent(new StatusEvent(STATUS_CHECKING_ROOT));
        boolean hasRoot = CommandUtils.checkRootPermission();
        if (hasRoot) {
            checkInstallStatus();
        } else {
            postEvent(new StatusEvent(STATUS_NO_ROOT));
        }
    }

    public void onEventBackgroundThread(CheckUpdateEvent event) {
        List<Gpack> gpacks = CloudHelper.getGpackList();
        mPanelUi.setGappsDetail(gpacks);

        if (gpacks == null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, R.string.msg_check_update_failed, Toast
                            .LENGTH_SHORT).show();
                }
            });
        }
    }

    public void onEventBackgroundThread(InstallEvent event) {
        if (mStatusUi.getStatus() == STATUS_INSTALLING) {
            return;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mStatusUi.setupCancelBtn(false, null);
                onEventMainThread(new StatusEvent(STATUS_INSTALLING));
            }
        });

        final boolean result;

        // start install
        if (event.isLocal) {
            result = ZipUtils.install(mChooserUi.getWorkingGpack(), true);
        } else {
            result = ZipUtils.install(mPanelUi.getGpack(event.filename), false);
        }


        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onEventMainThread(new StatusEvent(STATUS_INSTALL_FINISHED));
                mPanelUi.setInstallStatus(result);
                mPanelUi.onInstallFinished();
                mChooserUi.clearWorkingFile();

                if (result) {
                    new MaterialDialog.Builder(MainActivity.this)
                            .title(R.string.title_install_finished)
                            .content(R.string.msg_install_finished)
                            .positiveText(R.string.btn_reboot)
                            .negativeText(R.string.btn_cancel)
                            .callback(new RebootDialogCallback())
                            .build()
                            .show();
                } else {
                    new MaterialDialog.Builder(MainActivity.this)
                            .title(R.string.title_install_failed)
                            .content(R.string.msg_install_failed)
                            .positiveText(R.string.btn_ok)
                            .build()
                            .show();
                }
            }
        });
    }

    public void onEventMainThread(PanelDisplayEvent event) {
        mPanelUi.display(event.position);
    }

    public void onEventMainThread(DownloadEvent event) {
        if (event.status == 0) {
            postEvent(new StatusEvent(STATUS_DOWNLOADING));
            mStatusUi.setupCancelBtn(true, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CloudHelper.cancelDownloads();
                    postEvent(new StatusEvent(STATUS_DOWNLOAD_CANCELED));
                    v.setVisibility(View.GONE);
                    mPanelUi.onInstallFinished();
                }
            });
        } else if (event.status == 2) {
            mStatusUi.setStatusText(getString(R.string.title_downloading), getString(R.string
                    .title_downloaded, event.progress + "%"));
        } else if (event.status == 1) {
            postEvent(new InstallEvent(event.filename));
        } else if (event.status < 0) {
            mStatusUi.setStatusText(getString(R.string.msg_download_failed));
            postEvent(new StatusEvent(STATUS_DOWNLOADING_FAILED));
            mPanelUi.onInstallFinished();
        }
    }

    public void onEventMainThread(StatusEvent event) {
        mStatusUi.setStatus(event.status);
    }

    private boolean checkInstallStatus() {
        return checkInstallStatus(CloudHelper.PACKAGE_TYPE_MINIMAL);
    }

    private boolean checkInstallStatus(int type) {
        int status = CommandUtils.checkPackageInstalled(type);
        postEvent(new StatusEvent(status)); // primary check item will show status

        boolean isInstalled = status > 0;
        mPanelUi.setInstallStatus(type - 1, isInstalled);

        // check another item
        int nextPosition = type == 0 ? 1 : 0;
        mPanelUi.setInstallStatus(nextPosition, CommandUtils.checkPackageInstalled
                (nextPosition + 1) > 0);
        return isInstalled;
    }

    @Override
    public void onBackPressed() {
        if (mPanelUi.isPanelExpanded()) {
            mPanelUi.collapsePanel();
        } else {
            if (mStatusUi.getStatus() > 10) {
                moveTaskToBack(false);
            } else {
                super.onBackPressed();
            }
        }
    }

    public static class PanelDisplayEvent {
        public int position;
    }

    public static class CheckUpdateEvent {
    }

    public static class InstallEvent {
        public String filename;
        public boolean isLocal;

        public InstallEvent(String filename) {
            this.filename = filename;
        }
    }

    public static class StatusEvent {
        public int status;

        public StatusEvent(int status) {
            this.status = status;
        }
    }

    private class RebootDialogCallback extends MaterialDialog.ButtonCallback {
        @Override
        public void onPositive(MaterialDialog dialog) {
            CommandUtils.execCommand("reboot", true, false);
        }

        @Override
        public void onNegative(MaterialDialog dialog) {
            dialog.dismiss();
        }
    }

    public class CheckDataEvent {
    }

}
