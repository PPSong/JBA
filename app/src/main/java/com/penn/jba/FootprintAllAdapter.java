package com.penn.jba;

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

        int layoutId = 0;

        switch (viewType) {
            case 8:
                layoutId = R.layout.footprint_type8;
                break;
            default:
                layoutId = R.layout.list_row_all_moment;
                Log.v("pplog", "viewType not found:" + viewType);
        }

        View v = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);

        return new PPViewHolder(v);
    }

    @Override
    public void onBindRealViewHolder(RecyclerView.ViewHolder holder, int position) {
        //((PPViewHolder) holder).mainText.setText(data.get(position).getHash());
    }

    public static class PPViewHolder extends RecyclerView.ViewHolder {
        public TextView mainText;

        public PPViewHolder(View v) {
            super(v);
            mainText = (TextView) v.findViewById(R.id.main_text);
        }
    }
}
