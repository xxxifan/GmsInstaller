package org.coolapk.gmsinstaller.ui.feedback;

import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.coolapk.gmsinstaller.R;
import org.coolapk.gmsinstaller.app.AppHelper;

/**
 * Created by xifan on 15-4-11.
 */
public class FeedBackDismissListener implements MaterialDialog.OnDismissListener {

    @Override
    public void onDismiss(DialogInterface dialog) {
        View view = ((MaterialDialog) dialog).getCustomView();
        if (view == null) {
            return;
        }

        String contact = ((MaterialEditText) view.findViewById(R.id.feedback_contact)).getText()
                .toString();
        String feedback = ((MaterialEditText) view.findViewById(R.id.feedback_detail)).getText()
                .toString();
        if (!TextUtils.isEmpty(contact)) {
            AppHelper.getPrefs().edit().putString("contact", contact).apply();
        }

        if (!TextUtils.isEmpty(feedback)) {
            AppHelper.getPrefs().edit().putString("feedback", feedback).apply();
        } else {
            AppHelper.getPrefs().edit().remove("feedback").apply();
        }
    }
}
