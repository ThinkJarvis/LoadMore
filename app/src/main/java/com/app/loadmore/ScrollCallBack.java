package com.app.loadmore;

/**
 * 监听滚动开始和停止状态
 */
public interface ScrollCallBack {
    /**
     * 滚动停止
     * @param position 停止的位置
     */
    void onScrollingStop(int position);

    /**
     * 滚动
     */
    void onScrollingStart();




}
