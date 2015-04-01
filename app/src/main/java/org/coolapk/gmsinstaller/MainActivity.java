package org.coolapk.gmsinstaller;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.coolapk.gmsinstaller.cloud.CloudHelper;
import org.coolapk.gmsinstaller.model.Gpack;
import org.coolapk.gmsinstaller.ui.PanelPresenter;
import org.coolapk.gmsinstaller.ui.StatusPresenter;
import org.coolapk.gmsinstaller.util.CommandUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class MainActivity extends BaseActivity {

    private StatusPresenter mStatusPresenter;
    private PanelPresenter mPanelPresenter;
    private String[] mGapps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        initView();
        setTitle(R.string.app_mark);
    }

    @Override
    protected void setupToolbar(Toolbar toolbar) {
        super.setupToolbar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        toolbar.setTitleTextColor(getResources().getColor(android.support.v7.appcompat.R.color
                .primary_text_disabled_material_light));

        // hack toolbar to scroll with slidingUpPanel
        ((ViewGroup) toolbar.getParent()).removeView(toolbar);
        ((ViewGroup) findViewById(R.id.sliding_main)).addView(toolbar, 0);
    }

    private void initView() {
        View root = getWindow().getDecorView();
        mStatusPresenter = new StatusPresenter(root);
        mPanelPresenter = new PanelPresenter(root);
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
        if (mStatusPresenter.getStatus() == StatusPresenter.STATUS_INIT) {
            postEvent(new CheckDataEvent());
        }
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
            onNoRootEvent();
        }
    }

    public void onEventMainThread(PanelDisplayEvent event) {
        mPanelPresenter.display(event.position);
    }

    public void onEventBackgroundThread(CheckUpdateEvent event) {
        onStatusEvent(StatusPresenter.STATUS_CHECKING_UPDATES);
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

    private void checkInstallStatus() {
        int minStatus = CommandUtils.checkPackageInstalled(CloudHelper.PACKAGE_TYPE_MINIMAL);
        onStatusEvent(minStatus); // only display minimal install status
        boolean minInstalled = minStatus != StatusPresenter.STATUS_MINIMAL_NOT_INSTALLED;
        mPanelPresenter.setInstallStatus(0, minInstalled);

        // check extension pack if minimal package installed
        if (minInstalled) {
            mPanelPresenter.setInstallStatus(1, CommandUtils.checkPackageInstalled(CloudHelper
                    .PACKAGE_TYPE_EXTENSION) == StatusPresenter.STATUS_EXTENSION_INSTALLED);
        } else {
            mPanelPresenter.setInstallStatus(1, false);
        }
    }

    private void onNoRootEvent() {
        onStatusEvent(StatusPresenter.STATUS_NO_ROOT);
    }

    private void prepareCheckUpdateState() {
        onStatusEvent(StatusPresenter.STATUS_CHECKING_UPDATES);
    }

    private void prepareCheckRootState() {
        onStatusEvent(StatusPresenter.STATUS_CHECKING_ROOT);
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
            super.onBackPressed();
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
