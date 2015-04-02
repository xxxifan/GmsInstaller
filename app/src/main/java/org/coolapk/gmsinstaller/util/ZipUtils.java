package org.coolapk.gmsinstaller.util;

import org.coolapk.gmsinstaller.app.AppHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;

/**
 * Created by BobPeng on 2015/3/23.
 */
public class ZipUtils {
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
            return null;
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
            return null;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
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

    public static void unzipFile(File zipFile, File targetPath) {
        String targetDir = targetPath.getPath() + "/tmp";
        CommandUtils.execCommand(new String[]{"busybox rm -f " + targetDir + "/*", "unzip " + zipFile
                .getPath() + " -d " + targetDir}, true, false);
        //TODO
    }

    public static void flash() {
        EdifyParser.parseScript(AppHelper.getExternalFilePath());
    }
}
