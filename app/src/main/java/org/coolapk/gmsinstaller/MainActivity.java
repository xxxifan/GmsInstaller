package org.coolapk.gmsinstaller;

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
import org.coolapk.gmsinstaller.ui.PanelPresenter;
import org.coolapk.gmsinstaller.ui.StatusPresenter;
import org.coolapk.gmsinstaller.util.CommandUtils;
import org.coolapk.gmsinstaller.util.ViewUtils;
import org.coolapk.gmsinstaller.util.ZipUtils;
import org.coolapk.gmsinstaller.widget.ScrollView;

import java.util.List;

import de.greenrobot.event.EventBus;

public class MainActivity extends ActionBarActivity {

    private StatusPresenter mStatusPresenter;
    private PanelPresenter mPanelPresenter;
    private MaterialDialog mDialog;

    private String[] mGapps;
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
        mStatusPresenter = new StatusPresenter(root);
        mPanelPresenter = new PanelPresenter(root);
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
    protected void onResume() {
        super.onResume();
        if (!mIsServiceRunning && mStatusPresenter.getStatus() == StatusPresenter.STATUS_INIT) {
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
        CommandUtils.initEnvironment();

        // checking updates state
        postEvent(new CheckUpdateEvent());

        onStatusEvent(StatusPresenter.STATUS_CHECKING_ROOT);
        boolean hasRoot = CommandUtils.checkRootPermission();
        if (hasRoot) {
            checkInstallStatus();
        } else {
            onStatusEvent(StatusPresenter.STATUS_NO_ROOT);
        }
    }

    public void onEventBackgroundThread(CheckUpdateEvent event) {
        List<Gpack> gpacks = CloudHelper.getGpackList();
        mPanelPresenter.setGappsDetail(gpacks);

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
        if (mStatusPresenter.getStatus() == StatusPresenter.STATUS_INSTALLING) {
            return;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mStatusPresenter.setupCancelBtn(false, null);
            }
        });
        onStatusEvent(StatusPresenter.STATUS_INSTALLING);
        ZipUtils.install(mPanelPresenter.getGpack(event.filename));

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPanelPresenter.onInstallFinished();
                onStatusEvent(StatusPresenter.STATUS_INSTALL_FINISHED);
                mDialog = new MaterialDialog.Builder(MainActivity.this)
                        .title(R.string.btn_install_finished)
                        .content(R.string.msg_install_finished)
                        .positiveText(R.string.btn_reboot)
                        .negativeText(R.string.btn_cancel)
                        .callback(new RebootDialogCallback())
                        .build();
                mDialog.show();
            }
        });
    }

    public void onEventMainThread(PanelDisplayEvent event) {
        mPanelPresenter.display(event.position);
    }

    public void onEventMainThread(DownloadEvent event) {
        if (event.status == 0) {
            onStatusEvent(StatusPresenter.STATUS_DOWNLOADING);
            mStatusPresenter.setupCancelBtn(true, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CloudHelper.cancelDownloads();
                    onStatusEvent(StatusPresenter.STATUS_DOWNLOAD_CANCELED);
                    v.setVisibility(View.GONE);
                    mPanelPresenter.onInstallFinished();
                }
            });
        } else if (event.status == 2) {
            mStatusPresenter.setStatusText(getString(R.string.title_downloading), getString(R.string
                    .title_downloaded, event.progress + "%"));
        } else if (event.status == 1) {
            postEvent(new InstallEvent(event.filename));
        } else if (event.status < 0) {
            mStatusPresenter.setStatusText(getString(R.string.msg_download_failed));
            onStatusEvent(StatusPresenter.STATUS_DOWNLOADING_FAILED);
            mPanelPresenter.onInstallFinished();
        }
    }

    private boolean checkInstallStatus() {
        return checkInstallStatus(CloudHelper.PACKAGE_TYPE_MINIMAL);
    }

    private boolean checkInstallStatus(int type) {
        int status = CommandUtils.checkPackageInstalled(type);
        onStatusEvent(status); // primary check item will show status
        boolean isInstalled = status > 0;
        mPanelPresenter.setInstallStatus(type - 1, isInstalled);

        // check another item
        int nextItem = type == CloudHelper.PACKAGE_TYPE_MINIMAL ? CloudHelper
                .PACKAGE_TYPE_EXTENSION : CloudHelper.PACKAGE_TYPE_MINIMAL;
        mPanelPresenter.setInstallStatus(nextItem - 1, CommandUtils.checkPackageInstalled(nextItem) > 0);

        return isInstalled;
    }

    private void onStatusEvent(final int status) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mStatusPresenter.setStatus(status);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mPanelPresenter.isPanelExpanded()) {
            mPanelPresenter.collapsePanel();
        } else {
            if (mStatusPresenter.getStatus() > 10) {
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

        public InstallEvent(String filename) {
            this.filename = filename;
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
