package org.coolapk.gmsinstaller.util;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.coolapk.gmsinstaller.R;
import org.coolapk.gmsinstaller.app.AppHelper;
import org.coolapk.gmsinstaller.model.AppInfo;
import org.coolapk.gmsinstaller.ui.UpdateDialogCallback;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by BobPeng on 2015/3/19.
 */
public class ViewUtils {
    private static float DISPLAY_DENSITY = 0f;

    public static int dp2px(int dp) {
        if (DISPLAY_DENSITY == 0f) {
            DISPLAY_DENSITY = AppHelper.getContext().getResources().getDisplayMetrics().density;
        }
        return (int) (dp * DISPLAY_DENSITY);
    }

    public static void showUpdateDialog(Context context, AppInfo info, boolean showIgnore) {
        View view = View.inflate(context, R.layout.view_update, null);
        TextView version = (TextView) view.findViewById(R.id.update_version_name);
        TextView description = (TextView) view.findViewById(R.id.update_version_description);

        version.setText(info.versionShort + "(" + info.version + ")");
        description.setText(info.changelog);
        new MaterialDialog.Builder(context)
                .title(R.string.title_update_available)
                .customView(view, true)
                .positiveText(R.string.btn_download)
                .negativeText(showIgnore ? R.string.btn_ignore : R.string.btn_close)
                .callback(new UpdateDialogCallback(info, showIgnore))
                .build()
                .show();
    }

    public static void showInstallDialog(Context context, MaterialDialog.ButtonCallback callback) {
        new MaterialDialog.Builder(context)
                .title(R.string.title_warning)
                .content(R.string.title_install_confirm)
                .positiveText(R.string.btn_continue)
                .negativeText(R.string.btn_cancel)
                .callback(callback)
                .build()
                .show();
    }

    /**
     * Fix for Flyme OS and MIUI
     */
    public static boolean setStatusBarDarkIcon(Window window, boolean dark) {
        if (window == null) {
            return false;
        }
        boolean result = false;

        try {
            Field darkFlag = WindowManager.LayoutParams.class
                    .getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
            Field meizuFlags = WindowManager.LayoutParams.class
                    .getDeclaredField("meizuFlags");
            darkFlag.setAccessible(true);
            meizuFlags.setAccessible(true);

            WindowManager.LayoutParams lp = window.getAttributes();
            int bit = darkFlag.getInt(null);
            int value = meizuFlags.getInt(lp);
            if (dark) {
                value |= bit;
            } else {
                value &= ~bit;
            }
            meizuFlags.setInt(lp, value);
            window.setAttributes(lp);
            result = true;
        } catch (Exception e) {
            Log.e("ViewUtils", "No flyme os detected");
        }

        try {
            Class<? extends Window> clazz = window.getClass();
            int darkModeFlag;
            Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
            Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
            darkModeFlag = field.getInt(layoutParams);
            Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
            extraFlagField.invoke(window, dark ? darkModeFlag : 0, darkModeFlag);
        } catch (Exception e) {
            Log.e("ViewUtils", "No MIUI detected");
        }
        return result;
    }

}
