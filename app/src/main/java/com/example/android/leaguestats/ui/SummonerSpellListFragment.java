package com.example.android.leaguestats.ui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.example.android.leaguestats.R;
import com.example.android.leaguestats.data.database.entity.SummonerSpellEntry;
import com.example.android.leaguestats.utilities.InjectorUtils;
import com.example.android.leaguestats.viewmodels.SummonerSpellModel;
import com.example.android.leaguestats.viewmodels.SummonerSpellModelFactory;
import com.example.android.leaguestats.adapters.SummonerSpellAdapter;
import com.example.android.leaguestats.utilities.LeaguePreferences;

import java.util.ArrayList;
import java.util.List;

public class SummonerSpellListFragment extends Fragment implements SummonerSpellAdapter.SummonerSpellListener {

    private static final String LOG_TAG = SummonerSpellListFragment.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private SummonerSpellAdapter mAdapter;
    private SummonerSpellModel mViewModel;
    private ProgressBar mIndicator;

    public SummonerSpellListFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_list, container, false);

        mRecyclerView = rootView.findViewById(R.id.recycler_view);
        mIndicator = rootView.findViewById(R.id.indicator);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        int gridLayoutColumnCount;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            gridLayoutColumnCount = 3;
        } else {
            gridLayoutColumnCount = 2;
        }

        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), gridLayoutColumnCount));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        String patchVersion = LeaguePreferences.getPatchVersion(getContext());
        mAdapter = new SummonerSpellAdapter(getContext(), new ArrayList<SummonerSpellEntry>(), this, patchVersion);
        mRecyclerView.setAdapter(mAdapter);

        setupViewModel();
    }

    private void setupViewModel() {
        SummonerSpellModelFactory factory =
                InjectorUtils.provideSummonerSpellModelFactory(getActivity().getApplicationContext());
        mViewModel = ViewModelProviders.of(getActivity(), factory).get(SummonerSpellModel.class);
        mViewModel.getSummonerSpells().observe(getActivity(), new Observer<List<SummonerSpellEntry>>() {
            @Override
            public void onChanged(@Nullable List<SummonerSpellEntry> listSummonerSpellEntries) {
                Log.d(LOG_TAG, "Receiving database update from LiveData");
                mAdapter.setData(listSummonerSpellEntries);
                mIndicator.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void onSummonerSpellClick(SummonerSpellEntry summonerSpellEntry) {
        mViewModel.initSummonerSpell(summonerSpellEntry);
        MasterFragment masterFragment = (MasterFragment) getParentFragment();
        masterFragment.addSummonerSpellDetailFragment();
    }
}
