package org.coolapk.gmsinstaller.util;

import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okio.BufferedSource;
import okio.Okio;

/**
 * Original from https://github.com/Androguide/FlashGordon/blob/master/src/com/androguide/recovery/emulator/helpers/EdifyParser.java
 * Rewritten by xxxifan
 */
public class EdifyParser {
    private static final String SCRIPTER_PATH = "/META-INF/com/google/android/updater-script";

    private static String chained[] = {""};

    public static void noQuote(String line) {
        line = line.replaceAll("\"", "");
    }

    public static void noComma(String line) {
        line = line.replaceAll("[,]", "");
    }

    public static void noEnd(String line) {
        line = line.replaceAll("(\\);)", "");
    }

    public static void parseScript(File targetPath) throws FileNotFoundException {
        File scriptFile = new File(targetPath, SCRIPTER_PATH);
        if (!scriptFile.exists()) {
            throw new FileNotFoundException("Script file doesn't exist!");
        }

        File parseFile = new File(targetPath.getPath(), "flash.sh");
        try {
            // read script into buffer
            BufferedSource source = Okio.buffer(Okio.source(scriptFile));
            String script = source.readUtf8();
            source.close();
            edifyToBash(script, parseFile.getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void edifyToBash(String source, String outputPath) {
        // replace global symbols
        source = source.replaceAll("\\),", ");");
        chained = source.split("\\);");

        List<String> commands = new ArrayList<>();
        for (int i = 0; i < chained.length; i++) {
            commands.add(interpreterAlgorithm(chained[i], outputPath));
        }

        CommandUtils.execCommand(commands.toArray(new String[commands.size()]), false, false);
    }

    private static String interpreterAlgorithm(String curr, String outputPath) {
        // delete all ");"
        curr = curr.replaceAll("\\s+", " ");
        curr = curr.replaceAll("assert\\(", "");

        // symlink() parsing & translation
        if (curr.contains("symlink(")) {

            curr = curr.replaceAll(",", "");
            curr = curr.replaceAll("\"", "");
            curr = curr.replaceAll("\\)", "");
            curr = curr.replaceAll(";", "");
            curr = curr.replaceAll("symlink\\(", "ln -s ");
            Log.v("Recovery Emulator", curr);
//        } else if (curr.contains("getprop")) { // avoiding assert() lines
//        } else if (curr.contains("format(")) { // not support
//        } else if (curr.contains("#")) { // keep comment
//        } else if (curr.contains("show_progress(")) { // Deleting useless show_progress() lines
//        } else if (curr.contains("ui_print(")) {// Deleting useless ui_print() lines

            // package_extract_file() parsing & translation
        } else if (curr.contains("package_extract_file(")) {

            curr = curr.replaceAll("\"", "");
            curr = curr.replaceAll("\\)", "");
            curr = curr.replaceAll(";", "");

            if (curr.contains("boot.img")) {
                curr = curr.replaceAll("package_extract_file\\(", "dd if="
                        + outputPath + "/");
                curr = curr.replaceAll(", ", " of=");

                Log.v("Recovery Emulator", curr);
                curr = "echo \"" + curr + "\" >> " + outputPath;
            } else {
                curr = curr.replaceAll(",", "");

                curr = curr.replaceAll("package_extract_file\\(",
                        "busybox cp -fp " + outputPath + "/");

                Log.v("Recovery Emulator", curr);
                curr = "echo \"" + curr + "\" >> " + outputPath;
            }

            // package_extract_dir() parsing & translation
        } else if (curr.contains("package_extract_dir(")) {
            String original = curr;
            curr = curr.replace("package_extract_dir(\"", "");
            curr = curr.replaceAll("\"", "");
            curr = curr.replaceAll("\\)", "");
            curr = curr.replaceAll(";", "");

            String arr[] = curr.split(", ");

            Log.v("Recovery Emulator", "mkdir -p " + arr[1]);
            curr = "echo \"mkdir -p " + arr[1] + "\" >> " + outputPath + " && ";

            original = original.replace("package_extract_dir(\"", "busybox cp -rfp "
                    + outputPath + "/");
            original = original.replaceAll("\", \"", "/* ");
            original = original.replaceAll(",", "");
            original = original.replaceAll("\"", "");
            original = original.replaceAll("\\)", "");
            original = original.replaceAll(";", "");
            Log.v("Recovery Emulator", original);
            curr += "echo \"" + original + "\" >> " + outputPath;

            // set_perm() parsing & translation
        } else if (curr.contains("set_perm(")) {
            curr = curr.replaceAll("\"", "");
            curr = curr.replaceAll("\\)", "");
            curr = curr.replaceAll(";", "");
            curr = curr.replaceAll("set_perm\\(", "");
            String[] array = curr.split(",\\s");

            Log.v("Recovery Emulator", "chown " + array[0] + ":" + array[1]
                    + " " + array[3]);
            curr = "echo \"" + "chown " + array[0] + ":" + array[1] + " " + array[3] + "\" >> " +
                    outputPath + " && ";
            Log.v("Recovery Emulator", "chmod " + array[2] + " " + array[3]);
            curr += "echo \"" + "chmod " + array[2] + " " + array[3] + "\" >> " + outputPath;

            // set_perm_recursive() parsing & translation
        } else if (curr.contains("set_perm_recursive(")) {
            curr = curr.replaceAll("\\)", "");
            curr = curr.replaceAll(";", "");
            curr = curr.replaceAll("set_perm_recursive\\(", "");
            String[] array = curr.split(",\\s");

            Log.v("Recovery Emulator", "chown -R " + array[0] + ":" + array[1]
                    + " " + array[3]);
            curr = "echo \"" + "chown -R " + array[0] + ":" + array[1]
                    + " " + array[3] + "\" >> " + outputPath + " && ";
            Log.v("Recovery Emulator", "chmod -R " + array[2] + " " + array[3]);
            curr += "echo \"" + "chmod -R " + array[2] + " " + array[3]
                    + "\" >> " + outputPath;

            // delete() parsing & translation
        } else if (curr.contains("delete(\"")) {
            curr = curr.replaceAll(",", "");
            curr = curr.replaceAll("\"", "");
            curr = curr.replaceAll("\\)", "");
            curr = curr.replaceAll(";", "");
            curr = curr.replaceAll("delete\\(", "busybox rm -f ");
            Log.v("Recovery Emulator", curr);
            curr = "echo \"" + curr + "\" >> " + outputPath;

            // delete_recursive()
        } else if (curr.contains("delete_recursive(\"")) {
            curr = curr.replaceAll(",", "");
            curr = curr.replaceAll("\"", "");
            curr = curr.replaceAll("\\)", "");
            curr = curr.replaceAll(";", "");
            curr = curr.replaceAll("delete\\(", "busybox rm -rf ");
            Log.v("Recovery Emulator", curr);
            curr = "echo \"" + curr + "\" >> " + outputPath;
            // run_program() parsing & translation
        } else if (curr.contains("run_program(\"")) {
            if (curr.contains("/sbin/busybox")) {
                curr = curr.replaceAll("\\)", "");
                curr = curr.replaceAll(";", "");
                curr = curr.replaceAll("\"", "");
                curr = curr.replaceAll("run_program\\(/sbin/busybox",
                        "busybox ");
                String arr[] = curr.split(",");
                int i = 0;
                String chain = "";
                while (i < arr.length) {
                    chain = chain + arr[i];
                    i++;
                }
                Log.v("Recovery Emulator", chain);
                // TODO
            } else {
                curr = curr.replaceAll(",", "");
                curr = curr.replaceAll("\"", "");
                curr = curr.replaceAll("\\)", "");
                curr = curr.replaceAll(";", "");
                curr = curr.replaceAll("run_program\\(", "sh ");
                Log.v("Recovery Emulator", curr);
                curr = "echo \"" + curr + "\" >> " + outputPath;
            }

            // mount() / unmount() parsing & translation
        } else if (curr.contains("mount(")) {
            if (curr.contains("/system")) {
                if (curr.contains("unmount(")) {
                    // Log.v("Recovery Emulator",
                    // "busybox mount -o ro,remount -t auto /system");
                    // cmd.su.runWaitFor("echo \""+"busybox mount -o ro,remount -t auto /system");

                } else {

                    Log.v("Recovery Emulator",
                            "busybox mount -o rw,remount -t auto /system");
                    curr = "echo \"" + "busybox " + CommandUtils.CMD_RW_SYSTEM + "\" >> " + outputPath;
                }
            }

            // write_raw_image() parsing & translation
        } else if (curr.contains("write_raw_image(")) {
            curr = curr.replaceAll("write_raw_image\\(", "dd if=");
            String[] arr = curr.split("\", \"");
            arr[0] = arr[0].replaceAll("\"", "");
            arr[1] = arr[1].replaceAll("\"", "");
            Log.v("Recovery Emulator", arr[0] + " of=" + arr[1]);
            curr = "echo \"" + arr[0] + " of=" + arr[1] + "\" >> " + outputPath;

        } else {
            Log.v("Recovery Emulator", curr);
            curr = "";
        }

        return curr;
    }

}
