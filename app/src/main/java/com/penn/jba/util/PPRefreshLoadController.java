package com.penn.jba.util;

import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

/**
 * Created by penn on 14/04/2017.
 */

public abstract class PPRefreshLoadController implements SwipeRefreshLayout.OnRefreshListener {
    private boolean loading = false;

    private int visibleThreshold = 5;
    private int lastVisibleItem, totalItemCount;

    public SwipeRefreshLayout swipeRefreshLayout;
    public RecyclerView recyclerView;

    public PPRefreshLoadController(SwipeRefreshLayout swipeRefreshLayout, RecyclerView recyclerView) {
        this.swipeRefreshLayout = swipeRefreshLayout;
        this.swipeRefreshLayout.setOnRefreshListener(this);
        this.recyclerView = recyclerView;

        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {

            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView
                    .getLayoutManager();


            recyclerView
                    .addOnScrollListener(new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrolled(final RecyclerView recyclerView,
                                               int dx, int dy) {
                            super.onScrolled(recyclerView, dx, dy);

                            totalItemCount = linearLayoutManager.getItemCount();
                            lastVisibleItem = linearLayoutManager
                                    .findLastVisibleItemPosition();

                            if (!loading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                                // End has been reached
                                // Do something
                                loadMore(recyclerView);
//                                ((PPLoadAdapter)(recyclerView.getAdapter())).needLoadMoreCell();
//                                recyclerView.post(new Runnable() {
//                                    public void run() {
//                                        ((PPLoadAdapter)(recyclerView.getAdapter())).notifyItemInserted(((PPLoadAdapter)(recyclerView.getAdapter())).data.size());
//                                    }
//                                });
//
//
//                                new Handler().postDelayed(new Runnable() {
//                                                              @Override
//                                                              public void run() {
//                                                                  ((PPLoadAdapter)(recyclerView.getAdapter())).cancelLoadMoreCell();
//                                                                  ((PPLoadAdapter)(recyclerView.getAdapter())).notifyItemRemoved(((PPLoadAdapter)(recyclerView.getAdapter())).data.size());
//                                                                    end();
//                                                              }
//                                                          },2000
//                                );
                            }

                        }
                    });
        }
    }

    private void begin() {
        this.loading = true;
    }

    public void end() {
        this.loading = false;
    }

    public abstract void doRefresh();

    @Override
    public void onRefresh() {
        Log.v("pplog5", "onRefresh:" + loading);
        if (!loading) {
            begin();
            Log.v("pplog5", "onRefresh1:" + loading);
            doRefresh();
            Log.v("pplog5", "onRefresh2:" + loading);
        } else {
            Log.v("pplog5", "do nothing");
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    public abstract void doLoadMore();

    public void loadMore(RecyclerView recyclerView) {
        if (!loading) {
            begin();
            final PPLoadAdapter tmp = ((PPLoadAdapter) (recyclerView.getAdapter()));
            tmp.needLoadMoreCell();
            recyclerView.post(new Runnable() {
                @Override
                public void run() {
                    tmp.notifyItemInserted(tmp.data.size());
                }
            });
            doLoadMore();
        } else {
            //do nothing
        }
    }
}
