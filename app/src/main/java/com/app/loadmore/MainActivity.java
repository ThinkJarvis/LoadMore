package com.app.loadmore;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private int index = 0;

    private Handler mHandler = new Handler();

    private PageIndicator mPageIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        initRecyclerView3();
        mPageIndicator = findViewById(R.id.page_indicator);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        recyclerView.setAdapter(new RecyclerAdapter());
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
