package org.coolapk.gmsinstaller.ui.main.presenter;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.coolapk.gmsinstaller.R;
import org.coolapk.gmsinstaller.model.Gpack;
import org.coolapk.gmsinstaller.ui.UiPresenter;
import org.coolapk.gmsinstaller.ui.main.MainActivity;

import java.io.File;

import lecho.lib.filechooser.FilechooserActivity;
import lecho.lib.filechooser.ItemType;
import lecho.lib.filechooser.SelectionMode;

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
            try {
                Intent intent = new Intent(getActivity(), FilechooserActivity.class);
                intent.putExtra(FilechooserActivity.BUNDLE_ITEM_TYPE, ItemType.FILE);
                intent.putExtra(FilechooserActivity.BUNDLE_SELECTION_MODE, SelectionMode.SINGLE_ITEM);
                getActivity().startActivityForResult(intent, 1);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), R.string.msg_open_chooser_failed, Toast.LENGTH_SHORT).show();
            }
        } else if (v == mFlashBtn) {
            MainActivity.InstallEvent event = new MainActivity.InstallEvent(new File(mWorkingFile).getName());
            event.isLocal = true;
            postEvent(event);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            mWorkingFile = data.getStringArrayListExtra(FilechooserActivity.BUNDLE_SELECTED_PATHS).get(0);
            if (mWorkingFile != null) {
                if (mWorkingFile.endsWith(".zip")) {
                    File file = new File(mWorkingFile);
                    mSelectedText.setText(file.getName());
                    mFlashBtn.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(getContext(), R.string.msg_choose_valid_zip, Toast.LENGTH_SHORT).show();
                    mSelectedText.setText(null);
                    mFlashBtn.setVisibility(View.GONE);
                }
            } else {
                Toast.makeText(getContext(), R.string.msg_choose_file_failed, Toast.LENGTH_SHORT).show();
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
