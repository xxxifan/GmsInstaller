package org.coolapk.gmsinstaller.app;

import android.app.Application;

import com.avos.avoscloud.AVAnalytics;
import com.avos.avoscloud.AVOSCloud;

import org.coolapk.gmsinstaller.cloud.DownloadReceiver;

import de.greenrobot.event.EventBus;

/**
 * Created by BobPeng on 2015/3/18.
 */
public class AppApplication extends Application {
    public static AppApplication sApplication;
    private DownloadReceiver mDownloadReceiver;

    public static AppApplication getInstance() {
        return sApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = this;

        // init LeanCloud service
        AVOSCloud.initialize(this, AppHelper.LC_APP_ID, AppHelper.LC_APP_KEY);
        AVAnalytics.enableCrashReport(this, true);

        // init EventBus
        EventBus.builder().logNoSubscriberMessages(false).sendNoSubscriberEvent(false).installDefaultEventBus();
    }

    public void registerDownloadReceiver() {
        mDownloadReceiver = new DownloadReceiver();
        mDownloadReceiver.register(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        sApplication = null;
        if (mDownloadReceiver != null) {
            mDownloadReceiver.unregister(this);
            mDownloadReceiver = null;
        }
    }
}
