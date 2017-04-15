package com.penn.jba;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.penn.jba.databinding.FragmentFootprintBinding;
import com.penn.jba.databinding.FragmentNearbyBinding;

public class NearbyFragment extends Fragment {
    private Context activityContext;

    private FragmentNearbyBinding binding;

    public NearbyFragment() {
        // Required empty public constructor
    }

    public static NearbyFragment newInstance() {
        NearbyFragment fragment = new NearbyFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activityContext = getContext();

        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_nearby, container, false);
        View view = binding.getRoot();
        binding.setPresenter(this);

        return view;
    }
}
