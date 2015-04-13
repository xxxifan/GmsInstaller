package org.coolapk.gmsinstaller.cloud;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.coolapk.gmsinstaller.app.AppHelper;
import org.coolapk.gmsinstaller.model.AppInfo;
import org.coolapk.gmsinstaller.model.Gapps;
import org.coolapk.gmsinstaller.model.Gpack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by BobPeng on 2015/3/19.
 */
public class CloudHelper {
    public static final int PACKAGE_TYPE_MINIMAL = 1;
    public static final int PACKAGE_TYPE_EXTENSION = 2;
        private static final String CLOUD_DOMAIN = "http://image.coolapk.com/gapps/";
//    private static final String CLOUD_DOMAIN = "http://192.168.1.100/downloads/";
    private static final String FIR_BASE_URL = "http://fir.im/api/v2/app/version/";
    private static final String FIR_APP_ID = "5528e039fea0cae136001c1a";
    private static final String FIR_TOKEN = "2a7f7140decb11e4bc71d7769ade328c415f806b";

    public static List<Gpack> getGpackList() {
        Request request = new Request.Builder().url(CLOUD_DOMAIN + "gapps-list?" + AppHelper
                .getSimpleTimestamp())
                .build();
        try {
            Response response = new OkHttpClient().newCall(request).execute();
            if (response != null && response.isSuccessful()) {
                return new Gson().fromJson(response.body().charStream(), Gapps.class).data;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static AppInfo checkAppUpdate() {
        Request request = new Request.Builder().url(FIR_BASE_URL + FIR_APP_ID + "?token=" + FIR_TOKEN)
                .build();
        try {
            Response response = new OkHttpClient().newCall(request).execute();
            if (response != null && response.isSuccessful()) {
                return new Gson().fromJson(response.body().charStream(), AppInfo.class);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void downloadPackage(String packageName, Intent data) {
        Context context = AppHelper.getContext();
        Intent intent = new Intent(context, DownloadService.class);
        if (data != null) {
            intent.putExtras(data);
        }
        intent.putExtra("url", CLOUD_DOMAIN + packageName);
        context.startService(intent);
    }

    public static void cancelDownloads() {
        EventBus.getDefault().post(new DownloadService.StopEvent());
    }

    public static long downloadUpdate(Context context, String url, String version) {
        String filename = "GmsInstaller-" + version;
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context
                .DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
        request.setTitle(filename);
        request.setDescription("coolapk.com");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setMimeType("application/vnd.android.package-archive");

        AppHelper.getApplication().registerDownloadReceiver();
        return downloadManager.enqueue(request);
    }

    public static List<Gpack> getProperPackages(List<Gpack> packs) {
        if (packs == null || packs.size() == 0) {
            return null;
        }

        int sdkLevel = Build.VERSION.SDK_INT;
        if (sdkLevel == 15 || sdkLevel == 22) {
            sdkLevel--;
        }

        List<Gpack> newPacks = new ArrayList<>(2);
        for (Gpack pack : packs) {
            if (pack.sdkLevel == sdkLevel) {
                // minus 1 to match list position
                newPacks.add(newPacks.size() == 0 ? 0 : pack.packageType - 1, pack);
            }
        }

        if (newPacks.size() == 2) {
            return newPacks;
        } else {
            // data error
            return null;
        }
    }
}
