package com.app.loadmore;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

public class LoadMoreProgressAdapter extends LoadMoreAdapter {

    public LoadMoreProgressAdapter(BaseAbstractAdapter bodyAdapter) {
        super(bodyAdapter);

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_FOOTER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_progress_load_more, parent, false);
            return new ProgressBarViewHolder(view);
        } else if (viewType == TYPE_BODY) {
            return mBodyAdapter.onCreateViewHolder(parent, viewType);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof ProgressBarViewHolder) {
            ProgressBarViewHolder progressBarViewHolder = (ProgressBarViewHolder) viewHolder;
            switch (mCurrentStatus.get()) {
                case STATE_LOAD_MORE:
                    endProgressRotate(progressBarViewHolder.mLoadProgressBar);
                    break;
                case STATE_LOADING:
                    startProgressRotate(progressBarViewHolder.mLoadProgressBar);
                    break;
                case STATE_LOAD_END:
                    endProgressRotate(progressBarViewHolder.mLoadProgressBar);
                    break;
                case STATE_LOAD_ERROR:
                    endProgressRotate(progressBarViewHolder.mLoadProgressBar);
                    break;
            }
        } else {
            mBodyAdapter.onBindViewHolder(viewHolder, position);
        }
    }

    private void startProgressRotate(ProgressBar progressBar) {
        progressBar.setIndeterminate(true);
        progressBar.setIndeterminateDrawable(progressBar.getContext().getResources().getDrawable(
                R.drawable.circle_progress_bar));
        progressBar.setProgressDrawable(progressBar.getContext().getResources().getDrawable(
                R.drawable.circle_progress_bar));
    }

    private void endProgressRotate(ProgressBar progressBar) {
        progressBar.setIndeterminateDrawable(progressBar.getContext().getResources().getDrawable(
                R.mipmap.loading));
        progressBar.setProgressDrawable(progressBar.getContext().getResources().getDrawable(
                R.mipmap.loading));
    }


    public class ProgressBarViewHolder extends RecyclerView.ViewHolder {

        public ProgressBar mLoadProgressBar;

        public ProgressBarViewHolder(@NonNull View itemView) {
            super(itemView);
            mLoadProgressBar = itemView.findViewById(R.id.progressbar_loading);
        }
    }

}

