package com.penn.jba;

import android.content.ClipData;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.penn.jba.databinding.FootprintType0Binding;
import com.penn.jba.databinding.FootprintType10Binding;
import com.penn.jba.databinding.FootprintType1Binding;
import com.penn.jba.databinding.FootprintType3Binding;
import com.penn.jba.databinding.FootprintType8Binding;
import com.penn.jba.databinding.FootprintType9Binding;
import com.penn.jba.databinding.ListRowAllMomentBinding;
import com.penn.jba.realm.model.FootprintAll;
import com.penn.jba.realm.model.FootprintMine;
import com.penn.jba.util.MomentImageAdapter;
import com.penn.jba.util.PPHelper;
import com.penn.jba.util.PPLoadAdapter;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
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
    private Context context;

    public FootprintAllAdapter(Context context, List<FootprintAll> data) {
        super(data);
        this.context = context;
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
            case 1:
                FootprintType1Binding binding1 = FootprintType1Binding.inflate(layoutInflater, parent, false);
                return new FootprintType1ViewHolder(binding1);
            case 3:
                FootprintType3Binding binding3 = FootprintType3Binding.inflate(layoutInflater, parent, false);
                return new FootprintType3ViewHolder(binding3);
            case 10:
                FootprintType10Binding binding10 = FootprintType10Binding.inflate(layoutInflater, parent, false);
                return new FootprintType10ViewHolder(binding10);
            case 0:
                FootprintType0Binding binding0 = FootprintType0Binding.inflate(layoutInflater, parent, false);
                return new FootprintType0ViewHolder(binding0);
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
        } else if (holder instanceof FootprintType1ViewHolder) {
            ((FootprintType1ViewHolder) holder).bind(data.get(position));
        } else if (holder instanceof FootprintType3ViewHolder) {
            ((FootprintType3ViewHolder) holder).bind(data.get(position));
        } else if (holder instanceof FootprintType10ViewHolder) {
            ((FootprintType10ViewHolder) holder).bind(data.get(position));
        } else if (holder instanceof FootprintType0ViewHolder) {
            ((FootprintType0ViewHolder) holder).bind(data.get(position));
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

    public class FootprintType1ViewHolder extends RecyclerView.ViewHolder {
        private final FootprintType1Binding binding;

        public FootprintType1ViewHolder(FootprintType1Binding binding) {
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

    public class FootprintType3ViewHolder extends RecyclerView.ViewHolder {
        private final FootprintType3Binding binding;

        public FootprintType3ViewHolder(FootprintType3Binding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(FootprintAll ft) {
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

    public class FootprintType10ViewHolder extends RecyclerView.ViewHolder {
        private final FootprintType10Binding binding;

        public FootprintType10ViewHolder(FootprintType10Binding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(FootprintAll ft) {
            binding.setPresenter(ft);
            binding.executePendingBindings();
            binding.timeLineInclude.timeTv.setReferenceTime(ft.getCreateTime());
        }
    }

    public class FootprintType0ViewHolder extends RecyclerView.ViewHolder {
        private final FootprintType0Binding binding;

        public FootprintType0ViewHolder(FootprintType0Binding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(FootprintAll ft) {
            binding.setPresenter(ft);
            binding.executePendingBindings();
            binding.timeLineInclude.timeTv.setReferenceTime(ft.getCreateTime());
        }
    }
}
