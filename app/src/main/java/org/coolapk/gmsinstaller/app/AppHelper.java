package org.coolapk.gmsinstaller.app;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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
    public static final String LC_APP_ID = "wfxfxudtklqk2hd9trqpt3bv1w7hr3fpg59z77qa5aq83z5k";
    public static final String LC_APP_KEY = "jzze3zj1354wrclpm7d34vlvcwuxvx2rb9wr4wka6q3x7vjr";

    private static File sExternalFile;

    public static int getOsVersion() {
        return Build.VERSION.SDK_INT;
    }

    public static int getAppVersionCode() {
        try {
            PackageManager packageManager = getContext().getPackageManager();
            PackageInfo packInfo = packageManager.getPackageInfo(getContext().getPackageName(), 0);
            return packInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static AppApplication getApplication() {
        return AppApplication.getInstance();
    }

    public static Context getContext() {
        return AppApplication.getInstance();
    }

    public static SharedPreferences getPrefs() {
        return PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    public static SharedPreferences getPrefs(String name) {
        return getContext().getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    public static File getAppExternalPath() {
        if (sExternalFile == null) {
            Context context = getContext();
            File externalFileDir = new File("/sdcard/Android/data/" + context.getPackageName() +
                    "/files");

            if (!externalFileDir.exists()) {
                externalFileDir.mkdirs();
                externalFileDir.setReadable(true, false);
                externalFileDir.setExecutable(true, false);
            }
            sExternalFile = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) ?
                    externalFileDir : context.getFilesDir();
        }
        return sExternalFile;
    }

    public static boolean isServiceRunning(String className) {
        ActivityManager activityManager = (ActivityManager) AppHelper.getContext().getSystemService
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
