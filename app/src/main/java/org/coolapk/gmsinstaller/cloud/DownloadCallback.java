package org.coolapk.gmsinstaller.cloud;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

/**
 * Created by xifan on 15-4-1.
 */
public abstract class DownloadCallback implements Callback {
    @Override
    public void onFailure(Request request, IOException e) {
        e.printStackTrace();
    }

    @Override
    public void onResponse(Response response) throws IOException {
        if (response.isSuccessful()) {
            onDownloadStart();

        }
    }

    public abstract void onDownloadStart();
    public abstract void onDownloadProgress();
    public abstract void onDownloadEnd();
}
