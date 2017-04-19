package com.penn.jba;

import android.content.ClipData;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.penn.jba.realm.model.FootprintAll;
import com.penn.jba.realm.model.FootprintMine;
import com.penn.jba.util.PPHelper;
import com.penn.jba.util.PPLoadAdapter;

import java.util.List;

/**
 * Created by penn on 14/04/2017.
 */

public class FootprintAllAdapter extends PPLoadAdapter<FootprintAll> {
    public FootprintAllAdapter(List<FootprintAll> data) {
        super(data);
    }

    @Override
    public int getRealItemViewType(FootprintAll footprintAll) {
        return footprintAll.getType();
    }

    @Override
    public RecyclerView.ViewHolder onCreateRealViewHolder(ViewGroup parent, int viewType) {
        View v;

        switch (viewType) {
            case 8:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.footprint_type8, parent, false);

                return new PPViewHolder8(v, viewType);
            default:
                Log.v("pplog", "viewType not found:" + viewType);
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_row_all_moment, parent, false);

                return new PPViewHolder(v, viewType);
        }
    }

    @Override
    public void onBindRealViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof PPViewHolder8) {
            ((PPViewHolder8) holder).bind(data.get(position).getHash());
        } else {
            ((PPViewHolder) holder).bind("test");
        }
    }

    public static class PPViewHolder extends RecyclerView.ViewHolder {
        TextView mainText;

        public PPViewHolder(View v, int type) {
            super(v);
            mainText = (TextView) v.findViewById(R.id.main_text);
        }

        public void bind(String s) {
            mainText.setText(s);
        }
    }

    public static class PPViewHolder8 extends RecyclerView.ViewHolder {
        TextView mainText;

        public PPViewHolder8(View v, int type) {
            super(v);
            mainText = (TextView) v.findViewById(R.id.content_tv);
        }

        public void bind(String s) {
            mainText.setText(s);
        }
    }
}
