package org.coolapk.gmsinstaller.ui.about;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVAnalytics;

import org.coolapk.gmsinstaller.R;
import org.coolapk.gmsinstaller.app.AppHelper;
import org.coolapk.gmsinstaller.cloud.CloudHelper;
import org.coolapk.gmsinstaller.model.AppInfo;
import org.coolapk.gmsinstaller.util.ViewUtils;

public class AboutActivity extends ActionBarActivity implements View.OnClickListener {

    private int mLikeCounter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView versionText = (TextView) findViewById(R.id.about_version);
        versionText.setText(AppHelper.getAppVersionName() + "(" + AppHelper.getAppVersionCode() + ")");
        TextView checkVersionBtn = (TextView) findViewById(R.id.about_check_update);
        checkVersionBtn.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_about, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_like) {
            onLikeClick();
            return true;
        } else if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void onLikeClick() {
        switch (mLikeCounter) {
            case 0:
                Toast.makeText(this, R.string.msg_like1, Toast.LENGTH_SHORT).show();
                mLikeCounter++;
                AVAnalytics.onEvent(this, "like1");
                break;
            case 1:
                Toast.makeText(this, R.string.msg_like2, Toast.LENGTH_SHORT).show();
                mLikeCounter++;
                AVAnalytics.onEvent(this, "like2");
                break;
            case 2:
                Toast.makeText(this, R.string.msg_like3, Toast.LENGTH_SHORT).show();
                mLikeCounter++;
                AVAnalytics.onEvent(this, "like3");
                break;
            case 3:
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.btn_share));
                intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(Intent.createChooser(intent, getTitle()));
                mLikeCounter++;
                AVAnalytics.onEvent(this, "share");
                break;
            case 4:
                Toast.makeText(this, R.string.msg_like4, Toast.LENGTH_SHORT).show();
                mLikeCounter++;
                AVAnalytics.onEvent(this, "like4");
                break;
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.about_check_update) {
            onUpdateClick();
        }
    }

    private void onUpdateClick() {
        new AsyncTask<Void, Void, AppInfo>() {
            @Override
            protected AppInfo doInBackground(Void... params) {
                AppInfo info = CloudHelper.checkAppUpdate();
                if (info != null && Long.parseLong(info.version) > AppHelper.getAppVersionCode()) {
                    return info;
                }
                return null;
            }

            @Override
            protected void onPostExecute(AppInfo info) {
                super.onPostExecute(info);
                if (info != null) {
                    ViewUtils.showUpdateDialog(AboutActivity.this, info);
                } else {
                    Toast.makeText(AboutActivity.this, R.string.msg_no_app_update, Toast
                            .LENGTH_SHORT).show();
                }
            }
        }.execute();
    }
}
