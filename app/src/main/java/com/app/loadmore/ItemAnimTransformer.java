package com.app.loadmore;

import android.view.View;

/**
 * File description
 * 动效接口做动效需要实现
 * @Title ItemAnimTransformer
 * @Author junyanyang
 * @Email junyanyang@pateo.com.cn
 * @Date 2018/06/08 10:55
 * @Version :v1.0
 ***/
public interface ItemAnimTransformer {
    /**
     *
     * @param layoutManager
     * @param item
     * @param fraction　缩放区域的比例,距离中间距离越短数值越大,最大是１
     */
    void transformItem(MediaListLayoutManager layoutManager, View item, float fraction);
}
