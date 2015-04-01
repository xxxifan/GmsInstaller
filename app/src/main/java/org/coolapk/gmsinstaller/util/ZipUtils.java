package org.coolapk.gmsinstaller.util;

import java.text.DecimalFormat;

/**
 * Created by BobPeng on 2015/3/23.
 */
public class ZipUtils {
    public static final String SCRIPTER_PATH = "META-INF/com/google/android/updater-script";
    private static final String DECIMAL_FORMAT = "##0.0#";
    private static final String[] UNIT = {" B", " KB", " MB", " GB"};

    public static String getFormatSize(long fileSize) {
        if (fileSize < 0)
            fileSize = 0;

        int counter = 0;
        float threshold = 1024f, fsize = fileSize;
        while (fsize > threshold) {
            fsize /= threshold;
            //threshold *= 1024;
            counter++;
        }

        if (fsize >= 1000f) {
            fsize /= threshold;
            counter++;
        }

        return new DecimalFormat(DECIMAL_FORMAT).format(fsize) + UNIT[counter];
    }
}
