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
public class FeedbackDisplayListener implements DialogInterface.OnShowListener {

    @Override
    public void onShow(DialogInterface dialog) {
        View view = ((MaterialDialog) dialog).getCustomView();
        if (view == null) {
            return;
        }

        String contact = AppHelper.getPrefs().getString("contact", "");
        String feedback = AppHelper.getPrefs().getString("feedback", "");

        if (!TextUtils.isEmpty(contact)) {
            ((MaterialEditText) view.findViewById(R.id.feedback_contact)).setText(contact);
        }
        if (!TextUtils.isEmpty(feedback)) {
            ((MaterialEditText) view.findViewById(R.id.feedback_detail)).setText(feedback);
        }
    }
}
