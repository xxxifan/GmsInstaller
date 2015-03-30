package org.coolapk.gmsinstaller.cloud;

import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.coolapk.gmsinstaller.model.Gpack;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import okio.BufferedSource;

/**
 * Created by BobPeng on 2015/3/19.
 */
public class CloudHelper {

    public static List<Gpack> getGpackList() {
        Request request = new Request.Builder().url("http://ibobpeng.com/sitemap").build();
        try {
            Response response = new OkHttpClient().newCall(request).execute();
            if (response != null) {
                BufferedSource source = response.body().source();
                String xml = source.readString(Charset.forName("utf-8"));
                Log.e("", xml);
                return new ArrayList<>();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
