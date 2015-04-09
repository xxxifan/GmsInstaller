package org.coolapk.gmsinstaller.app;

import android.app.Application;

import com.avos.avoscloud.AVAnalytics;
import com.avos.avoscloud.AVOSCloud;

/**
 * Created by BobPeng on 2015/3/18.
 */
public class AppApplication extends Application {
    public static AppApplication sApplication;

    public static AppApplication getInstance() {
        return sApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = this;

        AVOSCloud.initialize(this, AppHelper.LC_APP_ID, AppHelper.LC_APP_KEY);
        AVAnalytics.enableCrashReport(this, true);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        sApplication = null;
    }
}
