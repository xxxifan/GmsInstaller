package org.coolapk.gmsinstaller.ui;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.coolapk.gmsinstaller.MainActivity;
import org.coolapk.gmsinstaller.R;
import org.coolapk.gmsinstaller.model.Gpack;
import org.coolapk.gmsinstaller.util.FileUtils;

import java.io.File;

/**
 * Created by xifan on 15-4-9.
 */
public class ChooserPresenter extends UiPresenter implements View.OnClickListener {
    private TextView mSelectedText;
    private TextView mChooseBtn;
    private TextView mFlashBtn;

    private String mWorkingFile;

    public ChooserPresenter(View view) {
        super(view);
    }

    @Override
    protected void initView(View rootView) {
        mSelectedText = (TextView) rootView.findViewById(R.id.flash_selected_file_name);
        mChooseBtn = (TextView) rootView.findViewById(R.id.flash_select_btn);
        mFlashBtn = (TextView) rootView.findViewById(R.id.flash_confirm_btn);
        mChooseBtn.setOnClickListener(this);
        mFlashBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == mChooseBtn) {
            Intent intent = FileUtils.createGetZipIntent();
            getActivity().startActivityForResult(intent, FileUtils.REQUEST_CODE);
        } else if (v == mFlashBtn) {
            MainActivity.InstallEvent event = new MainActivity.InstallEvent(new File(mWorkingFile).getName());
            event.isLocal = true;
            postEvent(event);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FileUtils.REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            mWorkingFile = FileUtils.getPath(getContext(), data.getData());
            if (mWorkingFile != null) {
                File file = new File(mWorkingFile);
                mSelectedText.setText(file.getName());
                mFlashBtn.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(getContext(),R.string.msg_choose_file_failed,Toast.LENGTH_SHORT).show();
                mSelectedText.setText(null);
                mFlashBtn.setVisibility(View.GONE);
            }

        }
    }

    public Gpack getWorkingGpack() {
        Gpack gpack = new Gpack();
        gpack.packageName = new File(mWorkingFile).getName();
        return gpack;
    }

    public void clearWorkingFile() {
        mWorkingFile = null;
        mSelectedText.setText(null);
        mFlashBtn.setVisibility(View.GONE);
    }
}
