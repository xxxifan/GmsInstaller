package org.coolapk.gmsinstaller.cloud;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.coolapk.gmsinstaller.app.AppHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import de.greenrobot.event.EventBus;

/**
 * Created by xifan on 15-4-1.
 */
public class DownloadService extends IntentService {
    private boolean mShutDown = false;

    public DownloadService() {
        super("DownloadService");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        EventBus.getDefault().register(this);
        return super.onStartCommand(intent, flags, startId);
    }

    public void onEvent(StopEvent event) {
        mShutDown = true;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.e("", "onIntent " + intent.toString());
        String url = intent.getStringExtra("url");
        String path = intent.getStringExtra("path");
        File targetFile = new File(path);

        // build download header
        Request.Builder requestBuilder = new Request.Builder().url(url);
        if (targetFile.exists()) {
            long downloaded = targetFile.length();
            requestBuilder.addHeader("Ranges", "bytes=" + downloaded + "-");
        }
        Request request = requestBuilder.build();

        // prepare eventBus
        EventBus eventBus = EventBus.getDefault();
        DownloadEvent event = new DownloadEvent();

        try {
            Response response = new OkHttpClient().newCall(request).execute();
            if (response.isSuccessful()) {
                // start event
                event.status = 0;
                event.total = Long.parseLong(response.header("Content-Length"));
                AppHelper.getPrefs(AppHelper.PREFERENCE_DOWNLOAD_FILES).edit().putLong(targetFile
                        .getName(), event.total).apply();

                eventBus.post(event);

                InputStream stream = response.body().byteStream();
                FileOutputStream fileOutputStream = new FileOutputStream(new File(path));
                byte[] buffer = new byte[8 * 1024];
                int count;
                long downloaded = 0;
                while ((count = stream.read(buffer)) > 0) {
                    if (mShutDown) {
                        mShutDown = false;
                        fileOutputStream.flush();
                        fileOutputStream.close();
                        stream.close();
                        return;
                    }
                    fileOutputStream.write(buffer, 0, count);
                    downloaded += count;
                    if (downloaded > 0) {
                        event.status = 2;
                        event.progress = (int) ((float) downloaded / event.total * 100);
                        event.downloaded = downloaded;
                        if (event.progress > event.lastProgress + 1) { // every 2 steps to update UI
                            eventBus.post(event);
                            event.lastProgress = event.progress;
                        }
                    }

                }
                fileOutputStream.flush();
                fileOutputStream.close();
                stream.close();
//                BufferedSource source = Okio.buffer(Okio.source(stream));
//                BufferedSink sink = Okio.buffer(Okio.sink(new File(path)));
//                long readBytes, downloaded = 0l;
//                int bufferSize = 8 * 1024;
//                while ((readBytes = source.read(sink.buffer(), bufferSize)) > 0) {
//                    if (mShutDown) {
//                        mShutDown = false;
//                        sink.flush();
//                        sink.close();
//                        source.close();
//                        return;
//                    }
//
//                    downloaded += readBytes;
//                    if (downloaded > 0) {
//                        event.status = 2;
//                        event.progress = (int) ((float) downloaded / event.total * 100);
//                        event.downloaded = downloaded;
//                        if (event.progress > event.lastProgress + 1) { // every 2 steps to update UI
//                            eventBus.post(event);
//                            event.lastProgress = event.progress;
//                        }
//                    }
//                }
//                sink.flush();
//                sink.close();
//                source.close();

                // end event
                event.status = 1;
                eventBus.post(event);
            } else {
                event.status = -response.code();
                eventBus.post(event);
            }
        } catch (IOException e) {
            event.status = -1;
            eventBus.post(event);
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public static class StopEvent {

    }
}
