package com.penn.jba.util;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.penn.jba.R;
import com.penn.jba.realm.model.FootprintMine;
import com.squareup.picasso.Picasso;

/**
 * Created by penn on 21/04/2017.
 */

public class MomentMineImageAdapter extends BaseAdapter {
    private Context mContext;
    private FootprintMine ft;
    private int width;

    public MomentMineImageAdapter(Context c, FootprintMine ft, int width) {
        mContext = c;
        this.ft = ft;
        this.width = width;
    }

    @Override
    public int getCount() {
        return ft.getPicNum();
    }

    @Override
    public Object getItem(int position) {
        if (ft.getStatus() == "local") {
            return ft.getPics().get(position);
        } else {
            return ft.getImages().get(position);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(width, width));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            imageView = (ImageView) convertView;
        }

        if (ft.getStatus().equals("local")) {
            imageView.setImageBitmap(ft.getPics().get(position).getBitmap());
        } else {
            Picasso.with(mContext)
                    .load(PPHelper.getImageUrl(ft.getImages().get(position)))
                    .placeholder(R.drawable.profile).into(imageView);
        }

        return imageView;
    }
}