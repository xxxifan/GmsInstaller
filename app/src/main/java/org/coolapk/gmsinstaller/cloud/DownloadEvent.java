package org.coolapk.gmsinstaller.cloud;

/**
 * Created by xifan on 15-4-1.
 */
public class DownloadEvent {
    public int status;
    public int progress;
    public long total;
    public long downloaded;
    public int lastProgress;
    public String filename;
}
