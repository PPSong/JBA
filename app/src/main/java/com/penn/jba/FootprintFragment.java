package com.penn.jba;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.penn.jba.databinding.ActivityLoginBinding;
import com.penn.jba.databinding.ActivityTabsBinding;
import com.penn.jba.databinding.FragmentFootprintBinding;
import com.penn.jba.realm.model.CurrentUser;
import com.penn.jba.realm.model.CurrentUserSetting;
import com.penn.jba.realm.model.FootprintAll;
import com.penn.jba.realm.model.FootprintMine;
import com.penn.jba.util.PPHelper;
import com.penn.jba.util.PPJSONObject;
import com.penn.jba.util.PPLoadAdapter;
import com.penn.jba.util.PPRefreshLoadController;
import com.penn.jba.util.PPRetrofit;
import com.penn.jba.util.PPWarn;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

import static android.R.attr.data;
import static android.R.attr.process;
import static com.penn.jba.util.PPHelper.ppFromString;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FootprintFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FootprintFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FootprintFragment extends Fragment {
    private Context activityContext;

    private FragmentFootprintBinding binding;

    private FragmentPagerAdapter adapterViewPager;

    private Menu menu;




    private Realm realm;

    private RealmResults<FootprintMine> footprintMines;

    private RealmResults<FootprintAll> footprintAlls;

    private FootprintMineAdapter footprintMineAdapter;

    private FootprintAllAdapter footprintAllAdapter;

    private OnFragmentInteractionListener mListener;

    // private AllPPRefreshLoadController allPPRefreshLoadController;

    private MinePPRefreshLoadController minePPRefreshLoadController;

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

            Log.v("pplog6", "isFootprintMine:" + currentUserSetting.isFootprintMine());
            if (currentUserSetting.isFootprintMine()) {
                menu.getItem(0).setIcon(R.drawable.ic_photo_black_24dp);
                binding.mainViewPager.setCurrentItem(0);
            } else {
                menu.getItem(0).setIcon(R.drawable.ic_photo_library_black_24dp);
                binding.mainViewPager.setCurrentItem(1);
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
//        realm = Realm.getDefaultInstance();
//        footprintMines = realm.where(FootprintMine.class).findAllSorted("createTime", Sort.DESCENDING);
//        footprintMines.addChangeListener(changeListener);

//        binding.allRv.setLayoutManager(new LinearLayoutManager(getActivity()));
//        footprintAllAdapter = new FootprintAllAdapter(footprintAlls);
//        binding.allRv.setAdapter(footprintAllAdapter);
//        allPPRefreshLoadController = new AllPPRefreshLoadController(binding.allSwipeRefreshLayout, binding.allRv);

//        binding.myRv.setLayoutManager(new LinearLayoutManager(getActivity()));
//        footprintMineAdapter = new FootprintMineAdapter(footprintMines);
//        binding.myRv.setAdapter(footprintMineAdapter);
//        minePPRefreshLoadController = new MinePPRefreshLoadController(binding.mySwipeRefreshLayout, binding.myRv);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //realm.close();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    private void refreshFootprintMine(String beforeThan, String afterThan) {
        PPJSONObject jBody = new PPJSONObject();
        jBody
                .put("beforeThan", beforeThan)
                .put("afterThan", afterThan);

        final Observable<String> apiResult = PPRetrofit.getInstance().api("footprint.mine", jBody.getJSONObject());
        apiResult
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Consumer<String>() {
                            public void accept(String s) {
                                Log.v("ppLog", "get result:" + s);
                                //jobProcessing.onNext(false);

                                PPWarn ppWarn = PPHelper.ppWarning(s);
                                if (ppWarn != null) {
                                    Toast.makeText(activityContext, ppWarn.msg, Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                //signInOk(s);
                                processFootprintMine(s, true);
                            }
                        },
                        new Consumer<Throwable>() {
                            public void accept(Throwable t1) {
                                //jobProcessing.onNext(false);

                                Toast.makeText(activityContext, t1.getMessage(), Toast.LENGTH_SHORT).show();
                                Log.v("ppLog", "error:" + t1.toString());
                                t1.printStackTrace();
                            }
                        }
                );
    }

    private void processFootprintMine(String s, boolean refresh) {
        try (Realm realm = Realm.getDefaultInstance()) {
            realm.beginTransaction();

            if (refresh) {
                realm.delete(FootprintMine.class);
            }

            JsonArray ja = PPHelper.ppFromString(s, "data").getAsJsonArray();

            for (int i = 0; i < ja.size(); i++) {

                //防止loadmore是查询到已有的记录
                FootprintMine ftm = realm.where(FootprintMine.class)
                        .equalTo("hash", PPHelper.ppFromString(s, "data." + i + ".hash").getAsString())
                        .findFirst();

                if (ftm == null) {
                    ftm = realm.createObject(FootprintMine.class, PPHelper.ppFromString(s, "data." + i + ".hash").getAsString());
                }

                ftm.setCreateTime(PPHelper.ppFromString(s, "data." + i + ".createTime").getAsLong());
                ftm.setId(PPHelper.ppFromString(s, "data." + i + ".id").getAsString());
                ftm.setStatus("net");
                ftm.setBody(PPHelper.ppFromString(s, "data." + i + "").getAsJsonObject().toString());
                Log.v("pplog1", PPHelper.ppFromString(s, "data." + i + "").getAsJsonObject().toString());
            }

            realm.commitTransaction();
        }
    }

    private final OrderedRealmCollectionChangeListener<RealmResults<FootprintMine>> changeListener = new OrderedRealmCollectionChangeListener<RealmResults<FootprintMine>>() {
        @Override
        public void onChange(RealmResults<FootprintMine> collection, OrderedCollectionChangeSet changeSet) {
            // `null`  means the async query returns the first time.
            if (changeSet == null) {
                footprintMineAdapter.notifyDataSetChanged();
                return;
            }
            // For deletions, the adapter has to be notified in reverse order.
            OrderedCollectionChangeSet.Range[] deletions = changeSet.getDeletionRanges();
            for (int i = deletions.length - 1; i >= 0; i--) {
                OrderedCollectionChangeSet.Range range = deletions[i];
                Log.v("pplog1", "getDeletionRanges" + range.length);
                footprintMineAdapter.notifyItemRangeRemoved(range.startIndex, range.length);
            }

            OrderedCollectionChangeSet.Range[] insertions = changeSet.getInsertionRanges();
            for (OrderedCollectionChangeSet.Range range : insertions) {
                Log.v("pplog1", "getInsertionRanges" + range.length);
                footprintMineAdapter.notifyItemRangeInserted(range.startIndex, range.length);
            }

            OrderedCollectionChangeSet.Range[] modifications = changeSet.getChangeRanges();
            for (OrderedCollectionChangeSet.Range range : modifications) {
                Log.v("pplog1", "getChangeRanges" + range.length);
                footprintMineAdapter.notifyItemRangeChanged(range.startIndex, range.length);
            }
        }
    };

//    private class AllPPRefreshLoadController extends PPRefreshLoadController {
//
//        public AllPPRefreshLoadController(SwipeRefreshLayout swipeRefreshLayout, RecyclerView recyclerView) {
//            super(swipeRefreshLayout, recyclerView);
//        }
//
//        @Override
//        public void doRefresh() {
//            swipeRefreshLayout.setRefreshing(true);
//            //...process
//            Log.v("pplog", "doRefresh AllPPRefreshLoadController");
//            swipeRefreshLayout.setRefreshing(false);
//            end();
//        }
//
//        @Override
//        public void doLoadMore() {
//            //...process
//            end();
//        }
//    }

    private class MinePPRefreshLoadController extends PPRefreshLoadController {

        public MinePPRefreshLoadController(SwipeRefreshLayout swipeRefreshLayout, RecyclerView recyclerView) {
            super(swipeRefreshLayout, recyclerView);
        }

        @Override
        public void doRefresh() {
            Log.v("pplog2", "doRefresh MinePPRefreshLoadController");
            PPJSONObject jBody = new PPJSONObject();
            jBody
                    .put("beforeThan", "")
                    .put("afterThan", "");

            final Observable<String> apiResult = PPRetrofit.getInstance().api("footprint.mine", jBody.getJSONObject());
            apiResult
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            new Consumer<String>() {
                                public void accept(String s) {
                                    Log.v("ppLog", "get result:" + s);
                                    //jobProcessing.onNext(false);

                                    PPWarn ppWarn = PPHelper.ppWarning(s);
                                    if (ppWarn != null) {
                                        Toast.makeText(activityContext, ppWarn.msg, Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    //signInOk(s);
                                    processFootprintMine(s, true);
                                    swipeRefreshLayout.setRefreshing(false);
                                    end();
                                    Log.v("ppLog2", "get result end:");
                                }
                            },
                            new Consumer<Throwable>() {
                                public void accept(Throwable t1) {
                                    //jobProcessing.onNext(false);

                                    Toast.makeText(activityContext, t1.getMessage(), Toast.LENGTH_SHORT).show();
                                    Log.v("ppLog", "error:" + t1.toString());
                                    swipeRefreshLayout.setRefreshing(false);
                                    end();
                                    Log.v("ppLog2", "get result error:");
                                    t1.printStackTrace();
                                }
                            }
                    );
        }

        @Override
        public void doLoadMore() {
            PPJSONObject jBody = new PPJSONObject();
            jBody
                    .put("beforeThan", "" + footprintMines.last().getHash())
                    .put("afterThan", "");

            final Observable<String> apiResult = PPRetrofit.getInstance().api("footprint.mine", jBody.getJSONObject());
            apiResult
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            new Consumer<String>() {
                                public void accept(String s) {
                                    Log.v("ppLog", "get result:" + s);

                                    PPWarn ppWarn = PPHelper.ppWarning(s);
                                    if (ppWarn != null) {
                                        Toast.makeText(activityContext, ppWarn.msg, Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    processFootprintMine(s, false);

                                    PPLoadAdapter tmp = ((PPLoadAdapter) (recyclerView.getAdapter()));
                                    tmp.cancelLoadMoreCell();
                                    tmp.notifyItemRemoved(tmp.data.size());
                                    end();
                                }
                            },
                            new Consumer<Throwable>() {
                                public void accept(Throwable t1) {
                                    //jobProcessing.onNext(false);

                                    Toast.makeText(activityContext, t1.getMessage(), Toast.LENGTH_SHORT).show();
                                    Log.v("ppLog", "error:" + t1.toString());
                                    t1.printStackTrace();

                                    PPLoadAdapter tmp = ((PPLoadAdapter) (recyclerView.getAdapter()));
                                    tmp.cancelLoadMoreCell();
                                    tmp.notifyItemRemoved(tmp.data.size());
                                    end();
                                }
                            }
                    );
        }
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

