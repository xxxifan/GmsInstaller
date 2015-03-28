package org.coolapk.gmsinstaller.ui;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState;

import org.coolapk.gmsinstaller.CardAdapter;
import org.coolapk.gmsinstaller.R;

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

    public PanelPresenter(View rootView) {
        mContext = rootView.getContext();

        mPanel = (SlidingUpPanelLayout) rootView.findViewById(R.id.sliding_up_panel);
        mSlidingTitle = (TextView) rootView.findViewById(R.id.sliding_title);
        mUpdateTimeText = (TextView) rootView.findViewById(R.id.update_time);
        mPackageSizeText = (TextView) rootView.findViewById(R.id.package_size);
        mPackageDetailsText = (TextView) rootView.findViewById(R.id.package_detail);
    }

    public void display(int position) {
        mSlidingTitle.setText(CardAdapter.CARD_ITEMS[position]);

        showPanel();
    }

    public boolean isPanelExpanded() {
        return mPanel.getPanelState() == PanelState.EXPANDED;
    }

    public void collapsePanel() {
        mPanel.setPanelState(PanelState.COLLAPSED);
    }

    public void showPanel() {
        mPanel.setPanelState(PanelState.ANCHORED);
    }
}
