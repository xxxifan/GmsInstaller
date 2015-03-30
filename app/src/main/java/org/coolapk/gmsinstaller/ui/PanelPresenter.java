package org.coolapk.gmsinstaller.ui;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState;

import org.coolapk.gmsinstaller.CardAdapter;
import org.coolapk.gmsinstaller.R;
import org.coolapk.gmsinstaller.model.Gpack;

import java.util.List;

/**
 * Created by BobPeng on 2015/3/27.
 */
public class PanelPresenter {
    private Context mContext;

    private SlidingUpPanelLayout mPanel;
    private TextView mSlidingTitle;
    private TextView mUpdateTimeText;
    private TextView mPackageSizeText;
    private TextView mPackageDetailsText;
    private Button mInstallBtn;
    private Button mUninstallBtn;

    private List<Gpack> mPackageDetails;

    public PanelPresenter(View rootView) {
        mContext = rootView.getContext();

        mPanel = (SlidingUpPanelLayout) rootView.findViewById(R.id.sliding_up_panel);
        mSlidingTitle = (TextView) rootView.findViewById(R.id.sliding_title);
        mUpdateTimeText = (TextView) rootView.findViewById(R.id.update_time);
        mPackageSizeText = (TextView) rootView.findViewById(R.id.package_size);
        mPackageDetailsText = (TextView) rootView.findViewById(R.id.package_detail);
        mInstallBtn = (Button) rootView.findViewById(R.id.package_install_btn);
        mUninstallBtn = (Button) rootView.findViewById(R.id.package_uninstall_btn);
    }

    public void display(int position) {
        mSlidingTitle.setText(CardAdapter.CARD_ITEMS[position]);
        if (mPackageDetails == null) {
            // TODO try fetch details and display null data
        } else {
            // TODO display data from mPackageDetails
        }
        showPanel();
    }

    public void setGappsDetail(List<Gpack> gpackList) {
        mPackageDetails = gpackList;
    }

    public boolean isPanelExpanded() {
        return mPanel.getPanelState() == PanelState.ANCHORED || mPanel.getPanelState() == PanelState.EXPANDED;
    }

    public void collapsePanel() {
        mPanel.setPanelState(PanelState.COLLAPSED);
    }

    public void showPanel() {
        mPanel.setPanelState(PanelState.ANCHORED);
    }
}
