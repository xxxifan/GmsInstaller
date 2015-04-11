package org.coolapk.gmsinstaller.util;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.coolapk.gmsinstaller.R;
import org.coolapk.gmsinstaller.app.AppHelper;
import org.coolapk.gmsinstaller.model.AppInfo;
import org.coolapk.gmsinstaller.ui.UpdateDialogCallback;

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

    public static void showUpdateDialog(Context context, AppInfo info) {
        View view = View.inflate(context, R.layout.view_update, null);
        TextView version = (TextView) view.findViewById(R.id.update_version_name);
        TextView description = (TextView) view.findViewById(R.id.update_version_description);

        version.setText(info.versionShort + "(" + info.version + ")");
        description.setText(info.changelog);
        new MaterialDialog.Builder(context)
                .title(R.string.title_update_available)
                .customView(view, true)
                .positiveText(R.string.btn_download)
                .negativeText(R.string.btn_close)
                .callback(new UpdateDialogCallback(info))
                .build()
                .show();
    }
}
