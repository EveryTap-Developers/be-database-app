package de.everytap.broteinheiten_datenbank.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

import de.everytap.broteinheiten_datenbank.fragments.FoodListFragment;
import de.everytap.broteinheiten_datenbank.interfaces.OnFoodItemClickListener;
import de.everytap.broteinheiten_datenbank.interfaces.UpdateBundleListener;
import de.everytap.broteinheiten_datenbank.model.Food;

/**
 * Created by Admin on 04.04.2015.
 */
public class CategoryViewPagerAdapter extends FragmentPagerAdapter implements OnFoodItemClickListener{

    public static final boolean[] onlyUserCreatedPages = new boolean[] {false, true};
    public static final String[] titles = {"Alle", "Eigene"};

    private final FragmentManager fragmentManager;
    private ArrayList<Food> foodList;
    private final OnFoodItemClickListener foodItemClickListener;

    public CategoryViewPagerAdapter(FragmentManager fm, ArrayList<Food> foodList, OnFoodItemClickListener foodItemClickListener) {
        super(fm);
        this.fragmentManager = fm;
        this.foodList = foodList;
        this.foodItemClickListener = foodItemClickListener;
    }

    public void setFoodList(ArrayList<Food> foodList, int viewPagerId) {
        this.foodList = foodList;

        //"notify all"
        for (int i = 0; i < getCount(); i++) {
            ((UpdateBundleListener) fragmentManager.findFragmentByTag(makeFragmentName(viewPagerId, i)))
                    .update(
                            buildBundle(
                                    getDataForPos(i),
                                    i
                            )
                    );
        }
    }

    @Override
    public Fragment getItem(int position) {

        FoodListFragment fragment = new FoodListFragment();
        ArrayList<Food> foodListFiltered = getDataForPos(position);
        Bundle bundle = buildBundle(foodListFiltered, position);
        fragment.setArguments(bundle);

        return fragment;
    }

    private Bundle buildBundle(ArrayList<Food> foodList, int id) {
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(FoodListFragment.ARG_FOOD_LIST, foodList);
        bundle.putInt(FoodListFragment.ARG_ID, id);
        return bundle;
    }

    private ArrayList<Food> getDataForPos(int position) {
        return Food.filterOnlyUserCreated(foodList, onlyUserCreatedPages[position]);
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }

    @Override
    public void onFoodItemClick(int id) {
        foodItemClickListener.onFoodItemClick(id);
    }

    //Extracted from FragmentPagerAdapter source
    private static String makeFragmentName(int containerViewId, int pos) {
        return "android:switcher:" + containerViewId + ":" + pos;
    }
}
