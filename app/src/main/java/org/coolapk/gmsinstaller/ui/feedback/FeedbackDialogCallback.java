package org.coolapk.gmsinstaller.ui.feedback;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.feedback.Comment;
import com.avos.avoscloud.feedback.FeedbackAgent;
import com.avos.avoscloud.feedback.FeedbackThread;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.coolapk.gmsinstaller.R;
import org.coolapk.gmsinstaller.app.AppHelper;

import java.util.List;

/**
 * Created by xifan on 15-4-11.
 */
public class FeedbackDialogCallback extends MaterialDialog.ButtonCallback {
    @Override
    public void onPositive(MaterialDialog dialog) {
        Context context = dialog.getContext();
        View view = dialog.getCustomView();
        if (view == null) {
            return;
        }
        MaterialEditText contactEdit = (MaterialEditText) view.findViewById(R.id.feedback_contact);
        MaterialEditText feedbackEdit = (MaterialEditText) view.findViewById(R.id.feedback_detail);

        String contact = contactEdit.getText().toString();
        String feedback = feedbackEdit.getText().toString();
        if (TextUtils.isEmpty(contact)) {
            Toast.makeText(context, R.string.msg_contact_cannot_be_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(feedback)) {
            Toast.makeText(context, R.string.msg_feedback_cannot_be_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        // clear content
        feedbackEdit.setText("");

        // start feedback thread
        FeedbackThread thread = new FeedbackAgent(context).getDefaultThread();
        thread.setContact(contact);
        thread.add(new Comment(feedback));
        thread.sync(new FeedbackThread.SyncCallback() {
            @Override
            public void onCommentsSend(List<Comment> list, AVException e) {
                if (e == null) {
                    Toast.makeText(AppHelper.getContext(), R.string.msg_feedback_success, Toast
                            .LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AppHelper.getContext(), e.getLocalizedMessage(), Toast
                            .LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCommentsFetch(List<Comment> list, AVException e) {
            }
        });

    }
}
