package org.coolapk.gmsinstaller.util;

import android.util.Log;

import java.io.File;
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
    private static final String SCRIPT_PATH = "/META-INF/com/google/android/updater-script";

    public static void parseScript(File targetPath) {
        File scriptFile = new File(targetPath, SCRIPT_PATH);
        if (!scriptFile.exists()) {
            throw new IllegalStateException("Script file doesn't exist!");
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

        if (curr.contains("symlink(")) {
            curr = curr.replaceAll(",", "");
            curr = curr.replaceAll("\"", "");
            curr = curr.replaceAll("\\)", "");
            curr = curr.replaceAll(";", "");
            curr = curr.replaceAll("symlink\\(", "ln -s ");
//        } else if (curr.contains("getprop")) { // avoiding assert() lines
//        } else if (curr.contains("format(")) { // not support
//        } else if (curr.contains("#")) { // keep comment
//        } else if (curr.contains("show_progress(")) { // Deleting useless show_progress() lines
//        } else if (curr.contains("ui_print(")) {// Deleting useless ui_print() lines

        } else if (curr.contains("package_extract_file(")) {
            curr = curr.replaceAll("\"", "");
            curr = curr.replaceAll("\\)", "");
            curr = curr.replaceAll(";", "");

            if (curr.contains("boot.img")) {
                curr = curr.replaceAll("package_extract_file\\(", "dd if=" + script + "/");
                curr = curr.replaceAll(", ", " of=");

                curr = "echo \"" + curr + "\" >> " + script;
            } else {
                curr = curr.replaceAll(",", "");
                curr = curr.replaceAll("package_extract_file\\(", "busybox cp -f " + outputPath +
                        "/").trim();

                curr = "echo \"" + curr + "\" >> " + script;
            }
        } else if (curr.contains("package_extract_dir(")) {
            String original = curr;
            curr = curr.replace("package_extract_dir(\"", "");
            curr = curr.replaceAll("\"", "");
            curr = curr.replaceAll("\\)", "");
            curr = curr.replaceAll(";", "");

            String arr[] = curr.split(", ");
            curr = "echo \"mkdir -p " + arr[1] + "\" >> " + script + " && ";

            original = original.replace("package_extract_dir(\"", "busybox cp -rf " + outputPath +
                    "/").trim();
            original = original.replaceAll("\", \"", "/* ");
            original = original.replaceAll(",", "");
            original = original.replaceAll("\"", "");
            original = original.replaceAll("\\)", "");
            original = original.replaceAll(";", "");
            curr += "echo \"" + original + "\" >> " + script;
        } else if (curr.contains("set_perm(")) {
            curr = curr.replaceAll("\"", "");
            curr = curr.replaceAll("\\)", "");
            curr = curr.replaceAll(";", "");
            curr = curr.replaceAll("set_perm\\(", "");
            String[] array = curr.split(",\\s");

            curr = "echo \"" + "chown " + array[0].trim() + ":" + array[1] + " " + array[3] + "\" >> " +
                    script + " && ";
            curr += "echo \"" + "chmod " + array[2] + " " + array[3] + "\" >> " + script;
        } else if (curr.contains("set_perm_recursive(")) {
            curr = curr.replaceAll("\\)", "");
            curr = curr.replaceAll(";", "");
            curr = curr.replaceAll("set_perm_recursive\\(", "");
            String[] array = curr.split(",\\s");

            curr = "echo \"" + "busybox chown -R " + array[0].trim() + ":" + array[1] + " " + array[4]
                    + "\" >> " + script + " && ";
            curr += "echo \"" + "find " + array[4] + " -type d -exec chmod " + array[2] + " {} +" +
                    "\" " + ">> " + script + " && ";
            curr += "echo \"" + "find " + array[4] + " -type f -exec chmod " + array[3] + " {} +" +
                    "\" " + ">> " + script;
        } else if (curr.contains("set_metadata_recursive")) {
            curr = curr.replaceAll("\\)", "");
            curr = curr.replaceAll(";", "");
            curr = curr.replaceAll("set_metadata_recursive\\(", "");
            curr = curr.replaceAll("\"", "");
            String[] array = curr.split(",\\s");

            curr = "echo \"" + "busybox chown -R " + array[2] + ":" + array[4] + " " + array[0].trim()
                    + "\" " + ">> " + script + " && ";
            curr += "echo \"" + "find " + array[0].trim() + " -type d -exec chmod " + array[6] + " {} " +
                    "+" + "\" >> " + script + " && ";
            curr += "echo \"" + "find " + array[0].trim() + " -type f -exec chmod " + array[8] + " {} " +
                    "+" + "\" >> " + script;
        } else if (curr.contains("delete(\"") || curr.contains("delete( \"")) {
            curr = curr.replaceAll(",", "");
            curr = curr.replaceAll("\"", "");
            curr = curr.replaceAll("\\)", "");
            curr = curr.replaceAll(";", "");
            curr = curr.replaceAll("delete\\(", "busybox rm -f ").trim();
            curr = curr.replaceAll("delete\\( ", "busybox rm -f ").trim();
            curr = "echo \"" + curr + "\" >> " + script;
        } else if (curr.contains("delete_recursive(\"") || curr.contains("delete_recursive( \"")) {
            curr = curr.replaceAll(",", "");
            curr = curr.replaceAll("\"", "");
            curr = curr.replaceAll("\\)", "");
            curr = curr.replaceAll(";", "");
            curr = curr.replaceAll("delete_recursive\\(", "busybox rm -rf ").trim();
            curr = curr.replaceAll("delete_recursive\\( ", "busybox rm -rf ").trim();

            curr = "echo \"" + curr + "\" >> " + script;
        } else if (curr.contains("run_program(\"")) {
            if (curr.contains("/sbin/busybox")) {
                curr = "";
            } else {
                curr = curr.replaceAll(",", "");
                curr = curr.replaceAll("\"", "");
                curr = curr.replaceAll("\\)", "");
                curr = curr.replaceAll(";", "");
                curr = curr.replaceAll("run_program\\(", "sh ").trim();
                curr = "echo \"" + curr + "\" >> " + script;
            }
//        } else if (curr.contains("mount(")) {
        } else if (curr.contains("write_raw_image(")) {
            curr = curr.replaceAll("write_raw_image\\(", "dd if=");
            String[] arr = curr.split("\", \"");
            arr[0] = arr[0].replaceAll("\"", "");
            arr[1] = arr[1].replaceAll("\"", "");
            curr = "echo \"" + arr[0] + " of=" + arr[1] + "\" >> " + script;
        } else {
            Log.v("Recovery Emulator", curr);
            curr = "";
        }
        return curr;
    }

}
