package org.coolapk.gmsinstaller.util;

import org.coolapk.gmsinstaller.app.AppHelper;

/**
 * Created by BobPeng on 2015/3/19.
 */
public class ViewUtils {
    private static float DISPLAY_DENSITY = 0f;

    public static int dp2px(int dp) {
        if (DISPLAY_DENSITY == 0f) {
            DISPLAY_DENSITY = AppHelper.getAppContext().getResources().getDisplayMetrics().density;
        }
        return (int) (dp * DISPLAY_DENSITY);
    }
}
