package com.app.loadmore;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class RadioNetAdapter extends BaseAbstractAdapter<NetProgramInfo> {


    public RadioNetAdapter() {
        super();
    }

    @Override
    public void setData(List<NetProgramInfo> list) {
        super.setData(list);
    }


    public NetProgramInfo getNetProgramInfo(int position) {
        return mList.get(position);
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_net_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

    }


    @Override
    public int getItemCount() {
        return mList.size();
    }


    class ItemViewHolder extends RecyclerView.ViewHolder {


        public ItemViewHolder(View itemView) {
            super(itemView);

        }
    }
}
