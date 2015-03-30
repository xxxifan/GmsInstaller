package org.coolapk.gmsinstaller;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.ViewStub;

import de.greenrobot.event.EventBus;

/**
 * Created by BobPeng on 2015/3/16.
 */
public class BaseActivity extends ActionBarActivity {

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(R.layout.base_activity);
        ViewStub stub = (ViewStub) findViewById(R.id.content);
        stub.setLayoutResource(layoutResID);
        stub.inflate();

        // setup toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setupToolbar(toolbar);
        }
    }

    protected void setupToolbar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void post(Object event) {
        EventBus.getDefault().post(event);
    }
}
