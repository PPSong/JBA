package com.penn.jba;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
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
import com.penn.jba.databinding.FragmentFootprintAllBinding;
import com.penn.jba.realm.model.FootprintAll;
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

public class FootprintAllFragment extends Fragment {
    private Context activityContext;

    private Realm realm;

    private RealmResults<FootprintAll> footprintAlls;

    private FootprintAllAdapter footprintAllAdapter;

    private FragmentFootprintAllBinding binding;

    private InnerPPRefreshLoadController ppRefreshLoadController;

    public FootprintAllFragment() {
        // Required empty public constructor
    }

    public static FootprintAllFragment newInstance() {
        FootprintAllFragment fragment = new FootprintAllFragment();
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

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_footprint_all, container, false);
        View view = binding.getRoot();
        binding.setPresenter(this);

        realm = Realm.getDefaultInstance();
        footprintAlls = realm.where(FootprintAll.class).findAllSorted("createTime", Sort.DESCENDING);
        footprintAlls.addChangeListener(changeListener);

        binding.mainRv.setLayoutManager(new LinearLayoutManager(getActivity()));
        footprintAllAdapter = new FootprintAllAdapter(footprintAlls);
        binding.mainRv.setAdapter(footprintAllAdapter);

        binding.mainRv.setHasFixedSize(true);

        ppRefreshLoadController = new InnerPPRefreshLoadController(binding.mainSwipeRefreshLayout, binding.mainRv);

        return view;
    }

    //-----helper-----
    private final OrderedRealmCollectionChangeListener<RealmResults<FootprintAll>> changeListener = new OrderedRealmCollectionChangeListener<RealmResults<FootprintAll>>() {
        @Override
        public void onChange(RealmResults<FootprintAll> collection, OrderedCollectionChangeSet changeSet) {
            // `null`  means the async query returns the first time.
            if (changeSet == null) {
                footprintAllAdapter.notifyDataSetChanged();
                return;
            }
            // For deletions, the adapter has to be notified in reverse order.
            OrderedCollectionChangeSet.Range[] deletions = changeSet.getDeletionRanges();
            for (int i = deletions.length - 1; i >= 0; i--) {
                OrderedCollectionChangeSet.Range range = deletions[i];
                footprintAllAdapter.notifyItemRangeRemoved(range.startIndex, range.length);
            }

            OrderedCollectionChangeSet.Range[] insertions = changeSet.getInsertionRanges();
            for (OrderedCollectionChangeSet.Range range : insertions) {
                footprintAllAdapter.notifyItemRangeInserted(range.startIndex, range.length);
            }

            OrderedCollectionChangeSet.Range[] modifications = changeSet.getChangeRanges();
            for (OrderedCollectionChangeSet.Range range : modifications) {
                footprintAllAdapter.notifyItemRangeChanged(range.startIndex, range.length);
            }
        }
    };

    private int processFootprintAll(String s, boolean refresh) {
        try (Realm realm = Realm.getDefaultInstance()) {
            realm.beginTransaction();

            if (refresh) {
                realm.delete(FootprintAll.class);
            }

            JsonArray ja = PPHelper.ppFromString(s, "data").getAsJsonArray();

            int realNum = 0;
            for (int i = 0; i < ja.size(); i++) {

                //防止loadmore是查询到已有的记录
                FootprintAll ftm = realm.where(FootprintAll.class)
                        .equalTo("hash", PPHelper.ppFromString(s, "data." + i + ".hash").getAsString())
                        .findFirst();

                if (ftm == null) {
                    ftm = realm.createObject(FootprintAll.class, PPHelper.ppFromString(s, "data." + i + ".hash").getAsString());
                    realNum++;
                }

                ftm.setCreateTime(PPHelper.ppFromString(s, "data." + i + ".createTime").getAsLong());
                ftm.setId(PPHelper.ppFromString(s, "data." + i + ".id").getAsString());
                ftm.setStatus("net");
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

            final Observable<String> apiResult = PPRetrofit.getInstance().api("footprint.mine", jBody.getJSONObject());
            apiResult
                    .subscribeOn(Schedulers.io())
                    .map(new Function<String, String>() {
                        @Override
                        public String apply(String s) throws Exception {
                            PPWarn ppWarn = PPHelper.ppWarning(s);

                            if (ppWarn != null) {
                                return ppWarn.msg;
                            } else {
                                processFootprintAll(s, true);
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
            PPJSONObject jBody = new PPJSONObject();
            jBody
                    .put("beforeThan", "" + footprintAlls.last().getHash())
                    .put("afterThan", "");

            final Observable<String> apiResult = PPRetrofit.getInstance().api("footprint.mine", jBody.getJSONObject());
            apiResult
                    .subscribeOn(Schedulers.io())
                    .map(new Function<String, String>() {
                        @Override
                        public String apply(String s) throws Exception {
                            PPWarn ppWarn = PPHelper.ppWarning(s);

                            if (ppWarn != null) {
                                return ppWarn.msg;
                            } else {
                                if (processFootprintAll(s, false) == 0) {
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
