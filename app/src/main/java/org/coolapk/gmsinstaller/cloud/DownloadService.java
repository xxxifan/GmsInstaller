package org.coolapk.gmsinstaller.cloud;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import de.greenrobot.event.EventBus;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

/**
 * Created by xifan on 15-4-1.
 */
public class DownloadService extends IntentService {
    public DownloadService() {
        super("DownloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // TODO DOWNLOAD START AND END STATE.
        Log.e("", "onHandleIntent");
        String url = intent.getStringExtra("url");
        String path = intent.getStringExtra("path");
        File targetFile = new File(path);

        Request.Builder requestBuilder = new Request.Builder().url(url);
        if (targetFile.exists()) {
            long downloaded = targetFile.length();
            requestBuilder.addHeader("Ranges", "bytes=" + downloaded + "-");
        }
        Request request = requestBuilder.build();

        try {
            Response response = new OkHttpClient().newCall(request).execute();
            EventBus eventBus = EventBus.getDefault();
            DownloadEvent event = new DownloadEvent();
            if (response.isSuccessful()) {
                Log.e("", response.headers().toString());
                if (true)
                    return;
                // start event
                event.status = 0;
                event.total = Long.parseLong(response.header("Content-Length"));
//                AppHelper.getPrefs(AppHelper.PREFERENCE_DOWNLOAD_FILES).edit().putLong(targetFile
//                        .getName(), event.totalSize).apply();

                eventBus.post(event);

                InputStream stream = response.body().byteStream();
                BufferedSource source = Okio.buffer(Okio.source(stream));
                BufferedSink sink = Okio.buffer(Okio.sink(new File(path)));
                long readBytes, downloaded = 0l;
                int bufferSize = 8 * 1024;
                while ((readBytes = source.read(sink.buffer(), bufferSize)) > 0) {
                    downloaded += readBytes;
                    if (downloaded > 0) {
                        event.status = 2;
                        event.progress = (int) (downloaded / event.total);
                        event.downloaded = downloaded;
                        eventBus.post(event);
                    }
                }

                // end event
                event.status = 1;
                eventBus.post(event);

                sink.close();
                source.close();
            } else {
                event.status = response.code();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
