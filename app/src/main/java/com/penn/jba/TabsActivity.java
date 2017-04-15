package com.penn.jba;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.penn.jba.databinding.ActivityLoginBinding;
import com.penn.jba.databinding.ActivityTabsBinding;
import com.penn.jba.realm.model.CurrentUser;
import com.penn.jba.realm.model.CurrentUserSetting;
import com.penn.jba.util.PPHelper;
import com.penn.jba.util.PPRetrofit;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.Realm;

public class TabsActivity extends AppCompatActivity implements Drawer.OnDrawerItemClickListener, FootprintFragment.OnFragmentInteractionListener, NearbyFragment.OnFragmentInteractionListener {
    private Context activityContext;

    private ActivityTabsBinding binding;

    private FragmentPagerAdapter adapterViewPager;

    private Drawer drawerResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //pptodo remove this testing entry
//        PPHelper.initRealm(this, "18602103868");
//        try (Realm realm = Realm.getDefaultInstance()) {
//
//            CurrentUser currentUser = realm.where(CurrentUser.class)
//                    .findFirst();
//
//            //设置PPRetrofit authBody
//            try {
//                String authBody = new JSONObject()
//                        .put("userid", currentUser.getUserId())
//                        .put("token", currentUser.getToken())
//                        .put("tokentimestamp", currentUser.getTokenTimestamp())
//                        .toString();
//                PPRetrofit.authBody = authBody;
//            } catch (JSONException e) {
//                Log.v("ppLog", "api data error:" + e);
//            }
//        }

        super.onCreate(savedInstanceState);
        activityContext = this;

        binding = DataBindingUtil.setContentView(this, R.layout.activity_tabs);
        binding.setPresenter(this);

        adapterViewPager = new MyPagerAdapter(getSupportFragmentManager());
        binding.mainViewPager.setAdapter(adapterViewPager);

        binding.mainViewPager.setSwipeable(false);
        binding.mainViewPager.setCurrentItem(0);
        Log.v("ppLog", "getPageTitle:" + adapterViewPager.getPageTitle(0));
        binding.toolbar.setTitle(adapterViewPager.getPageTitle(0));

        setSupportActionBar(binding.toolbar);

        //if you want to update the items at a later time it is recommended to keep it in a variable
        PrimaryDrawerItem item1 = new PrimaryDrawerItem().withIdentifier(1).withName(R.string.footprint).withIcon(R.drawable.ic_collections_black_24dp);
        PrimaryDrawerItem item2 = new PrimaryDrawerItem().withIdentifier(2).withName(R.string.nearby).withIcon(R.drawable.ic_near_me_black_24dp);

        PrimaryDrawerItem item0 = new PrimaryDrawerItem().withIdentifier(0).withName(R.string.logout).withIcon(R.drawable.ic_eject_black_24dp);

        // Create the AccountHeader
        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.header)
                .addProfiles(
                        new ProfileDrawerItem().withName("Mike Penz").withIcon(R.drawable.profile)
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                        return false;
                    }
                })
                .build();


        //create the drawer and remember the `Drawer` result object
        drawerResult = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(binding.toolbar)
                .withAccountHeader(headerResult)
                .addDrawerItems(
                        item1,
                        new DividerDrawerItem(),
                        item2
                )
                .withOnDrawerItemClickListener(this)
                .build();

        drawerResult.addStickyFooterItem(item0);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
        Log.v("ppLog", "test:" + drawerItem.getIdentifier());
        switch ((int) drawerItem.getIdentifier()) {
            case 0:
                //logout
                break;
            case 1:
                //足迹
                binding.mainViewPager.setCurrentItem(0);
                binding.toolbar.setTitle(adapterViewPager.getPageTitle(0));
                drawerResult.closeDrawer();
                break;
            case 2:
                //迹伴
                binding.mainViewPager.setCurrentItem(1);
                binding.toolbar.setTitle(adapterViewPager.getPageTitle(1));
                drawerResult.closeDrawer();
                break;
            default:
        }
        // do something with the clicked item :D
        return true;
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
                    return FootprintFragment.newInstance();
                case 1: // Fragment # 0 - This will show FirstFragment different title
                    return NearbyFragment.newInstance();
                default:
                    return null;
            }
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0: // Fragment # 0 - This will show FirstFragment
                    return getResources().getString(R.string.footprint);
                case 1: // Fragment # 0 - This will show FirstFragment different title
                    return getResources().getString(R.string.nearby);
                default:
                    return "";
            }
        }
    }
}
