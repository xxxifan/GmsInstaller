package org.coolapk.gmsinstaller.cloud;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * Created by xifan on 15-4-11.
 */
public class DownloadReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(DownloadManager.ACTION_NOTIFICATION_CLICKED)) {
            Intent i = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }

    public void register(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED);
        context.registerReceiver(this, filter);
    }

    public void unregister(Context context) {
        context.unregisterReceiver(this);
    }
}
