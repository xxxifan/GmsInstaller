package org.coolapk.gmsinstaller.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

/**
 * Created by BobPeng on 2015/3/18.
 */
public class AppHelper {

    public static int getOsVersion() {
        return Build.VERSION.SDK_INT;
    }

    public static AppApplication getApplication() {
        return AppApplication.getInstance();
    }

    public static Context getAppContext() {
        return AppApplication.getInstance();
    }

    public static SharedPreferences getPrefs() {
        return PreferenceManager.getDefaultSharedPreferences(getAppContext());
    }
}
