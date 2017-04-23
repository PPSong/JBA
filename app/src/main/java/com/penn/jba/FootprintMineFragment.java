package com.penn.jba;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.penn.jba.databinding.FragmentFootprintMineBinding;
import com.penn.jba.realm.model.FootprintAll;
import com.penn.jba.realm.model.FootprintMine;
import com.penn.jba.util.PPHelper;
import com.penn.jba.util.PPJSONObject;
import com.penn.jba.util.PPLoadAdapter;
import com.penn.jba.util.PPRefreshLoadController;
import com.penn.jba.util.PPRetrofit;
import com.penn.jba.util.PPWarn;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class FootprintMineFragment extends Fragment {
    private Context activityContext;

    private Realm realm;

    private RealmResults<FootprintMine> footprintMines;

    private FootprintMineAdapter footprintMineAdapter;

    private FragmentFootprintMineBinding binding;

    private InnerPPRefreshLoadController ppRefreshLoadController;

    public FootprintMineFragment() {
        // Required empty public constructor
    }

    public static FootprintMineFragment newInstance() {
        FootprintMineFragment fragment = new FootprintMineFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityContext = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_footprint_mine, container, false);
        View view = binding.getRoot();
        binding.setPresenter(this);

        realm = Realm.getDefaultInstance();
        footprintMines = realm.where(FootprintMine.class).findAllSorted("createTime", Sort.DESCENDING);
        footprintMines.addChangeListener(changeListener);

        binding.mainRv.setLayoutManager(new LinearLayoutManager(getActivity()));
        footprintMineAdapter = new FootprintMineAdapter(activityContext, footprintMines);
        binding.mainRv.setAdapter(footprintMineAdapter);
        ppRefreshLoadController = new InnerPPRefreshLoadController(binding.mainSwipeRefreshLayout, binding.mainRv);

        return view;
    }

    //-----helper-----
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
                footprintMineAdapter.notifyItemRangeRemoved(range.startIndex, range.length);
            }

            OrderedCollectionChangeSet.Range[] insertions = changeSet.getInsertionRanges();
            for (OrderedCollectionChangeSet.Range range : insertions) {
                Log.v("pplog25", "insert:" + range.startIndex + "," + range.length);
                footprintMineAdapter.notifyItemRangeInserted(range.startIndex, range.length);
            }

            OrderedCollectionChangeSet.Range[] modifications = changeSet.getChangeRanges();
            for (OrderedCollectionChangeSet.Range range : modifications) {
                footprintMineAdapter.notifyItemRangeChanged(range.startIndex, range.length);
            }
        }
    };

    private int processFootprintMine(String s, boolean refresh) {
        try (Realm realm = Realm.getDefaultInstance()) {
            realm.beginTransaction();

            if (refresh) {
                realm.delete(FootprintMine.class);
            }

            JsonArray ja = PPHelper.ppFromString(s, "data").getAsJsonArray();

            int realNum = 0;
            for (int i = 0; i < ja.size(); i++) {

                //防止loadmore是查询到已有的记录
                FootprintMine ftm = realm.where(FootprintMine.class)
                        .equalTo("hash", PPHelper.ppFromString(s, "data." + i + ".hash").getAsString())
                        .findFirst();

                if (ftm == null) {
                    ftm = realm.createObject(FootprintMine.class, PPHelper.ppFromString(s, "data." + i + ".hash").getAsString());
                    realNum++;
                }

                ftm.setCreateTime(PPHelper.ppFromString(s, "data." + i + ".createTime").getAsLong());
                ftm.setId(PPHelper.ppFromString(s, "data." + i + ".id").getAsString());
                ftm.setStatus("net");
                ftm.setType(PPHelper.ppFromString(s, "data." + i + ".type").getAsInt());
                ftm.setBody(PPHelper.ppFromString(s, "data." + i + "").getAsJsonObject().toString());
            }
            realm.commitTransaction();

            return realNum;
        }
    }

    private class InnerPPRefreshLoadController extends PPRefreshLoadController {

        public InnerPPRefreshLoadController(SwipeRefreshLayout swipeRefreshLayout, RecyclerView recyclerView) {
            super(swipeRefreshLayout, recyclerView);
        }

        @Override
        public void doRefresh() {
            PPJSONObject jBody = new PPJSONObject();
            jBody
                    .put("beforeThan", "")
                    .put("afterThan", "");

            final Observable<String> apiResult = PPRetrofit.getInstance().api("footprint.myMoment", jBody.getJSONObject());
            apiResult
                    .subscribeOn(Schedulers.io())
                    .map(new Function<String, String>() {
                        @Override
                        public String apply(String s) throws Exception {
                            PPWarn ppWarn = PPHelper.ppWarning(s);

                            if (ppWarn != null) {
                                return ppWarn.msg;
                            } else {
                                processFootprintMine(s, true);
                                return "OK";
                            }
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            new Consumer<String>() {
                                public void accept(String s) {
                                    if (s != "OK") {
                                        PPHelper.showPPToast(activityContext, s, Toast.LENGTH_SHORT);

                                        return;
                                    }
                                    swipeRefreshLayout.setRefreshing(false);
                                    end();
                                    reset();
                                }
                            },
                            new Consumer<Throwable>() {
                                public void accept(Throwable t1) {
                                    PPHelper.showPPToast(activityContext, t1.getMessage(), Toast.LENGTH_SHORT);

                                    swipeRefreshLayout.setRefreshing(false);
                                    end();

                                    t1.printStackTrace();
                                }
                            }
                    );
        }

        @Override
        public void doLoadMore() {
            Log.v("pplog25", "doLoadMore");
            PPJSONObject jBody = new PPJSONObject();
            jBody
                    .put("beforeThan", "" + footprintMines.last().getHash())
                    .put("afterThan", "");

            final Observable<String> apiResult = PPRetrofit.getInstance().api("footprint.myMoment", jBody.getJSONObject());
            apiResult
                    .subscribeOn(Schedulers.io())
                    .map(new Function<String, String>() {
                        @Override
                        public String apply(String s) throws Exception {
                            PPWarn ppWarn = PPHelper.ppWarning(s);

                            if (ppWarn != null) {
                                return ppWarn.msg;
                            } else {
                                if (processFootprintMine(s, false) == 0) {
                                    noMore();
                                }

                                return "OK";
                            }
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            new Consumer<String>() {
                                public void accept(String s) {
                                    if (s != "OK") {
                                        PPHelper.showPPToast(activityContext, s, Toast.LENGTH_SHORT);

                                        return;
                                    }

                                    PPLoadAdapter tmp = ((PPLoadAdapter) (recyclerView.getAdapter()));
                                    tmp.cancelLoadMoreCell();
                                    tmp.notifyItemRemoved(tmp.data.size());
                                    end();
                                }
                            },
                            new Consumer<Throwable>() {
                                public void accept(Throwable t1) {
                                    PPHelper.showPPToast(activityContext, t1.getMessage(), Toast.LENGTH_SHORT);
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
}
