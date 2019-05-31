package com.app.loadmore;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;


public abstract class HorizontalLoadMoreListener extends RecyclerView.OnScrollListener {

    private boolean isSlidingLeftward = false;


    @Override
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        MediaListLayoutManager manager = (MediaListLayoutManager) recyclerView.getLayoutManager();
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            int lastItemPosition = manager.findLastVisiblePosition();
            Log.e("wjq", "lastItemPosition = " + lastItemPosition);
//            int itemCount = manager.getItemCount();
//
//            if (lastItemPosition == (itemCount - 1) && isSlidingLeftward) {
//                onLoadMore();
//            }
        }
    }


    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        isSlidingLeftward = dx > 0;
    }

    public abstract void onLoadMore();
}
