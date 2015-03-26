package org.coolapk.gmsinstaller;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;

import org.coolapk.gmsinstaller.app.AppPresenter;
import org.coolapk.gmsinstaller.model.Gapp;
import org.coolapk.gmsinstaller.util.CommandUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends BaseActivity {

    private MaterialDialog mDialog;

    private RecyclerView mRecyclerView;
    private AppPresenter mPresenter;
    private String[] mGapps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        initView();
    }

    @Override
    protected void setupToolbar(Toolbar toolbar) {
        super.setupToolbar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        // hide divider
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.setElevation(0);
        } else {
            findViewById(R.id.toolbar_shadow).setVisibility(View.GONE);
        }
    }

    private void initView() {
        mPresenter = new AppPresenter(getWindow().getDecorView());
        mPresenter.setOnInstallClickListener(new InstallClickListener());
        mRecyclerView = (RecyclerView) findViewById(R.id.main_list);
        mRecyclerView.setLayoutManager(new CardLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(new CardAdapter(this));
        mRecyclerView.addItemDecoration(new CardItemDecoration(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPresenter.getStatus() == AppPresenter.STATUS_INIT) {
            checkData();
        }
    }

    private void checkInstallStatus() {
        int installStatus = CommandUtils.checkMinPkgInstall();
        if (installStatus < 0) {
            //TODO 未安装
            mPresenter.setStatus(AppPresenter.STATUS_NOT_INSTALLED);
        } else if (installStatus < 3) {
            // TODO 安装不完整
            mPresenter.setStatus(AppPresenter.STATUS_INSTALL_INCOMPLETE);
        } else {
            // TODO 已安装最小包，继续检测其他包
            mPresenter.setStatus(AppPresenter.STATUS_INSTALLED);
        }
    }

    private void checkData() {
        mDialog = new MaterialDialog.Builder(this)
                .cancelable(false).content(R.string.msg_loading).progress(true, 0)
                .build();
        mDialog.show();
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                CommandUtils.initEnvironment();
                boolean isRoot = CommandUtils.checkRootPermission();
                return isRoot;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
                if (result) {
                    checkInstallStatus();
                } else {
                    mPresenter.setStatus(AppPresenter.STATUS_NO_ROOT);
                }
                mDialog.dismiss();
            }
        }.execute();
    }

    private void unpackGapps() {
        File dataPath = getFilesDir();
        File flagFile = new File(dataPath, ".extract");
        if (!dataPath.exists()) {
            dataPath.mkdirs();
        }

        // cache gapp list
        try {
            mGapps = getAssets().list(Gapp.MIN_FOLDER);
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

                    InputStream is = getAssets().open(Gapp.MIN_FOLDER + File.separator + gapp);
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
            if (mPresenter.getStatus() == AppPresenter.STATUS_INSTALLED) {
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

}
