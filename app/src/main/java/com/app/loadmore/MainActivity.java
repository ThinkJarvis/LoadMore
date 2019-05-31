package com.app.loadmore;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private int index = 0;

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initRecyclerView3();
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

        recyclerView.addOnScrollListener(new HorizontalLoadMoreListener() {
            @Override
            public void onLoadMore() {
                if (loadMoreAdapter.getLoadStatus() == LoadMoreAdapter.STATE_LOAD_MORE) {
                    if (index >= 2) {
                        loadMoreAdapter.setLoadStatus(LoadMoreAdapter.STATE_LOAD_END);
                        loadMoreAdapter.notifyDataSetChanged();
                    } else {
                        loadMoreAdapter.setLoadStatus(LoadMoreAdapter.STATE_LOADING);
                        loadMoreAdapter.notifyDataSetChanged();
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                index++;
                                loadMoreAdapter.setData(getNetPrograms(index));
                                loadMoreAdapter.setLoadStatus(LoadMoreAdapter.STATE_LOAD_MORE);
                                loadMoreAdapter.notifyDataSetChanged();
                            }
                        }, 3000);
                    }
                }


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
