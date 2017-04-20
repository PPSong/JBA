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
import com.penn.jba.databinding.FootprintType9Binding;
import com.penn.jba.databinding.ListRowAllMomentBinding;
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
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case 8:
                FootprintType8Binding binding8 = FootprintType8Binding.inflate(layoutInflater, parent, false);
                return new FootprintType8ViewHolder(binding8);
            case 9:
                FootprintType9Binding binding9 = FootprintType9Binding.inflate(layoutInflater, parent, false);
                return new FootprintType9ViewHolder(binding9);
            default:
                ListRowAllMomentBinding binding = ListRowAllMomentBinding.inflate(layoutInflater, parent, false);
                return new PPViewHolder(binding);
        }
    }

    @Override
    public void onBindRealViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof FootprintType8ViewHolder) {
            ((FootprintType8ViewHolder) holder).bind(data.get(position));
        } else if (holder instanceof FootprintType9ViewHolder) {
            ((FootprintType9ViewHolder) holder).bind(data.get(position));
        } else {
            ((PPViewHolder) holder).bind(data.get(position));
        }
    }

    public static class PPViewHolder extends RecyclerView.ViewHolder {
        private final ListRowAllMomentBinding binding;

        public PPViewHolder(ListRowAllMomentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(FootprintAll ft) {
            binding.setPresenter(ft);
            binding.executePendingBindings();
        }
    }

    public class FootprintType8ViewHolder extends RecyclerView.ViewHolder {
        private final FootprintType8Binding binding;

        public FootprintType8ViewHolder(FootprintType8Binding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(FootprintAll ft) {
            binding.setPresenter(ft);
            binding.executePendingBindings();
            binding.timeLineInclude.timeTv.setReferenceTime(ft.getCreateTime());
            Picasso.with(PPApplication.getContext())
                    .load(PPHelper.getImageUrl(ft.getAvatarName()))
                    .placeholder(R.drawable.profile).into(binding.avatarImg);
        }
    }

    public class FootprintType9ViewHolder extends RecyclerView.ViewHolder {
        private final FootprintType9Binding binding;

        public FootprintType9ViewHolder(FootprintType9Binding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(FootprintAll ft) {
            binding.setPresenter(ft);
            binding.executePendingBindings();
            binding.timeLineInclude.timeTv.setReferenceTime(ft.getCreateTime());
            Picasso.with(PPApplication.getContext())
                    .load(PPHelper.getImageUrl(ft.getAvatarName()))
                    .placeholder(R.drawable.profile).into(binding.avatarImg);
        }
    }
}
