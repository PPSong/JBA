package com.penn.jba.util;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.penn.jba.R;

import java.util.List;

/**
 * Created by penn on 14/04/2017.
 */

public abstract class PPLoadAdapter<T> extends RecyclerView.Adapter {
    private final int VIEW_PROG = 0;
    private boolean loadMoreCell = false;

    public List<T> data;

    public PPLoadAdapter(List<T> data) {
        this.data = data;
    }

    public void needLoadMoreCell() {
        loadMoreCell = true;
    }

    public void cancelLoadMoreCell() {
        loadMoreCell = false;
    }

    @Override
    public int getItemViewType(int position) {
        Log.v("pplog3", "size:" + data.size());
       // T tmp = data.get(position);
        if (position < data.size()) {
            Log.v("pplog3", "normal," + position);
            return getRealItemViewType(data.get(position));
        } else {
            Log.v("pplog34", "load more cell," + position);
            return VIEW_PROG;
        }
    }

    abstract public int getRealItemViewType(T t);

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_PROG) {
            View v = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.progress_item, parent, false);

            vh = new ProgressViewHolder(v);
        } else {
            vh = onCreateRealViewHolder(parent, viewType);
        }
        return vh;
    }

    abstract public RecyclerView.ViewHolder onCreateRealViewHolder(ViewGroup parent, int viewType);

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ProgressViewHolder) {
            ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
        } else {
            onBindRealViewHolder(holder, position);
        }
    }

    abstract public void onBindRealViewHolder(RecyclerView.ViewHolder holder, int position);

    @Override
    public int getItemCount() {
        int i = data == null ? 0 : data.size();
        if (loadMoreCell) {
            i = i + 1;
        }
        Log.v("pplog3", "i:" + i);
        return i;
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public ProgressViewHolder(View v) {
            super(v);
            progressBar = (ProgressBar) v.findViewById(R.id.progressBar1);
        }
    }
}
