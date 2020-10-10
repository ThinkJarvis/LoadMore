package com.app.loadmore;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private int index = 0;

    private Handler mHandler = new Handler();

    private PageIndicator mPageIndicator;


    float mDistanceX = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        initRecyclerView3();
        mPageIndicator = findViewById(R.id.page_indicator);
        final RecyclerView recyclerView = findViewById(R.id.recycler_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(new RecyclerAdapter());

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                //整体的总宽度，注意是整体，包括在显示区域之外的。
                int range = recyclerView.computeHorizontalScrollRange();
                //计算出溢出部分的宽度，即屏幕外剩下的宽度
                float maxDistanceX = range - recyclerView.getMeasuredWidth();
                //滑动的距离
                mDistanceX += dx;
                //计算比例
                float proportion = mDistanceX / maxDistanceX;
                Log.d("wjq", "range = " + range + " | maxDistanceX = " + maxDistanceX + " | mEndX = " + mDistanceX + " | proportion = " + proportion);

            }
        });
    }




































































    private void initRecyclerView3() {


        RecyclerView recyclerView = findViewById(R.id.recycler_view);

        RadioNetAdapter myAdapter = new RadioNetAdapter();
        final LoadMoreProgressAdapter loadMoreAdapter = new LoadMoreProgressAdapter(myAdapter);


        MediaListLayoutManager mediaListLayoutManager = new MediaListLayoutManager(-30);

        mediaListLayoutManager.attach(recyclerView);

        recyclerView.setAdapter(loadMoreAdapter);
        loadMoreAdapter.setData(getNetPrograms(index));
        loadMoreAdapter.setLoadStatus(LoadMoreAdapter.STATE_LOAD_MORE);
        loadMoreAdapter.notifyDataSetChanged();


        mediaListLayoutManager.setScrollCallBack(new ScrollCallBack() {
            @Override
            public void onScrollingStop(int position) {
                if (loadMoreAdapter.getItemViewType(position) == LoadMoreAdapter.TYPE_FOOTER && loadMoreAdapter.getLoadStatus() == LoadMoreAdapter.STATE_LOAD_MORE) {
                    loadMoreAdapter.setLoadStatus(LoadMoreAdapter.STATE_LOADING);
                    loadMoreAdapter.notifyDataSetChanged();
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (index < 5) {
                                index = index + 1;
                                loadMoreAdapter.addData(getNetPrograms(index));
                                loadMoreAdapter.setLoadStatus(LoadMoreAdapter.STATE_LOAD_MORE);
                                loadMoreAdapter.notifyDataSetChanged();
                                Log.e("wjq", "load more");
                            } else {
                                loadMoreAdapter.setLoadStatus(LoadMoreAdapter.STATE_LOAD_END);
                                loadMoreAdapter.notifyDataSetChanged();
                                Log.e("wjq", "load end");
                            }
                        }
                    }, 1000 * 3);
                }
            }

            @Override
            public void onScrollingStart() {

            }
        });

    }


    private List<NetProgramInfo> getNetPrograms(int index) {
        List<NetProgramInfo> list = new ArrayList<>();
        for (int i = index * 20; i < 20 * (index + 1); i++) {
            NetProgramInfo netProgramInfo = new NetProgramInfo();
            list.add(netProgramInfo);
        }
        return list;
    }
}
