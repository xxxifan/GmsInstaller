package org.coolapk.gmsinstaller.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.coolapk.gmsinstaller.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by BobPeng on 2015/3/26.
 */
public class CardAdapter extends RecyclerView.Adapter<CardAdapter.ViewHolder> {

    public static int[] CARD_ITEMS = new int[]{
            R.string.title_gapps_framework, R.string.title_gapps_extension, R.string.title_gapps_family
    };

    private Context mContext;
    private List<ItemInfo> mDataList;
    private boolean mClickable;

    private ItemClickListener mItemClickListener;

    public CardAdapter(Context context) {
        mContext = context;

        mDataList = new ArrayList<>();
        mDataList.add(new ItemInfo(R.drawable.ic_framework, mContext.getString(CARD_ITEMS[0])));
        mDataList.add(new ItemInfo(R.drawable.ic_extension, mContext.getString(CARD_ITEMS[1])));
        mDataList.add(new ItemInfo(R.drawable.ic_gapps, mContext.getString(CARD_ITEMS[2]), -1));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(mContext, R.layout.list_item_card, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ItemInfo info = mDataList.get(position);
        holder.imageView.setImageResource(info.icon);
        holder.titleText.setText(info.title);
        if (info.installStatus == 1) {
            holder.actionBtn.setText(mContext.getString(R.string.btn_installed));
            holder.actionBtn.setTextColor(mContext.getResources().getColor(R.color.green));
        } else if (info.installStatus == 0) {
            holder.actionBtn.setText(mContext.getString(R.string.btn_not_installed));
            holder.actionBtn.setTextColor(mContext.getResources().getColor(R.color.pink));
        } else {
            holder.actionBtn.setText(null);
        }
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public List<ItemInfo> getItemInfoList() {
        return mDataList;
    }

    public void setItemClickable(boolean clickable) {
        mClickable = clickable;
    }

    public void setInstallStatus(int position, boolean status) {
        mDataList.get(position).installStatus = status ? 1 : 0;
        notifyItemChanged(position);
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
            int position = getAdapterPosition();
            if (mItemClickListener != null && (mClickable || position > 1)) {
                mItemClickListener.onItemClick(v, position);
            }
        }
    }

    public class ItemInfo {
        public int icon;
        public String title;
        public int installStatus;

        public ItemInfo(int icon, String title) {
            this.icon = icon;
            this.title = title;
        }

        public ItemInfo(int icon, String title, int status) {
            this.icon = icon;
            this.title = title;
            this.installStatus = status;
        }
    }
}
