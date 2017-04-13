package com.penn.jba;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.widget.LinearLayoutManager;
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
import com.penn.jba.databinding.FragmentFootprintBinding;
import com.penn.jba.realm.model.CurrentUser;
import com.penn.jba.realm.model.CurrentUserSetting;
import com.penn.jba.realm.model.FootprintMine;
import com.penn.jba.util.PPHelper;
import com.penn.jba.util.PPJSONObject;
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
import io.realm.Realm;
import io.realm.RealmResults;

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

    private Menu menu;

    private Realm realm;

    private RealmResults<FootprintMine> footprintMines;

    private OnFragmentInteractionListener mListener;

    private FragmentFootprintBinding binding;

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
        Log.v("ppLog", "fragment onCreate");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.v("ppLog", "onCreateOptionsMenu");
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
                refreshFootprintMine("", "");
            }

            if (currentUserSetting.isFootprintMine()) {
                menu.getItem(0).setIcon(R.drawable.ic_photo_black_24dp);
                binding.myRv.setVisibility(View.VISIBLE);
                binding.allRv.setVisibility(View.INVISIBLE);
            } else {
                menu.getItem(0).setIcon(R.drawable.ic_photo_library_black_24dp);
                binding.allRv.setVisibility(View.VISIBLE);
                binding.myRv.setVisibility(View.INVISIBLE);
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

        realm = Realm.getDefaultInstance();
        footprintMines = realm.where(FootprintMine.class).findAll();
        
        binding.allRv.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.allRv.setAdapter(new FootprintAdapter(footprintMines));

        binding.myRv.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.myRv.setAdapter(new FootprintAdapter(footprintMines));

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        realm.close();
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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
                                processFootprintMine(s);
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

    private void processFootprintMine(String s) {
        try (Realm realm = Realm.getDefaultInstance()) {
            realm.beginTransaction();

            //remove old
            realm.delete(FootprintMine.class);

            JsonArray ja = PPHelper.ppFromString(s, "data").getAsJsonArray();
            for (int i = 0; i < ja.size(); i++) {
                FootprintMine ftm = realm.createObject(FootprintMine.class, PPHelper.ppFromString(s, "data." + i + ".hash").getAsString());
                ftm.setCreateTime(PPHelper.ppFromString(s, "data." + i + ".createTime").getAsLong());
                ftm.setCreateTime(PPHelper.ppFromString(s, "data." + i + ".id").getAsLong());
                ftm.setStatus("net");
                ftm.setBody(s);
            }

            realm.commitTransaction();
        }
    }
}