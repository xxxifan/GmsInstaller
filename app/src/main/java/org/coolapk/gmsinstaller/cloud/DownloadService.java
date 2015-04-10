package org.coolapk.gmsinstaller.cloud;

import android.app.IntentService;
import android.content.Intent;

import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import org.coolapk.gmsinstaller.app.AppHelper;

import java.io.File;
import java.io.IOException;

import de.greenrobot.event.EventBus;
import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * Created by xifan on 15-4-1.
 */
public class DownloadService extends IntentService {
    private boolean mShutDown = false;
    private EventBus mEventBus;
    private DownloadEvent mDownloadEvent;

    public DownloadService() {
        super("DownloadService");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mEventBus = EventBus.getDefault();
        mEventBus.register(this);
        return super.onStartCommand(intent, flags, startId);
    }

    public void onEvent(StopEvent event) {
        mShutDown = true;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String url = intent.getStringExtra("url");
        String path = intent.getStringExtra("path");
        File targetFile = new File(path);

        // prepare event
        mDownloadEvent = new DownloadEvent();
        mDownloadEvent.filename = targetFile.getName();

        // build download header
        Request.Builder requestBuilder = new Request.Builder().url(url);
        if (targetFile.exists()) {
            long downloaded = targetFile.length();
            requestBuilder.addHeader("Ranges", "bytes=" + downloaded + "-");
        }

        Request request = requestBuilder.build();

        // add progress interceptor
        OkHttpClient client = new OkHttpClient();
        client.networkInterceptors().add(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Response originalResponse = chain.proceed(chain.request());
                return originalResponse.newBuilder()
                        .body(new ProgressResponseBody(originalResponse.body()))
                        .build();
            }
        });

        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                // post start
                mDownloadEvent.status = 0;
                mDownloadEvent.total = response.body().contentLength();
                AppHelper.getPrefs(AppHelper.PREFERENCE_DOWNLOAD_FILES).edit().putLong(mDownloadEvent
                        .filename, mDownloadEvent.total).apply();
                mEventBus.post(mDownloadEvent);

                // read data
                BufferedSource source = response.body().source();
                BufferedSink sink = Okio.buffer(Okio.sink(new File(path)));
                int bufferSize = 8 * 1024;
                while (source.read(sink.buffer(), bufferSize) > 0) {
                    sink.emit();
                    if (mShutDown) {
                        mShutDown = false;
                        sink.close();
                        source.close();
                        return;
                    }
                }
                sink.close();
                source.close();

                // end event
                mDownloadEvent.status = 1;
                mEventBus.post(mDownloadEvent);
            } else {
                mDownloadEvent.status = -response.code();
                mEventBus.post(mDownloadEvent);
            }
        } catch (IOException e) {
            mDownloadEvent.status = -1;
            mEventBus.post(mDownloadEvent);
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mEventBus != null) {
            mEventBus.unregister(this);
        }
    }

    private class ProgressResponseBody extends ResponseBody {
        private ResponseBody responseBody;

        public ProgressResponseBody(ResponseBody responseBody) {
            this.responseBody = responseBody;
        }

        @Override
        public MediaType contentType() {
            return responseBody.contentType();
        }

        @Override
        public long contentLength() throws IOException {
            return responseBody.contentLength();
        }


        @Override
        public BufferedSource source() throws IOException {
            return Okio.buffer(dispatchProgress(responseBody.source()));
        }

        private Source dispatchProgress(Source source) {
            return new ForwardingSource(source) {
                private long totalBytes = 0L;

                @Override
                public long read(Buffer sink, long byteCount) throws IOException {
                    long read = super.read(sink, byteCount);
                    totalBytes += read != -1 ? read : 0;

                    // dispatch event
                    mDownloadEvent.progress = 100 * totalBytes / mDownloadEvent.total;
                    mDownloadEvent.status = 2;
                    mDownloadEvent.downloaded = totalBytes;

                    mEventBus.post(mDownloadEvent);

                    return read;
                }
            };
        }
    }

    public static class StopEvent {
    }
}
