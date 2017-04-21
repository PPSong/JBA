package com.penn.jba.util;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.penn.jba.PPApplication;
import com.penn.jba.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by penn on 21/04/2017.
 */

public class MomentImageAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<String> data;
    private int width;

    public MomentImageAdapter(Context c, ArrayList<String> data, int width) {
        mContext = c;
        this.data = data;
        this.width = width;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
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

        Picasso.with(mContext)
                .load(PPHelper.getImageUrl(data.get(position)))
                .placeholder(R.drawable.profile).into(imageView);

        return imageView;
    }
}