package org.coolapk.gmsinstaller.app;

import android.app.Application;

import com.pgyersdk.crash.PgyCrashManager;

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

        PgyCrashManager.register(this, AppHelper.PGY_APP_ID);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        sApplication = null;
    }
}
