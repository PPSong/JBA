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
import com.penn.jba.realm.model.FootprintMine;

import org.w3c.dom.Text;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by penn on 14/04/2017.
 */

public abstract class PPLoadAdapter<T> extends RecyclerView.Adapter {
    private final int VIEW_PROG = -1;

    public List<T> data;

    public PPLoadAdapter(List<T> data) {
        this.data = data;
    }

    public void needLoadMoreCell() {
        try (Realm realm = Realm.getDefaultInstance()) {
            FootprintMine footprintMine = new FootprintMine();
            footprintMine.setHash("loadMore");
            footprintMine.setType(VIEW_PROG);
            realm.beginTransaction();
            final FootprintMine ft = realm.copyToRealmOrUpdate(footprintMine);
            realm.commitTransaction();
        }
    }

    public void cancelLoadMoreCell() {
        try (Realm realm = Realm.getDefaultInstance()) {
            final FootprintMine ft = realm.where(FootprintMine.class).findFirst();

            realm.beginTransaction();
            ft.deleteFromRealm();
            realm.commitTransaction();
        }
    }

    @Override
    public int getItemViewType(int position) {
        return getRealItemViewType(data.get(position));
    }

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

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
//        if (getItemViewType(position) == VIEW_PROG) {
//            ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
//            ((ProgressViewHolder) holder).bind();
//        } else {
//            onBindRealViewHolder(holder, position);
//        }

        if (holder instanceof ProgressViewHolder) {
            ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
            ((ProgressViewHolder) holder).bind();
        } else {
            onBindRealViewHolder(holder, position);
        }
    }

    @Override
    public int getItemCount() {
        int i = data == null ? 0 : data.size();

        return i;
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;
        public TextView tv;
        public int number = PPHelper.testCount++;

        public ProgressViewHolder(View v) {
            super(v);
            progressBar = (ProgressBar) v.findViewById(R.id.progressBar1);
            tv = (TextView) v.findViewById(R.id.number);
        }

        public void bind() {
            tv.setText("" + number);
        }
    }

    abstract public int getRealItemViewType(T t);

    abstract public RecyclerView.ViewHolder onCreateRealViewHolder(ViewGroup parent, int viewType);

    abstract public void onBindRealViewHolder(RecyclerView.ViewHolder holder, int position);
}
