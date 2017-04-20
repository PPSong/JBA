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

import com.penn.jba.databinding.FootprintType1Binding;
import com.penn.jba.databinding.FootprintType8Binding;
import com.penn.jba.realm.model.FootprintAll;
import com.penn.jba.realm.model.FootprintMine;
import com.penn.jba.util.PPHelper;
import com.penn.jba.util.PPLoadAdapter;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.List;

import io.realm.Realm;

import static com.penn.jba.R.id.avatar_img;
import static com.penn.jba.R.id.imageView;
import static com.penn.jba.R.id.time_tv;
import static java.lang.System.load;

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
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ViewDataBinding binding;

        switch (viewType) {
            case 8:
                binding = DataBindingUtil.inflate(
                        layoutInflater, R.layout.footprint_type8, parent, false);

                FootprintType8Binding binding1 = FootprintType8Binding.inflate(layoutInflater);
                return new MyViewHolder(binding1);
            case 1:
                binding = DataBindingUtil.inflate(
                        layoutInflater, R.layout.footprint_type1, parent, false);
                return new MyViewHolder(binding);
            default:
                Log.v("pplog", "viewType not found:" + viewType);
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_row_all_moment, parent, false);

                return new PPViewHolder(v, viewType);
        }
    }

    @Override
    public void onBindRealViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MyViewHolder) {
            ((MyViewHolder) holder).bind(data.get(position));
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

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private final ViewDataBinding binding;

        public MyViewHolder(ViewDataBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(FootprintAll ft) {
            binding.setVariable(BR.presenter, ft);
            binding.executePendingBindings();

            if (ft.getType() == 8) {
                ((FootprintType8Binding) binding).timeLineInclude.timeTv.setReferenceTime(new Date().getTime());
                Picasso.with(PPApplication.getContext()).load(PPHelper.getImageUrl(ft.getAvatarName())).placeholder(R.drawable.profile).into(((FootprintType8Binding) binding).avatarImg);
            }
        }
    }
}
