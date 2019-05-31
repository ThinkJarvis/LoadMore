package com.app.loadmore;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class LoadMoreAdapter extends BaseAbstractAdapter {


    public final static int STATE_LOAD_MORE = 1;
    public final static int STATE_LOADING = STATE_LOAD_MORE << 1;
    public final static int STATE_LOAD_END = STATE_LOAD_MORE << 2;
    public final static int STATE_LOAD_ERROR = STATE_LOAD_MORE << 3;

    public final static int TYPE_FOOTER = -1;
    public final static int TYPE_BODY = -2;

    public BaseAbstractAdapter mBodyAdapter;


    public AtomicInteger mCurrentStatus = new AtomicInteger();


    public LoadMoreAdapter(BaseAbstractAdapter bodyAdapter) {
        this.mBodyAdapter = bodyAdapter;
        mCurrentStatus.set(STATE_LOAD_MORE);
    }

    @Override
    public void setData(List list) {
        super.setData(list);
        mBodyAdapter.setData(list);
    }


    @Override
    public void addData(List list) {
        super.addData(list);
        mBodyAdapter.addData(list);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_FOOTER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_load_more, parent, false);
            return new FooterViewHolder(view);
        } else if (viewType == TYPE_BODY) {
            return mBodyAdapter.onCreateViewHolder(parent, viewType);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof FooterViewHolder) {
            FooterViewHolder footerViewHolder = (FooterViewHolder) viewHolder;
            switch (mCurrentStatus.get()) {
                case STATE_LOAD_MORE:
                    footerViewHolder.mLoadTips.setText("加载更多");
                    break;
                case STATE_LOADING:
                    footerViewHolder.mLoadTips.setText("加载中...");
                    break;
                case STATE_LOAD_END:
                    footerViewHolder.mLoadTips.setText("到底了，别拽了");
                    break;
                case STATE_LOAD_ERROR:
                    footerViewHolder.mLoadTips.setText("加载失败请重试");
                    break;
            }
        } else {
            mBodyAdapter.onBindViewHolder(viewHolder, position);
        }
    }

    @Override
    public int getItemCount() {
        return (mBodyAdapter == null || mBodyAdapter.getItemCount() == 0) ? -1 : (mBodyAdapter.getItemCount() + 1);
    }

    @Override
    public int getItemViewType(int position) {
        if (position + 1 == getItemCount()) {
            return TYPE_FOOTER;
        } else {
            return TYPE_BODY;
        }
    }

    public class FooterViewHolder extends RecyclerView.ViewHolder {

        public TextView mLoadTips;

        public FooterViewHolder(@NonNull View itemView) {
            super(itemView);
            mLoadTips = itemView.findViewById(R.id.load_tips);
        }
    }

    public void setLoadStatus(int status) {
        mCurrentStatus.set(status);
    }

    public int getLoadStatus() {
        return mCurrentStatus.get();
    }

}

