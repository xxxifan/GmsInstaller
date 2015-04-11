package org.coolapk.gmsinstaller.ui;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by BobPeng on 2015/3/26.
 */
public class CardLayoutManager extends LinearLayoutManager {
    public CardLayoutManager(Context context) {
        super(context);
    }

    @Override
    public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state, int widthSpec, int heightSpec) {
        View view = recycler.getViewForPosition(0);
        if (view != null) {
            measureChild(view, widthSpec, heightSpec);
            int measuredWidth = View.MeasureSpec.getSize(widthSpec);
            int measuredHeight = view.getMeasuredHeight() * state.getItemCount();
            setMeasuredDimension(measuredWidth, measuredHeight);
        }
    }
}
