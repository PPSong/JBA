package com.penn.jba;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import com.penn.jba.databinding.FragmentFootprintBinding;
import com.penn.jba.realm.model.CurrentUserSetting;
import io.realm.Realm;

public class FootprintFragment extends Fragment {
    private Context activityContext;

    private FragmentFootprintBinding binding;

    private FragmentPagerAdapter adapterViewPager;

    private Menu menu;

    public FootprintFragment() {
        // Required empty public constructor
    }

    public static FootprintFragment newInstance() {
        FootprintFragment fragment = new FootprintFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityContext = getActivity();
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.footprint_option, menu);
        this.menu = menu;
        toggleMyMoment(false);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.my_footprint:
                toggleMyMoment(true);

                return true;
            default:
                break;
        }

        return false;
    }

    private void toggleMyMoment(boolean change) {
        //根据当前用户的setting来设置图标
        try (Realm realm = Realm.getDefaultInstance()) {
            realm.beginTransaction();

            CurrentUserSetting currentUserSetting = realm.where(CurrentUserSetting.class)
                    .findFirst();

            if (change) {
                //更新当前用户的setting
                currentUserSetting.setFootprintMine(!currentUserSetting.isFootprintMine());
            }

            if (currentUserSetting.isFootprintMine()) {
                menu.getItem(0).setIcon(R.drawable.ic_photo_black_24dp);
                binding.mainViewPager.setCurrentItem(0, false);
            } else {
                menu.getItem(0).setIcon(R.drawable.ic_photo_library_black_24dp);
                binding.mainViewPager.setCurrentItem(1, false);
            }

            realm.commitTransaction();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_footprint, container, false);
        View view = binding.getRoot();
        binding.setPresenter(this);

        adapterViewPager = new MyPagerAdapter(getChildFragmentManager());
        binding.mainViewPager.setAdapter(adapterViewPager);

        return view;
    }

    public class MyPagerAdapter extends FragmentPagerAdapter {
        private final int NUM_ITEMS = 2;

        public MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: // Fragment # 0 - This will show FirstFragment
                    return FootprintAllFragment.newInstance();
                case 1: // Fragment # 0 - This will show FirstFragment different title
                    return FootprintMineFragment.newInstance();
                default:
                    return null;
            }
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0: // Fragment # 0 - This will show FirstFragment
                    return getResources().getString(R.string.all);
                case 1: // Fragment # 0 - This will show FirstFragment different title
                    return getResources().getString(R.string.mine);
                default:
                    return "";
            }
        }
    }
}

