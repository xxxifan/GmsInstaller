package org.coolapk.gmsinstaller;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

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

    private RecyclerView mRecyclerView;
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

        mRecyclerView = (RecyclerView) findViewById(R.id.main_list);
        mRecyclerView.addItemDecoration(new CardItemDecoration(this));
        mRecyclerView.setLayoutManager(new CardLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);
        CardAdapter adapter = new CardAdapter(this);
        adapter.setOnItemClickListener(new CardAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                mPanelPresenter.display(position);
            }
        });
        mRecyclerView.setAdapter(adapter);
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
            post(new CheckDataEvent());
        }
    }

    private void checkInstallStatus() {
        int installStatus = CommandUtils.checkMinPkgInstall();
        if (installStatus < 0) {
            onStatusEvent(StatusPresenter.STATUS_NOT_INSTALLED);
        } else if (installStatus < 3) {
            onStatusEvent(StatusPresenter.STATUS_INSTALL_INCOMPLETE);
        } else {
            onStatusEvent(StatusPresenter.STATUS_INSTALLED);
        }
    }

    /**
     * CheckData
     */
    public void onEventBackgroundThread(CheckDataEvent event) {
        CommandUtils.initEnvironment();

        // update to checking updates state
        onStatusEvent(StatusPresenter.STATUS_CHECKING_UPDATES);
        boolean success = CloudHelper.getGpackList() != null;
        if (success) {
            // TODO prepare package detail
        } else {
            mPanelPresenter.setGappsDetail(null);
        }

        onStatusEvent(StatusPresenter.STATUS_CHECKING_ROOT);
        boolean hasRoot = CommandUtils.checkRootPermission();
        if (hasRoot) {
            checkInstallStatus();
        } else {
            onNoRootEvent();
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
        try {
            mGapps = getAssets().list(Gpack.MIN_FOLDER);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!flagFile.exists()) {
            try {
                int count;
                for (String gapp : mGapps) {
                    File targetPath = new File(getFilesDir(), gapp);
                    if (targetPath.exists()) {
                        continue;
                    }

                    InputStream is = getAssets().open(Gpack.MIN_FOLDER + File.separator + gapp);
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

    private class InstallClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (mStatusPresenter.getStatus() == StatusPresenter.STATUS_INSTALLED) {
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
