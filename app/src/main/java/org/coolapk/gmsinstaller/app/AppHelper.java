package org.coolapk.gmsinstaller.app;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by BobPeng on 2015/3/18.
 */
public class AppHelper {

    public static final String PREFERENCE_DOWNLOAD_FILES = "download_files";
    private static final String SDCARD_ZERO_FOLDER = "/storage/emulated/0/";
    private static final String SDCARD_LEGACY_FOLDER = "/storage/emulated/legacy/";

    private static File sExternalFile;

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

    public static SharedPreferences getPrefs(String name) {
        return getAppContext().getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    public static File getExternalFilePath() {
        if (sExternalFile == null) {
            Context context = getAppContext();
            sExternalFile = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) ?
                    context.getExternalFilesDir(null) : context.getFilesDir();
            if (sExternalFile.getPath().contains(SDCARD_ZERO_FOLDER)) {
                sExternalFile = new File(sExternalFile.getPath().replace(SDCARD_ZERO_FOLDER,
                        SDCARD_LEGACY_FOLDER));
            }
        }
        return sExternalFile;
    }

    public static boolean isServiceRunning(String className) {
        ActivityManager activityManager = (ActivityManager) AppHelper.getAppContext().getSystemService
                (Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager.getRunningServices(30);
        if (serviceList == null || serviceList.size() <= 0) {
            return false;
        }
        for (int i = 0; i < serviceList.size(); i++) {
            if (serviceList.get(i).service.getClassName().equals(className)) {
                return true;
            }
        }
        return false;
    }

    public static String getSimpleTimestamp() {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMDD", Locale.getDefault());
        return format.format(Calendar.getInstance().getTime());
    }
}
