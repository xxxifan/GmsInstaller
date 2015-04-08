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

    public static void parseScript(File targetPath) throws FileNotFoundException {
        File scriptFile = new File(targetPath, SCRIPTER_PATH);
        if (!scriptFile.exists()) {
            throw new FileNotFoundException("Script file doesn't exist!");
        }

        try {
            // read script into buffer
            BufferedSource source = Okio.buffer(Okio.source(scriptFile));
            String script = source.readUtf8();
            source.close();
            edifyToBash(script, targetPath.getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void edifyToBash(String source, String outputPath) {
        // replace global symbols
        source = source.replaceAll("\\),", ");");
        String[] chained = source.split("\\);");
        List<String> commands = new ArrayList<>();
        for (int i = 0; i < chained.length; i++) {
            commands.add(interpreterAlgorithm(chained[i], outputPath));
        }

        CommandUtils.execCommand(commands.toArray(new String[commands.size()]), false, false);
    }

    private static String interpreterAlgorithm(String curr, String outputPath) {
        String script = outputPath + "/flash.sh";
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
                curr = curr.replaceAll("package_extract_file\\(", "dd if=" + script + "/");
                curr = curr.replaceAll(", ", " of=");

                Log.v("Recovery Emulator", curr);
                curr = "echo \"" + curr + "\" >> " + script;
            } else {
                curr = curr.replaceAll(",", "");
                curr = curr.replaceAll("package_extract_file\\(", "busybox cp -f " + outputPath +
                        "/").trim();

                Log.v("Recovery Emulator", curr);
                curr = "echo \"" + curr + "\" >> " + script;
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
            curr = "echo \"mkdir -p " + arr[1] + "\" >> " + script + " && ";

            original = original.replace("package_extract_dir(\"", "busybox cp -rf " + outputPath +
                    "/").trim();
            original = original.replaceAll("\", \"", "/* ");
            original = original.replaceAll(",", "");
            original = original.replaceAll("\"", "");
            original = original.replaceAll("\\)", "");
            original = original.replaceAll(";", "");
            Log.v("Recovery Emulator", original);
            curr += "echo \"" + original + "\" >> " + script;

            // set_perm() parsing & translation
        } else if (curr.contains("set_perm(")) {
            curr = curr.replaceAll("\"", "");
            curr = curr.replaceAll("\\)", "");
            curr = curr.replaceAll(";", "");
            curr = curr.replaceAll("set_perm\\(", "");
            String[] array = curr.split(",\\s");

            Log.v("Recovery Emulator", "chown " + array[0] + ":" + array[1] + " " + array[3]);
            curr = "echo \"" + "chown " + array[0].trim() + ":" + array[1] + " " + array[3] + "\" >> " +
                    script + " && ";
            Log.v("Recovery Emulator", "chmod " + array[2] + " " + array[3]);
            curr += "echo \"" + "chmod " + array[2] + " " + array[3] + "\" >> " + script;

            // set_perm_recursive() parsing & translation
        } else if (curr.contains("set_perm_recursive(")) {
            curr = curr.replaceAll("\\)", "");
            curr = curr.replaceAll(";", "");
            curr = curr.replaceAll("set_perm_recursive\\(", "");
            String[] array = curr.split(",\\s");

            Log.v("Recovery Emulator", "busybox chown -R " + array[0] + ":" + array[1] + " " + array[4]);
            curr = "echo \"" + "busybox chown -R " + array[0].trim() + ":" + array[1] + " " + array[4]
                    + "\" >> " + script + " && ";

            Log.v("Recovery Emulator", "find " + array[4] + " -type d -exec chmod " + array[2] + " {} +");
            curr += "echo \"" + "find " + array[4] + " -type d -exec chmod " + array[2] + " {} +" +
                    "\" " + ">> " + script + " && ";

            Log.v("Recovery Emulator", "find " + array[4] + " -type f -exec chmod " + array[3] + " {} +");
            curr += "echo \"" + "find " + array[4] + " -type f -exec chmod " + array[3] + " {} +" +
                    "\" " + ">> " + script;
        } else if (curr.contains("set_metadata_recursive")) {
            curr = curr.replaceAll("\\)", "");
            curr = curr.replaceAll(";", "");
            curr = curr.replaceAll("set_metadata_recursive\\(", "");
            curr = curr.replaceAll("\"", "");
            String[] array = curr.split(",\\s");

            Log.v("Recovery Emulator", "busybox chown -R " + array[2] + ":" + array[4] + " " +
                    array[0].trim());
            curr = "echo \"" + "busybox chown -R " + array[2] + ":" + array[4] + " " + array[0].trim()
                    + "\" " + ">> " + script + " && ";

            Log.v("Recovery Emulator", "find " + array[0].trim() + " -type d -exec chmod " + array[6]
                    + " {} +");
            curr += "echo \"" + "find " + array[0].trim() + " -type d -exec chmod " + array[6] + " {} " +
                    "+" + "\" >> " + script + " && ";

            Log.v("Recovery Emulator", "find " + array[0].trim() + " -type f -exec chmod " + array[8]
                    + " {} +");
            curr += "echo \"" + "find " + array[0].trim() + " -type f -exec chmod " + array[8] + " {} " +
                    "+" + "\" >> " + script;
            // delete() parsing & translation
        } else if (curr.contains("delete(\"") || curr.contains("delete( \"")) {
            curr = curr.replaceAll(",", " ");
            curr = curr.replaceAll("\"", "");
            curr = curr.replaceAll("\\)", "");
            curr = curr.replaceAll(";", "");
            curr = curr.replaceAll("delete\\(", "busybox rm -f ").trim();
            curr = curr.replaceAll("delete\\( ", "busybox rm -f ").trim();
            Log.v("Recovery Emulator", curr);
            curr = "echo \"" + curr + "\" >> " + script;

            // delete_recursive()
        } else if (curr.contains("delete_recursive(\"") || curr.contains("delete_recursive( \"")) {
            curr = curr.replaceAll(",", "");
            curr = curr.replaceAll("\"", "");
            curr = curr.replaceAll("\\)", "");
            curr = curr.replaceAll(";", "");
            curr = curr.replaceAll("delete_recursive\\(", "busybox rm -rf ").trim();
            curr = curr.replaceAll("delete_recursive\\( ", "busybox rm -rf ").trim();
            Log.v("Recovery Emulator", curr);
            curr = "echo \"" + curr + "\" >> " + script;
            // run_program() parsing & translation
        } else if (curr.contains("run_program(\"")) {
            if (curr.contains("/sbin/busybox")) {
                curr = "";
            } else {
                curr = curr.replaceAll(",", "");
                curr = curr.replaceAll("\"", "");
                curr = curr.replaceAll("\\)", "");
                curr = curr.replaceAll(";", "");
                curr = curr.replaceAll("run_program\\(", "sh ").trim();
                Log.v("Recovery Emulator", curr);
                curr = "echo \"" + curr + "\" >> " + script;
            }

            // mount() / unmount() parsing & translation
//        } else if (curr.contains("mount(")) {
            // write_raw_image() parsing & translation
        } else if (curr.contains("write_raw_image(")) {
            curr = curr.replaceAll("write_raw_image\\(", "dd if=");
            String[] arr = curr.split("\", \"");
            arr[0] = arr[0].replaceAll("\"", "");
            arr[1] = arr[1].replaceAll("\"", "");
            Log.v("Recovery Emulator", arr[0] + " of=" + arr[1]);
            curr = "echo \"" + arr[0] + " of=" + arr[1] + "\" >> " + script;

        } else {
            Log.v("Recovery Emulator", curr);
            curr = "";
        }
        return curr;
    }

}
