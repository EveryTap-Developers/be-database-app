package de.everytap.broteinheiten_datenbank.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;

import de.everytap.broteinheiten_datenbank.R;
import de.everytap.broteinheiten_datenbank.adapter.BeAdapter;
import de.everytap.broteinheiten_datenbank.interfaces.OnFoodItemClickListener;
import de.everytap.broteinheiten_datenbank.interfaces.UpdateBundleListener;
import de.everytap.broteinheiten_datenbank.interfaces.ViewPagerViewReferenceReceiveable;
import de.everytap.broteinheiten_datenbank.model.Food;
import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;

/**
 * Created by Admin on 04.04.2015.
 */
public class FoodListFragment extends RoboFragment implements UpdateBundleListener {

    public static final String ARG_FOOD_LIST = "arg_food_list";
    public static final String ARG_ID = "arg_id"; //id for sending back recyclerView for FAB

    private @InjectView(R.id.recycler_view) RecyclerView recyclerView;
    private ArrayList<Food> foodList;
    private int id = -1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_food_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            unpackBundle(getArguments());
        }
        buildUi();

        //notify main for FAB RecyclerView reference
        Fragment parentFragment = getParentFragment();
        if (parentFragment != null) {
            ViewPagerViewReferenceReceiveable receiver = (ViewPagerViewReferenceReceiveable) parentFragment;
            receiver.sendView(recyclerView, id);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        buildUi();
    }

    private void buildUi() {

        if (recyclerView.getAdapter() == null) {
            OnFoodItemClickListener foodItemClickListener;
            if (getParentFragment()!= null) {
                foodItemClickListener = (OnFoodItemClickListener) getParentFragment();
            } else {
                Log.w("FoodListFragment", "ParentFragment is null! No callback available for click on element.");
                foodItemClickListener = new OnFoodItemClickListener() {
                    @Override
                    public void onFoodItemClick(int id) {
                        Toast.makeText(getActivity(), "Id: " + id, Toast.LENGTH_SHORT).show();
                    }
                };
            }

            BeAdapter beAdapter = new BeAdapter(foodList, foodItemClickListener);

            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(beAdapter);
        } else {
            BeAdapter beAdapter = (BeAdapter) recyclerView.getAdapter();
            beAdapter.setFoodList(foodList, true);
        }
    }

    private void unpackBundle(@NonNull Bundle bundle) {
        foodList = bundle.getParcelableArrayList(ARG_FOOD_LIST);
        id = bundle.getInt(ARG_ID);
    }

    @Override
    public void update(@NonNull Bundle bundle) {
        //todo: gucken, was sich verändert hat -> notify auf RecyclerView
        unpackBundle(bundle);
        buildUi();

    }
}
