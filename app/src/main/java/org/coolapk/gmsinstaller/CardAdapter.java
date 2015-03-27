package org.coolapk.gmsinstaller;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by BobPeng on 2015/3/26.
 */
public class CardAdapter extends RecyclerView.Adapter<CardAdapter.ViewHolder> {

    public static final int STATUS_FW_INSTALLED = 1;
    public static final int STATUS_EX_INSTALLED = 2;

    private Context mContext;
    private List<ItemInfo> mArray;
    private boolean mIsInstalled;

    private ItemClickListener mItemClickListener;

    public CardAdapter(Context context) {
        mContext = context;

        mArray = new ArrayList<>();
        mArray.add(new ItemInfo(R.drawable.ic_framework, mContext.getString(R.string
                .title_gapps_framework)));
        mArray.add(new ItemInfo(R.drawable.ic_extension, mContext.getString(R.string
                .title_gapps_extension)));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(mContext, R.layout.list_item_card, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.imageView.setImageResource(mArray.get(position).icon);
        holder.titleText.setText(mArray.get(position).title);
        if (mIsInstalled) {
            holder.actionBtn.setText(mContext.getString(R.string.btn_installed));
            holder.actionBtn.setTextColor(mContext.getResources().getColor(R.color.green));
        } else {
            holder.actionBtn.setText(mContext.getString(R.string.btn_not_installed));
            holder.actionBtn.setTextColor(mContext.getResources().getColor(R.color.pink));
        }
    }

    public void setInstallStatus(boolean status) {
        mIsInstalled = status;
    }

    @Override
    public int getItemCount() {
        return mArray.size();
    }

    public void setOnItemClickListener(ItemClickListener listener) {
        mItemClickListener = listener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView imageView;
        public TextView titleText;
        public TextView actionBtn;

        public ViewHolder(View view) {
            super(view);
            imageView = (ImageView) view.findViewById(R.id.list_icon);
            titleText = (TextView) view.findViewById(R.id.list_title);
            actionBtn = (TextView) view.findViewById(R.id.list_status_text);
            view.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(v, getAdapterPosition());
            }
        }
    }

    private class ItemInfo {
        public int icon;
        public String title;

        public ItemInfo(int icon, String title) {
            this.icon = icon;
            this.title = title;
        }
    }
}
