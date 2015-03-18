package org.coolapk.gmsinstaller;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.coolapk.gmsinstaller.app.AppPresenter;
import org.coolapk.gmsinstaller.util.Utils;


public class MainActivity extends BaseActivity {

    private static final int STATUS_INIT = -1;
    private static final int STATUS_NO_ROOT = -2;
    private static final int STATUS_NOT_INSTALLED = 0;
    private static final int STATUS_INSTALLED = 1;
    private static final int STATUS_INSTALL_INCOMPLETE = 2;
    private static final int STATUS_UPDATE_AVAILABLE = 3;

    private MaterialDialog mDialog;

    private AppPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        // Don't show up indicator
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        initView();

    }

    private void initView() {
        TextView statusTextView = (TextView) findViewById(R.id.main_install_status);
        TextView commandTextView = (TextView) findViewById(R.id.main_command_status);
        Button installBtn = (Button) findViewById(R.id.main_install_btn);

        installBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onInstallClick();
            }
        });
        mPresenter = new AppPresenter(this, statusTextView, commandTextView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPresenter.getStatus() == STATUS_INIT) {
            checkRoot();
        } else {
            checkInstallStatus();
        }
    }

    private void checkInstallStatus() {
        int installStatus = Utils.checkMinPkgInstall();
        if (installStatus < 0) {
            //TODO 未安装
            mPresenter.setStatus(STATUS_NOT_INSTALLED);
            Toast.makeText(this, "未安装", Toast.LENGTH_SHORT).show();
        } else if (installStatus < 3) {
            // TODO 安装不完整
            mPresenter.setStatus(STATUS_INSTALL_INCOMPLETE);
            Toast.makeText(this, "安装不完整", Toast.LENGTH_SHORT).show();
        } else {
            // TODO 已安装最小包，继续检测其他包
            mPresenter.setStatus(STATUS_INSTALLED);
            Toast.makeText(this, "已安装最小包，继续检测其他包", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkRoot() {
        mDialog = new MaterialDialog.Builder(this)
                .cancelable(false).content(R.string.msg_loading).progress(true, 0)
                .build();
        mDialog.show();

        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                return Utils.checkRootPermission();
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
                if (result) {
                    checkInstallStatus();
                } else {
                    mPresenter.setStatus(STATUS_NO_ROOT);
                    Toast.makeText(MainActivity.this, R.string.msg_no_root, Toast.LENGTH_SHORT).show();
                }
                mDialog.dismiss();
            }
        }.execute();
    }

    private void onInstallClick() {
        // check selection
        // download and install
    }

}
