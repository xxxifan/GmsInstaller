package org.coolapk.gmsinstaller.ui;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState;

import org.coolapk.gmsinstaller.CardAdapter;
import org.coolapk.gmsinstaller.MainActivity;
import org.coolapk.gmsinstaller.R;
import org.coolapk.gmsinstaller.cloud.CloudHelper;
import org.coolapk.gmsinstaller.model.Gpack;
import org.coolapk.gmsinstaller.model.PackageInfo;
import org.coolapk.gmsinstaller.util.ZipUtils;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by BobPeng on 2015/3/27.
 */
public class PanelPresenter implements View.OnClickListener {
    private Context mContext;

    private SlidingUpPanelLayout mPanel;
    private TextView mSlidingTitle;
    private TextView mUpdateTimeText;
    private TextView mPackageSizeText;
    private TextView mPackageDetailsText;
    private Button mInstallBtn;
    private Button mUninstallBtn;

    private int mDisplayIndex;

    private List<PackageInfo> mPackageInfos;

    public PanelPresenter(View rootView) {
        mContext = rootView.getContext();
        mPackageInfos = new ArrayList<>();

        mPanel = (SlidingUpPanelLayout) rootView.findViewById(R.id.sliding_up_panel);
        mSlidingTitle = (TextView) rootView.findViewById(R.id.sliding_title);
        mUpdateTimeText = (TextView) rootView.findViewById(R.id.update_time);
        mPackageSizeText = (TextView) rootView.findViewById(R.id.package_size);
        mPackageDetailsText = (TextView) rootView.findViewById(R.id.package_detail);
        mInstallBtn = (Button) rootView.findViewById(R.id.package_install_btn);
        mUninstallBtn = (Button) rootView.findViewById(R.id.package_uninstall_btn);

        String[] descriptions = mContext.getResources().getStringArray(R.array.gapps_description);
        for (String descriptor : descriptions) {
            PackageInfo info = new PackageInfo();
            info.setPackageDescription(descriptor);
            mPackageInfos.add(info);
        }

        mInstallBtn.setOnClickListener(this);
        mUninstallBtn.setOnClickListener(this);
    }

    public void display(int position) {
        mSlidingTitle.setText(CardAdapter.CARD_ITEMS[position]);
        PackageInfo packageInfo = mPackageInfos.get(position);
        Gpack pack = packageInfo.getGpack();
        if (pack == null) {
            mUpdateTimeText.setText(R.string.title_no_info);
            mPackageSizeText.setText(R.string.title_no_info);
            mPackageDetailsText.setText(R.string.title_no_info);
            mInstallBtn.setEnabled(false);
            EventBus.getDefault().post(new MainActivity.CheckUpdateEvent());
        } else {
            mUpdateTimeText.setText(pack.updateTime);
            mPackageSizeText.setText(ZipUtils.getFormatSize(Long.parseLong(pack.packageSize)));
            mPackageDetailsText.setText(packageInfo.getPackageDescription());
            mInstallBtn.setEnabled(true);
        }

        if (packageInfo.isInstalled()) {
            mUninstallBtn.setEnabled(true);
            mUninstallBtn.setTextColor(mContext.getResources().getColor(R.color.pink));
        } else {
            mUninstallBtn.setEnabled(false);
            mUninstallBtn.setTextColor(mContext.getResources().getColor(R.color.diabled_text));
        }
        showPanel();
        mDisplayIndex = position;
    }

    public void setGappsDetail(List<Gpack> gpackList) {
        // get proper packages from raw data.
        if (gpackList != null) {
            List<Gpack> list = CloudHelper.getProperPackages(gpackList);
            if (list != null) {
                for (int i = 0; i < list.size(); i++) {
                    mPackageInfos.get(i).setGpack(list.get(i));
                }
            }
        }
    }

    public void setInstallStatus(int position, boolean installed) {
        mPackageInfos.get(position).setInstallState(installed);
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

    @Override
    public void onClick(View v) {
        if (v == mInstallBtn) {
            if (mPackageInfos.get(mDisplayIndex).isInstalled()) {
                // TODO alert already installed
            } else {
                // TODO Start install
            }
        } else if (v == mUninstallBtn) {
            // TODO uninstall confirm
        }
    }
}
