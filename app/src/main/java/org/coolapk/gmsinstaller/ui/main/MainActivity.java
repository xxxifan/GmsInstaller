package org.coolapk.gmsinstaller.ui.main;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.coolapk.gmsinstaller.R;
import org.coolapk.gmsinstaller.app.AppHelper;
import org.coolapk.gmsinstaller.cloud.CloudHelper;
import org.coolapk.gmsinstaller.cloud.DownloadEvent;
import org.coolapk.gmsinstaller.cloud.DownloadService;
import org.coolapk.gmsinstaller.model.AppInfo;
import org.coolapk.gmsinstaller.model.Gpack;
import org.coolapk.gmsinstaller.ui.about.AboutActivity;
import org.coolapk.gmsinstaller.ui.feedback.FeedBackDismissListener;
import org.coolapk.gmsinstaller.ui.feedback.FeedbackDialogCallback;
import org.coolapk.gmsinstaller.ui.feedback.FeedbackDisplayListener;
import org.coolapk.gmsinstaller.ui.main.presenter.ChooserPresenter;
import org.coolapk.gmsinstaller.ui.main.presenter.PanelPresenter;
import org.coolapk.gmsinstaller.ui.main.presenter.StatusPresenter;
import org.coolapk.gmsinstaller.util.CommandUtils;
import org.coolapk.gmsinstaller.util.ViewUtils;
import org.coolapk.gmsinstaller.util.ZipUtils;
import org.coolapk.gmsinstaller.widget.ScrollView;

import java.util.List;

import de.greenrobot.event.EventBus;


import static org.coolapk.gmsinstaller.ui.main.presenter.StatusPresenter.STATUS_CHECKING_ROOT;
import static org.coolapk.gmsinstaller.ui.main.presenter.StatusPresenter.STATUS_DOWNLOADING;
import static org.coolapk.gmsinstaller.ui.main.presenter.StatusPresenter.STATUS_DOWNLOADING_FAILED;
import static org.coolapk.gmsinstaller.ui.main.presenter.StatusPresenter.STATUS_DOWNLOAD_CANCELED;
import static org.coolapk.gmsinstaller.ui.main.presenter.StatusPresenter.STATUS_INIT;
import static org.coolapk.gmsinstaller.ui.main.presenter.StatusPresenter.STATUS_INSTALLING;
import static org.coolapk.gmsinstaller.ui.main.presenter.StatusPresenter.STATUS_INSTALL_FINISHED;
import static org.coolapk.gmsinstaller.ui.main.presenter.StatusPresenter.STATUS_NO_ROOT;

public class MainActivity extends ActionBarActivity {

    private StatusPresenter mStatusUi;
    private PanelPresenter mPanelUi;
    private ChooserPresenter mChooserUi;
    private ScrollView mScrollView;

    private boolean mIsDlServiceRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        initView();
        setTitle(R.string.app_mark);
        mIsDlServiceRunning = AppHelper.isServiceRunning(DownloadService.class.getName());
    }

    private void initView() {
        // keep it before toolbar setup
        mScrollView = (ScrollView) findViewById(R.id.main_scroller);
        setupToolbar();
        View root = getWindow().getDecorView();
        mStatusUi = new StatusPresenter(root);
        mPanelUi = new PanelPresenter(root);
        mChooserUi = new ChooserPresenter(root);
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mScrollView.smoothScrollTo(0, ViewUtils.dp2px(56));
            }
        });
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
    protected void onResume() {
        super.onResume();
        if (!mIsDlServiceRunning && mStatusUi.getStatus() == STATUS_INIT) {
            postEvent(new CheckDataEvent());
        } else if (mIsDlServiceRunning) {
            postEvent(new DownloadService.ProgressUpdateEvent());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mChooserUi.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // correct bottom height to scroll more smoother
        correctBottomHeight();

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void correctBottomHeight() {
        int count = mScrollView.getChildCount();
        int childHeight = 0;
        for (int i = 0; i < count; i++) {
            childHeight += mScrollView.getChildAt(i).getMeasuredHeight();
        }

        int toolbarHeight = ViewUtils.dp2px(56);
        int diff = childHeight - mScrollView.getMeasuredHeight();
        if (diff > 0 && diff < toolbarHeight) {
            diff = toolbarHeight - diff;
            // fix extra status bar height
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                diff -= ViewUtils.dp2px(24);
            }

            findViewById(R.id.sliding_main).setPadding(0, 0, 0, diff);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_about:
                startActivity(new Intent(this, AboutActivity.class));
                break;
            case R.id.action_feedback:
                showFeedbackDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showFeedbackDialog() {
        new MaterialDialog.Builder(this)
                .title(R.string.action_feedback)
                .customView(R.layout.view_feedback, true)
                .positiveText(R.string.avoscloud_feedback_send_text)
                .negativeText(R.string.btn_close)
                .dismissListener(new FeedBackDismissListener())
                .showListener(new FeedbackDisplayListener())
                .callback(new FeedbackDialogCallback())
                .build()
                .show();
    }

    private void showUpdateDialog(final AppInfo info) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ViewUtils.showUpdateDialog(MainActivity.this, info);
            }
        });
    }

    public void postEvent(Object event) {
        EventBus.getDefault().post(event);
    }

    /**
     * Events
     */

    public void onEventAsync(CheckUpdateEvent event) {
        // check package info
        List<Gpack> gpacks = CloudHelper.getGpackList();
        mPanelUi.setGappsDetail(gpacks);

        // check app update
        AppInfo info = CloudHelper.checkAppUpdate();
        if (info != null && Long.parseLong(info.version) > AppHelper.getAppVersionCode()) {
            showUpdateDialog(info);
        }

        // tip error
        if (gpacks == null || info == null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, R.string.msg_check_update_failed, Toast
                            .LENGTH_SHORT).show();
                }
            });
        }
    }

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

    public void onEventBackgroundThread(InstallEvent event) {
        if (mStatusUi.getStatus() == STATUS_INSTALLING) {
            return;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mStatusUi.setupCancelBtn(false, null);
                if (mScrollView.getScrollY() > 0) {
                    mScrollView.smoothScrollTo(0, ViewUtils.dp2px(56));
                }
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
        int status = CommandUtils.checkPackageInstall(type);
        postEvent(new StatusEvent(status)); // primary check item will show status

        boolean isInstalled = status > 0;
        mPanelUi.setInstallStatus(CloudHelper.getTypePosition(type), isInstalled);

        // check another item
        int nextPosition = CloudHelper.getTypePosition(type) == 0 ? 1 : 0;
        mPanelUi.setInstallStatus(nextPosition, CommandUtils.checkPackageInstall(CloudHelper
                .getPositionType(nextPosition)) > 0);
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

    public static class CheckDataEvent {
    }

    private class RebootDialogCallback extends MaterialDialog.ButtonCallback {
        @Override
        public void onPositive(MaterialDialog dialog) {
            CommandUtils.execCommand("reboot", true, false);
        }
    }

}
