package org.coolapk.gmsinstaller.util;

import android.util.Log;

import org.coolapk.gmsinstaller.app.AppHelper;
import org.coolapk.gmsinstaller.model.Gpack;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;

import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

/**
 * Created by BobPeng on 2015/3/23.
 */
public class ZipUtils {
    private static final String TAG = "ZipUtils";

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

    public static String getFileMd5(File file) {
        if (file == null || !file.exists()) {
            return "";
        }

        int bufferSize = 4 * 1024;
        FileInputStream fileInputStream = null;
        DigestInputStream digestInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            digestInputStream = new DigestInputStream(fileInputStream, messageDigest);

            byte[] buffer = new byte[bufferSize];
            while (digestInputStream.read(buffer) > 0) ;

            messageDigest = digestInputStream.getMessageDigest();
            byte[] digestResult = messageDigest.digest();

            // 把字节数组转换成字符串
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < digestResult.length; i++) {
                sb.append(Character.forDigit((digestResult[i] & 240) >> 4, 16));
                sb.append(Character.forDigit(digestResult[i] & 15, 16));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return "";
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "";
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        } finally {
            try {
                if (digestInputStream != null)
                    digestInputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if (fileInputStream != null)
                    fileInputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean unzipFile(File zipFile, File targetPath) {
        String path = targetPath.getPath();
        CommandUtils.execCommand(new String[]{
                "busybox rm -rf " + path + "/*",
                "busybox unzip -o " + zipFile.getPath() + " -d " + path
        }, true, false);
        String[] list = targetPath.list();
        return list != null && list.length > 0;
    }

    public static boolean install(Gpack gpack, boolean isLocal) {
        if (gpack == null) {
            return false;
        }

        File storagePath = AppHelper.getAppExternalPath();
        File gappFile = new File(storagePath, gpack.packageName);
        File tmpPath = new File(storagePath, "tmp");
        if (!tmpPath.exists()) {
            tmpPath.mkdirs();
        }

        if (isLocal || (gappFile.exists() && ZipUtils.getFileMd5(gappFile).equals(gpack.md5))) {
            // unzip gapps to storagePath.
            boolean extracted = unzipFile(isLocal ? new File(gpack.localPath) : gappFile, tmpPath);
            if (!extracted) {
                Log.e(TAG, "extract zip failed");
                return false;
            }
            // convert flash script
            EdifyParser.parseScript(tmpPath);

            File flash = new File(tmpPath, "flash.sh");
            if (flash.exists()) {
                CommandUtils.CommandResult result = CommandUtils.execCommand(new String[]{
                        CommandUtils.CMD_RW_SYSTEM,
                        "busybox mount -o remount,rw /",
                        "sh " + flash.getPath(),
                        "sh " + AppHelper.getContext().getFilesDir().getPath() + "/fix_permission",
                        "busybox mount -o remount,ro /",
                        CommandUtils.CMD_RO_SYSTEM
                }, true, true);
                Log.e(TAG, result.successMsg + " \nerror: " + result.errorMsg);
                return true;
            } else {
                Log.e(TAG, "flash file doesn't exists");
                return false;
            }
        } else {
            Log.e(TAG, "file md5sum is not match or gapps zip not exist");
            return false;
        }
    }

    public static void writeFile(InputStream inputStream, File targetFile) throws IOException {
        BufferedSource buffer = Okio.buffer(Okio.source(inputStream)); // read source into buffer
        BufferedSink sink = Okio.buffer(Okio.sink(targetFile)); // get output sink
        buffer.readAll(sink); // read buffer into sink
        sink.emit(); // flush
        buffer.close();
        sink.close();
    }
}
