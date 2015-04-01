package org.coolapk.gmsinstaller.cloud;

import android.os.Build;

import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.coolapk.gmsinstaller.model.Gapps;
import org.coolapk.gmsinstaller.model.Gpack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by BobPeng on 2015/3/19.
 */
public class CloudHelper {
    private static final String CLOUD_DOMAIN = "http://image.coolapk.com/gapps/";
    public static final int PACKAGE_TYPE_MINIMAL = 1;
    public static final int PACKAGE_TYPE_EXTENSION = 2;

    public static List<Gpack> getProperPackages(List<Gpack> packs) {
        int sdkLevel = Build.VERSION.SDK_INT;
        if (sdkLevel == 15 || sdkLevel == 22) {
            sdkLevel--;
        }

        List<Gpack> newPacks = new ArrayList<>(2);
        for (Gpack pack : packs) {
            if (pack.sdkLevel == sdkLevel) {
                newPacks.add(pack.packageType - 1, pack);
            }
        }

        if (newPacks.size() == 2) {
            return newPacks;
        } else {
            // data error
            return null;
        }
    }

    public static List<Gpack> getGpackList() {
        Request request = new Request.Builder().url(CLOUD_DOMAIN + "gapps-list").build();
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
}
