package org.coolapk.gmsinstaller;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class MainActivity extends ActionBarActivity {

    private StatusPresenter mStatusPresenter;
    private PanelPresenter mPanelPresenter;
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
        if (diff < toolbarHeight) {
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

        // update to checking updates state
        onEventBackgroundThread(new CheckUpdateEvent());

        onStatusEvent(StatusPresenter.STATUS_CHECKING_ROOT);
        boolean hasRoot = CommandUtils.checkRootPermission();
        if (hasRoot) {
            checkInstallStatus();
        } else {
            onStatusEvent(StatusPresenter.STATUS_NO_ROOT);
        }
    }

    public void onEventBackgroundThread(CheckUpdateEvent event) {
        if (!event.noEvent) {
            onStatusEvent(StatusPresenter.STATUS_CHECKING_UPDATES);
        }

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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mStatusPresenter.setupCancelBtn(false, null);
            }
        });
        onStatusEvent(StatusPresenter.STATUS_INSTALLING);
        Gpack gpack = mPanelPresenter.getWorkingGpack();
        ZipUtils.install(gpack);
        checkInstallStatus(gpack.packageType);
        mPanelPresenter.onInstallFinished();
    }

    public void onEventMainThread(PanelDisplayEvent event) {
        mPanelPresenter.display(event.position);
    }

    public void onEventMainThread(DownloadEvent event) {
        if (event.status == 2) {
            if (event.progress == 0) {
                // first run
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
            }

            mStatusPresenter.setStatusText(getString(R.string.title_downloading), getString(R.string
                    .title_downloaded, event.progress + "%"));
        } else if (event.status == 1) {
            postEvent(new InstallEvent());
        } else if (event.status < 0) {
            mStatusPresenter.setStatusText(getString(R.string.msg_download_failed));
            onStatusEvent(StatusPresenter.STATUS_DOWNLOADING_FAILED);
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

    private void unpackGapps() {
        File dataPath = getFilesDir();
        File flagFile = new File(dataPath, ".extract");
        if (!dataPath.exists()) {
            dataPath.mkdirs();
        }

        // cache gapp list
//        try {
//            mGapps = getAssets().list(Gpack.MIN_FOLDER);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        if (!flagFile.exists()) {
            try {
                int count;
                for (String gapp : mGapps) {
                    File targetPath = new File(getFilesDir(), gapp);
                    if (targetPath.exists()) {
                        continue;
                    }

//                    InputStream is = getAssets().open(Gpack.MIN_FOLDER + File.separator + gapp);
                    InputStream is = getAssets().open(File.separator + gapp);
                    byte[] buffer = new byte[is.available()];
                    count = is.read(buffer);

                    FileOutputStream outputStream = new FileOutputStream(targetPath);
                    outputStream.write(buffer, 0, count);
                    outputStream.flush();
                    outputStream.close();
                    is.close();
                    CommandUtils.chmod("0755", targetPath.getPath());
                }

                // create flag file
                flagFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static class PanelDisplayEvent {
        public int position;
    }

    public static class CheckUpdateEvent {
        public boolean noEvent;

        public CheckUpdateEvent(boolean bool) {
            noEvent = bool;
        }

        public CheckUpdateEvent() {
        }
    }

    public static class InstallEvent {
    }

    private class InstallClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (mStatusPresenter.getStatus() == StatusPresenter.STATUS_MINIMAL_INSTALLED) {
                // TODO installed confirm
            }
            boolean isFormerSdk = CommandUtils.isFormerSdk();
            String systemFolder = isFormerSdk ? CommandUtils.SYSTEM_APP : CommandUtils.SYSTEM_PRIV_APP;
            List<String> commands = new ArrayList<>();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                commands.add("setenforce 0");
            }
            commands.add("mount -o remount,rw /system");
            for (String gapp : mGapps) {
                commands.add("cat " + getFilesDir() + File.separator + gapp + " > " + systemFolder +
                        gapp);
                commands.add("chmod 0644 " + systemFolder + gapp);
            }
            commands.add("mount -o remount,ro /system");
            if (!isFormerSdk) {
                commands.add("pm clear com.google.android.gms");
            }

//            Utils.execCommand(command.toArray(new String[command.size()]), true, false);
            for (String cmd : commands) {
                Log.e("", cmd);
            }
        }
    }

    public class CheckDataEvent {
    }

}
