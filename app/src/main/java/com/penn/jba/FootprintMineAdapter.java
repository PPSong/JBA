package com.penn.jba;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.penn.jba.databinding.FootprintMineType3Binding;
import com.penn.jba.databinding.FootprintType1Binding;
import com.penn.jba.databinding.FootprintType3Binding;
import com.penn.jba.databinding.FootprintType8Binding;
import com.penn.jba.databinding.FootprintType9Binding;
import com.penn.jba.databinding.ListRowAllMomentBinding;
import com.penn.jba.databinding.ListRowMineMomentBinding;
import com.penn.jba.realm.model.FootprintAll;
import com.penn.jba.realm.model.FootprintMine;
import com.penn.jba.util.MomentImageAdapter;
import com.penn.jba.util.PPHelper;
import com.penn.jba.util.PPLoadAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by penn on 14/04/2017.
 */

public class FootprintMineAdapter extends PPLoadAdapter<FootprintMine> {
    private Context context;

    public FootprintMineAdapter(Context context, List<FootprintMine> data) {
        super(data);
        this.context = context;
    }

    @Override
    public int getRealItemViewType(FootprintMine footprintMine) {
        return footprintMine.getType();
    }

    @Override
    public RecyclerView.ViewHolder onCreateRealViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case 3:
                FootprintMineType3Binding binding3 = FootprintMineType3Binding.inflate(layoutInflater, parent, false);
                return new FootprintMineType3ViewHolder(binding3);
            default:
                ListRowMineMomentBinding binding = ListRowMineMomentBinding.inflate(layoutInflater, parent, false);
                return new PPViewHolder(binding);
        }
    }

    @Override
    public void onBindRealViewHolder(RecyclerView.ViewHolder holder, int position) {
       if (holder instanceof FootprintMineType3ViewHolder) {
            ((FootprintMineType3ViewHolder) holder).bind(data.get(position));
        } else {
            ((PPViewHolder) holder).bind(data.get(position));
        }
    }

    public static class PPViewHolder extends RecyclerView.ViewHolder {
        private final ListRowMineMomentBinding binding;

        public PPViewHolder(ListRowMineMomentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(FootprintMine ft) {
            binding.setPresenter(ft);
            binding.executePendingBindings();
        }
    }

    public class FootprintMineType3ViewHolder extends RecyclerView.ViewHolder {
        private final FootprintMineType3Binding binding;

        public FootprintMineType3ViewHolder(FootprintMineType3Binding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(FootprintMine ft) {
            binding.setPresenter(ft);
            binding.executePendingBindings();
            binding.timeLineInclude.timeTv.setReferenceTime(ft.getCreateTime());

            binding.contentTv.setText(ft.getContent());
            binding.placeTv.setText(ft.getPlace());

            //设置图片
            ArrayList<String> pics = ft.getImages();
//            ArrayList<String> pics = new ArrayList<>();
//            for (int i = 0; i < 0; i++) {
//                pics.add("1488363775010.01Eg703-1024x1024.jpg");
//            }
            int picNum = pics.size();
            int width = 0;
            if (picNum == 0) {
                //do nothing
            } else if (picNum == 1) {
                binding.mainGv.setNumColumns(1);
                width = PPHelper.MomentGridViewWidth;
            } else if (picNum == 2) {
                binding.mainGv.setNumColumns(2);
                width = PPHelper.MomentGridViewWidth / 2;
            } else if (picNum == 3) {
                binding.mainGv.setNumColumns(2);
                width = PPHelper.MomentGridViewWidth / 2;
            } else if (picNum == 4) {
                binding.mainGv.setNumColumns(2);
                width = PPHelper.MomentGridViewWidth / 2;
            } else {
                binding.mainGv.setNumColumns(3);
                width = PPHelper.MomentGridViewWidth / 3;
            }

            final float scale = context.getResources().getDisplayMetrics().density;
            int pixels = (int) (width * scale + 0.5f);
            MomentImageAdapter momentImageAdapter = new MomentImageAdapter(context, pics, pixels);
            binding.mainGv.setAdapter(momentImageAdapter);
        }
    }
}
