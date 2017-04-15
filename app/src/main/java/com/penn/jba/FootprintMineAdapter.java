package com.penn.jba;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.penn.jba.realm.model.FootprintMine;
import com.penn.jba.util.PPLoadAdapter;

import java.util.List;

/**
 * Created by penn on 14/04/2017.
 */

public class FootprintMineAdapter extends PPLoadAdapter<FootprintMine> {
    public FootprintMineAdapter(List<FootprintMine> data) {
        super(data);
    }

    @Override
    public int getRealItemViewType(FootprintMine footprintMine) {
        //因为my moment只有一种, 所以可以用任意非0数值
        return 3;
    }

    @Override
    public RecyclerView.ViewHolder onCreateRealViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.list_row_my_moment, parent, false);

        return new PPViewHolder(v);
    }

    @Override
    public void onBindRealViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((PPViewHolder) holder).mainText.setText("" + data.get(position).getCreateTime());
    }

    public static class PPViewHolder extends RecyclerView.ViewHolder {
        public TextView mainText;

        public PPViewHolder(View v) {
            super(v);
            mainText = (TextView) v.findViewById(R.id.main_text);
        }
    }
}
