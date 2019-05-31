package com.app.loadmore;

import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;


public abstract class BaseAbstractAdapter<T extends Object> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    protected List<T> mList = new ArrayList<>();

    protected int mCurrentSelectedPosition = 0;

    public BaseAbstractAdapter() {
        mList.clear();
    }

    public void setData(List<T> list) {
        mList.clear();
        mList.addAll(list);
    }


    public void addData(List<T> list) {
        mList.addAll(list);
    }


    public void setSelectedPosition(int position) {
        mCurrentSelectedPosition = position;
        notifyDataSetChanged();
    }

    public int getCurrentSelectedPosition() {
        return mCurrentSelectedPosition;
    }
}
