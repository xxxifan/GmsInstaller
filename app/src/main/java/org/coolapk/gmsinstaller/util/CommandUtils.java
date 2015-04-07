package org.coolapk.gmsinstaller.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import org.coolapk.gmsinstaller.app.AppHelper;
import org.coolapk.gmsinstaller.cloud.CloudHelper;
import org.coolapk.gmsinstaller.ui.StatusPresenter;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by BobPeng on 2015/3/17.
 */
public class CommandUtils {

    public static final String MIN_PKGS = "com.google.android.gms,com.google.android.gsf,com.google.android.gsf.login,com.android.vending";
    public static final String FALLBACK_MIN_PKGS = "com.google.android.gsf,com.google.android.gsf.login,com.android.vending";
    public static final String EXT_PKGS = "com.android.facelock,com.google.android.googlequicksearchbox";
    public static final String SYSTEM_APP = "/system/app/";
    public static final String SYSTEM_PRIV_APP = "/system/priv-app/";

    public static final String CMD_RW_SYSTEM = "mount -o remount,rw /system";
    public static final String CMD_RO_SYSTEM = "mount -o remount,ro /system";
    private static final String COMMAND_SU = "su";
    private static final String COMMAND_SH = "sh";
    private static final String COMMAND_EXIT = "exit\n";
    private static final String COMMAND_LINE_END = "\n";

    public static boolean checkRootPermission() {
        return execCommand("echo root", true, false).result == 0;
    }

    public static void chmod(String mode, String path) {
        execCommand("chmod " + mode + " " + path, true, false);
    }

    public static CommandResult execCommand(String command, boolean isRoot, boolean isNeedResultMsg) {
        return execCommand(new String[]{
                command
        }, isRoot, isNeedResultMsg);
    }

    public static CommandResult execCommand(String[] commands, boolean isRoot,
                                            boolean isNeedResultMsg) {
        int result = -1;
        if (commands == null || commands.length == 0) {
            return new CommandResult(result, null, null);
        }

        Process process = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = null;
        StringBuilder errorMsg = null;

        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec(isRoot ? COMMAND_SU : COMMAND_SH);
            os = new DataOutputStream(process.getOutputStream());
            for (String command : commands) {
                if (TextUtils.isEmpty(command)) {
                    continue;
                }

                // donnot use os.writeBytes(commmand), avoid chinese charset
                // error
                os.write(command.getBytes());
                os.writeBytes(COMMAND_LINE_END);
                os.flush();
            }
            os.writeBytes(COMMAND_EXIT);
            os.flush();

            result = process.waitFor();
            // get command result
            if (isNeedResultMsg) {
                successMsg = new StringBuilder();
                errorMsg = new StringBuilder();
                successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
                errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String s;
                while ((s = successResult.readLine()) != null) {
                    successMsg.append(s);
                }
                while ((s = errorResult.readLine()) != null) {
                    errorMsg.append(s);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (successResult != null) {
                    successResult.close();
                }
                if (errorResult != null) {
                    errorResult.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (process != null) {
                process.destroy();
            }
        }
        return new CommandResult(result, successMsg == null ? null : successMsg.toString(),
                errorMsg == null ? null : errorMsg.toString());
    }

    public static int checkPackageInstalled(int type) {
        boolean isMinimal = type == CloudHelper.PACKAGE_TYPE_MINIMAL;
        int installed = isMinimal ? StatusPresenter.STATUS_MINIMAL_INSTALLED : StatusPresenter
                .STATUS_EXTENSION_INSTALLED;
        int notInstall = isMinimal ? StatusPresenter.STATUS_MINIMAL_NOT_INSTALLED : StatusPresenter
                .STATUS_EXTENSION_NOT_INSTALLED;

        PackageManager packageManager = AppHelper.getAppContext().getPackageManager();
        if (packageManager == null) {
            return notInstall;
        }

        String[] packages;
        boolean noGms = Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1;
        packages = isMinimal ? (noGms ? FALLBACK_MIN_PKGS.split(",") : MIN_PKGS.split(","))
                : EXT_PKGS.split(",");
        int count = 0;
        for (String pkgName : packages) {
            try {
                packageManager.getApplicationInfo(pkgName, 0);
                count++;
                Log.e("", "get " + pkgName);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }


        if (count > 0 && count < packages.length) {
            return isMinimal ? StatusPresenter.STATUS_MINIMAL_INSTALL_INCOMPLETE : installed;
        } else if (count == packages.length) {
            return installed;
        } else {
            return notInstall;
        }
    }

    public static boolean isFormerSdk() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP;
    }

    public static void initEnvironment() {
        Context context = AppHelper.getAppContext();
        File zipBin = new File("/system/xbin/zip");
        File busyBoxBin = new File("/system/xbin/busybox");
        File fixBin = new File(context.getFilesDir(), "fix_permission");

        if (!zipBin.exists()) {
            try {
                File tmp = new File(context.getFilesDir(), "zip");
                extractAssetTo(context.getAssets().open("binary/zip"), tmp, zipBin);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!busyBoxBin.exists()) {
            try {
                File tmp = new File(context.getFilesDir(), "busybox");
                extractAssetTo(context.getAssets().open("binary/busybox"), tmp, busyBoxBin);
            } catch (IOException e) {
                e.printStackTrace();
            }
            CommandUtils.execCommand(new String[]{
                    CMD_RW_SYSTEM,
                    "/system/xbin/busybox --install -s " + "/system/xbin",
                    CMD_RO_SYSTEM
            }, true, false);
        }

        if (!fixBin.exists()) {
            try {
                extractAssetTo(context.getAssets().open("binary/fix_permission"), fixBin);
                CommandUtils.execCommand(new String[]{
                        "chmod 0755 " + fixBin.getPath(),
                }, true, false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void extractAssetTo(InputStream inputStream, File target) throws
            IOException {
        ZipUtils.writeFile(inputStream, target);
    }

    public static void extractAssetTo(InputStream inputStream, File tmpFile, File target) throws
            IOException {
        ZipUtils.writeFile(inputStream, tmpFile);

        CommandUtils.execCommand(new String[]{
                CommandUtils.CMD_RW_SYSTEM,
                "cat " + tmpFile.getPath() + " > " + target.getPath(),
                "chmod 0755 " + target.getPath(),
                CMD_RO_SYSTEM
        }, true, false);
    }

    /**
     * result of command
     * <ul>
     * <li>{@link CommandResult#result} means result of command, 0 means normal,
     * else means error, same to excute in linux shell</li>
     * <li>{@link CommandResult#successMsg} means success message of command
     * result</li>
     * <li>{@link CommandResult#errorMsg} means error message of command result</li>
     * </ul>
     *
     * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a>
     *         2013-5-16
     */
    public static class CommandResult {

        /**
         * result of command *
         */
        public int result;
        /**
         * success message of command result *
         */
        public String successMsg;
        /**
         * error message of command result *
         */
        public String errorMsg;

        public CommandResult(int result) {
            this.result = result;
        }

        public CommandResult(int result, String successMsg, String errorMsg) {
            this.result = result;
            this.successMsg = successMsg;
            this.errorMsg = errorMsg;
        }
    }
}
