package com.example.android.leaguestats;

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
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.leaguestats.viewModels.ItemViewModelShared;
import com.example.android.leaguestats.viewModels.ItemViewModelSharedFactory;
import com.example.android.leaguestats.adapters.ItemAdapter;
import com.example.android.leaguestats.database.AppDatabase;
import com.example.android.leaguestats.database.ItemEntry;
import com.example.android.leaguestats.utilities.DataUtils;
import com.example.android.leaguestats.utilities.PreferencesUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ItemDetailFragment extends Fragment
        implements ItemAdapter.ItemClickListener {

    private static final String LOG_TAG = ItemDetailFragment.class.getSimpleName();
    private String mPatchVersion;
    private TextView mBaseGoldLabel, mTotalGoldLabel, mSellGoldLabel, mDescriptionLabel,
            mPlainTextLabel, mFromLabel, mIntoLabel;
    private TextView mItemNameTv, mBaseGoldTv, mTotalGoldTv, mSellGoldTv, mDescriptionTv,
            mPlainTextTv;
    private ImageView mImage;
    private ItemAdapter mItemFromAdapter;
    private ItemAdapter mItemIntoAdapter;
    private RecyclerView mFromRecycler;
    private RecyclerView mIntoRecycler;
    private String FROM_LAYOUT_MANAGER_STATE_KEY = "fromLayoutManagerStateKey";
    private String INTO_LAYOUT_MANAGER_STATE_KEY = "intoLayoutManagerStateKey";
    private ItemViewModelShared mViewModel;
    private AppDatabase mDb;

    public ItemDetailFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_item_detail, container, false);
        Log.d(LOG_TAG, "onCreateView");

        mBaseGoldLabel = rootView.findViewById(R.id.base_gold_label);
        mTotalGoldLabel = rootView.findViewById(R.id.total_gold_label);
        mSellGoldLabel = rootView.findViewById(R.id.sell_gold_label);
        mDescriptionLabel = rootView.findViewById(R.id.item_description_label);
        mPlainTextLabel = rootView.findViewById(R.id.plain_text_label);
        mFromLabel = rootView.findViewById(R.id.item_from_label);
        mIntoLabel = rootView.findViewById(R.id.item_into_label);

        mItemNameTv = rootView.findViewById(R.id.item_detail_name);
        mBaseGoldTv = rootView.findViewById(R.id.base_gold_tv);
        mTotalGoldTv = rootView.findViewById(R.id.total_gold_tv);
        mSellGoldTv = rootView.findViewById(R.id.sell_gold_tv);
        mDescriptionTv = rootView.findViewById(R.id.item_description_tv);
        mPlainTextTv = rootView.findViewById(R.id.plain_text_tv);
        mImage = rootView.findViewById(R.id.item_detail_image);

        mFromRecycler = rootView.findViewById(R.id.item_from_recycler);
        mIntoRecycler = rootView.findViewById(R.id.item_into_recycler);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(LOG_TAG, "onActivityCreated");

        mPatchVersion = PreferencesUtils.getPatchVersion(getContext());

        int gridLayoutColumnCount;
        if (getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            gridLayoutColumnCount = 2;
        } else {
            gridLayoutColumnCount = 1;
        }

        mFromRecycler.setLayoutManager(new GridLayoutManager(getContext(), gridLayoutColumnCount));
        mFromRecycler.setHasFixedSize(true);
        mFromRecycler.setItemAnimator(new DefaultItemAnimator());
        mItemFromAdapter = new ItemAdapter(getContext(), new ArrayList<ItemEntry>(), ItemDetailFragment.this, mPatchVersion);
        mFromRecycler.setAdapter(mItemFromAdapter);

        mIntoRecycler.setLayoutManager(new GridLayoutManager(getContext(), gridLayoutColumnCount));
        mIntoRecycler.setHasFixedSize(true);
        mIntoRecycler.setItemAnimator(new DefaultItemAnimator());
        mItemIntoAdapter = new ItemAdapter(getContext(), new ArrayList<ItemEntry>(), ItemDetailFragment.this, mPatchVersion);
        mIntoRecycler.setAdapter(mItemIntoAdapter);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(FROM_LAYOUT_MANAGER_STATE_KEY)) {
                mFromRecycler.getLayoutManager().onRestoreInstanceState(savedInstanceState.getParcelable(FROM_LAYOUT_MANAGER_STATE_KEY));
            }
            if (savedInstanceState.containsKey(INTO_LAYOUT_MANAGER_STATE_KEY)) {
                mIntoRecycler.getLayoutManager().onRestoreInstanceState(savedInstanceState.getParcelable(INTO_LAYOUT_MANAGER_STATE_KEY));
            }
        }

        mFromRecycler.setNestedScrollingEnabled(false);
        mIntoRecycler.setNestedScrollingEnabled(false);

        mDb = AppDatabase.getInstance(getActivity().getApplicationContext());

        getItem();
    }

    private void getItem() {
        Log.d(LOG_TAG, "Getting item");
        ItemViewModelSharedFactory factory = new ItemViewModelSharedFactory(mDb);
        mViewModel = ViewModelProviders.of(getActivity(), factory).get(ItemViewModelShared.class);
        mViewModel.getSelected().observe(this, new Observer<ItemEntry>() {
            @Override
            public void onChanged(@Nullable ItemEntry itemEntry) {
                mViewModel.getSelected().removeObserver(this);
                Log.d(LOG_TAG, "Receiving database update from LiveData");
                updateUi(itemEntry);
            }
        });
    }

    private void updateUi(@Nullable ItemEntry itemEntry) {
        Log.d(LOG_TAG, "updating Ui");
        String image = itemEntry.getImage();

        Picasso.get()
                .load("http://ddragon.leagueoflegends.com/cdn/" + mPatchVersion + "/img/item/" + image)
                .resize(200, 200)
                .error(R.drawable.ic_launcher_background)
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(mImage);

        int baseGold = itemEntry.getBaseGold();
        int totalGold = itemEntry.getTotalGold();
        int sellGold = itemEntry.getSellGold();

        String name = itemEntry.getName();
        String description = itemEntry.getDescription();
        String plainText = itemEntry.getPlainText();

        mItemFromAdapter.clear();
        mItemIntoAdapter.clear();

        DataUtils.setTextWithLabel(baseGold, mBaseGoldTv, mBaseGoldLabel);
        DataUtils.setTextWithLabel(totalGold, mTotalGoldTv, mTotalGoldLabel);
        DataUtils.setTextWithLabel(sellGold, mSellGoldTv, mSellGoldLabel);

        DataUtils.setTextWithLabel(description, mDescriptionTv, mDescriptionLabel);
        DataUtils.setTextWithLabel(plainText, mPlainTextTv, mPlainTextLabel);

        mItemNameTv.setText(name);

        List<String> from = itemEntry.getFrom();
        String[] fromIds = DataUtils.listStringToStringArray(from);
        if (fromIds.length != 0) {
            mFromLabel.setVisibility(View.VISIBLE);
            mViewModel.setItemFrom(fromIds);
            mViewModel.getItemFrom().observe(this, new Observer<List<ItemEntry>>() {
                @Override
                public void onChanged(@Nullable List<ItemEntry> list) {
                    mViewModel.getItemFrom().removeObserver(this);
                    Log.d(LOG_TAG, "Receiving fromItems database update from LiveData");
                    mItemFromAdapter.setData(list);
                }
            });
        } else {
            mFromLabel.setVisibility(View.INVISIBLE);
        }

        List<String> into = itemEntry.getInto();
        String[] intoIds = DataUtils.listStringToStringArray(into);
        if (intoIds.length != 0) {
            mIntoLabel.setVisibility(View.VISIBLE);
            mViewModel.setItemInto(intoIds);
            mViewModel.getItemInto().observe(this, new Observer<List<ItemEntry>>() {
                @Override
                public void onChanged(@Nullable List<ItemEntry> list) {
                    mViewModel.getItemInto().removeObserver(this);
                    Log.d(LOG_TAG, "Receiving fromItems database update from LiveData");
                    mItemIntoAdapter.setData(list);
                }
            });
        } else {
            mIntoLabel.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onItemClick(ItemEntry itemEntry) {
        Log.d(LOG_TAG, "onItemClick");
        mViewModel.select(itemEntry);
        getItem();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(LOG_TAG, "onDetach");
        mFromRecycler.setAdapter(null);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(LOG_TAG, "onSaveInstanceState");

        outState.putParcelable(FROM_LAYOUT_MANAGER_STATE_KEY, mFromRecycler.getLayoutManager().onSaveInstanceState());
        outState.putParcelable(INTO_LAYOUT_MANAGER_STATE_KEY, mIntoRecycler.getLayoutManager().onSaveInstanceState());
    }
}
