package de.everytap.broteinheiten_datenbank.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.melnykov.fab.FloatingActionButton;
import com.quinny898.library.persistentsearch.SearchBox;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import de.everytap.broteinheiten_datenbank.R;
import de.everytap.broteinheiten_datenbank.Utils.Utils;
import de.everytap.broteinheiten_datenbank.adapter.CategoryViewPagerAdapter;
import de.everytap.broteinheiten_datenbank.database.db.BeDataSource;
import de.everytap.broteinheiten_datenbank.database.db.DatabaseManager;
import de.everytap.broteinheiten_datenbank.dialogs.EditFoodDialogFragment;
import de.everytap.broteinheiten_datenbank.interfaces.ImplMangerWorkEventListener;
import de.everytap.broteinheiten_datenbank.interfaces.ManagerWorkUiListener;
import de.everytap.broteinheiten_datenbank.interfaces.OnEditFoodDialogListener;
import de.everytap.broteinheiten_datenbank.interfaces.OnFoodItemClickListener;
import de.everytap.broteinheiten_datenbank.interfaces.ViewPagerViewReferenceReceivable;
import de.everytap.broteinheiten_datenbank.model.Food;
import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;

public class DatabaseFragmentSliding extends RoboFragment implements DrawerLayout.DrawerListener, ViewPagerViewReferenceReceivable, OnFoodItemClickListener, OnEditFoodDialogListener {

    private static final String URL_TO_DATABASE_JSON = "http://everytap.de/datenbank.txt";

    private @InjectView(R.id.tabs) PagerSlidingTabStrip tabStrip;
    private @InjectView(R.id.view_pager) ViewPager viewPager;
    private @InjectView(R.id.fab) FloatingActionButton fab;
    private @InjectView(R.id.search_view) SearchBox searchBox;

    private BeDataSource dataSource;
    private boolean tabsConnectedToViewPager = false;

    private List<RecyclerView> foodListRecyclerViews = new ArrayList<RecyclerView>(); //memory leak?

    public long timeCreated = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dataSource = new BeDataSource(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();

        if (openDb()) { //Daten anzeigen
            displayFoods(null);
        } else {
            Utils.makeOkDialog(getActivity(), "Konnte Datenbank nicht öffnen!");
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        dataSource.close();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sliding_food_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        searchBox.enableVoiceRecognition(this);
        if (getActivity() instanceof SearchBox.MenuListener) {
            searchBox.setMenuListener((SearchBox.MenuListener) getActivity());
        }
        searchBox.setLogoText("BE-Datenbank");
        searchBox.setSearchListener(new SearchBox.SearchListener() {
            @Override
            public void onSearchOpened() {

            }

            @Override
            public void onSearchCleared() {

            }

            @Override
            public void onSearchClosed() {

            }

            @Override
            public void onSearchTermChanged() {
                displayFoods(dataSource.getData(searchBox.getSearchText(), false));
            }

            @Override
            public void onSearch(String s) {

            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showEditFoodDialog(null);
                /*EditFoodDialog.newInstance(getActivity(), null, new OnEditFoodDialogListener() {
                    @Override
                    public void onSave(Food food) {
                        int idInsertedTo = ((int) dataSource.addData(food)); //In Db
                        List<Food> foodList = dataSource.getData(null, false);

                        int posOfNewElement = foodList.indexOf(new Food(idInsertedTo));

                        recyclerViewScrollToPos(posOfNewElement);

                        displayFoods(); //refreshen, ohne notify, könnte in FoodListFragment gemacht werden
                    }

                    @Override
                    public void onCancel() {
                        //Nicht speichern
                    }

                    @Override
                    public void onDelete(int id) {
                        //id wird immer -1 sein
                        //Noch nichts gespeichert -> muss nichts gelöscht werden
                    }
                })
                        .show();*/
            }
        });

        tabStrip.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                fabAttachRecyclerView();
            }
        });
    }

    /**
     * @return true if successful
     */
    private boolean openDb() {
        try {
            dataSource.open();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            //todo: fehlerbehandlung
            Log.e("Database", "Database open exception!");
            return false;
        }
    }

    private @Nullable ArrayList<Food> getAllFoods() {

        //Datenbank offen?
        if (!dataSource.isDbOpen()) {
            Log.e("Database", "Database is closed");
            return null;
        }

        return dataSource.getData(null, false);
    }

    private void displayFoods(@Nullable ArrayList<Food> foodList) {

        if (foodList == null) {
            //getAllFoods
            foodList = getAllFoods();
            if (foodList == null) {
                //Nach getAllFoods null
                Toast.makeText(getActivity(), "Error open db", Toast.LENGTH_LONG).show();
                return;
            } else if (foodList.size() == 0) {
                //getAllFoods 0 lang
                updateDb();
                return;
            }
        }

        if (viewPager.getAdapter() == null) {
            viewPager.setAdapter(new CategoryViewPagerAdapter(getChildFragmentManager(), foodList, this));
        } else {
            CategoryViewPagerAdapter categoryViewPagerAdapter = (CategoryViewPagerAdapter) viewPager.getAdapter();
            categoryViewPagerAdapter.setFoodList(foodList, viewPager.getId());
        }

        if (!tabsConnectedToViewPager) {
            tabStrip.setViewPager(viewPager);
            tabsConnectedToViewPager = true;
        }
    }

    private void recyclerViewScrollToPos(int pos) {
        int currentItemPos = viewPager.getCurrentItem();
        if ((foodListRecyclerViews.size() > currentItemPos) && foodListRecyclerViews.get(currentItemPos) != null) {
            foodListRecyclerViews.get(currentItemPos).scrollToPosition(pos);
        }
    }

    private void fabAttachRecyclerView() {
        fab.show(); //Show FAB to give the possibility to click on "add"
        int currentItemPos = viewPager.getCurrentItem();
        if ((foodListRecyclerViews.size() > currentItemPos) && foodListRecyclerViews.get(currentItemPos) != null) {
            fab.attachToRecyclerView(foodListRecyclerViews.get(currentItemPos));

        } else {
            Log.w("FAB", "Attaching RecyclerView to FAB failed because FoodListFragment hasn't sent its RecyclerView yet! Pos: " + currentItemPos);
        }
    }

    private void updateDb() {

        //Datenbank herunterladen
        final ProgressDialog progressDialog = ProgressDialog.show(getActivity(), "Bitte warten...", "Lade Datenbank herunter...", false, false);
        progressDialog.setMax(100);

        ImplMangerWorkEventListener implMangerWorkEventListener = new ImplMangerWorkEventListener(new ManagerWorkUiListener() {
            @Override
            public void onTextForProgressDialog(final String text) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.setMessage(text);
                    }
                });
            }

            @Override
            public void onProgressUpdate(final int progress) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (progress == -1) {
                            progressDialog.setIndeterminate(true);
                        } else {
                            progressDialog.setIndeterminate(false);
                            progressDialog.setProgress(progress);
                        }
                    }
                });
            }

            @Override
            public void onTextForSimpleAlertDialog(final String text) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                        alertDialog.setMessage(text)
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        progressDialog.dismiss();
                        alertDialog.create().show();
                    }
                });
            }

            @Override
            public void progressDialogDismiss() { //Finished
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        if (getActivity() != null) {
                            displayFoods(null); //Alles neu laden aus Datenbank laden
                        }
                    }
                });
            }
        });

        DatabaseManager.downloadDatabaseAsync(getActivity(), URL_TO_DATABASE_JSON, implMangerWorkEventListener);
    }

    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {

    }

    @Override
    public void onDrawerOpened(View drawerView) {
        ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(searchBox.getWindowToken(), 0);
    }

    @Override
    public void onDrawerClosed(View drawerView) {

    }

    @Override
    public void onDrawerStateChanged(int newState) {

    }

    @Override
    public void sendView(View view, int idSender) {
        if ((foodListRecyclerViews.size() > idSender) && foodListRecyclerViews.get(idSender) != null) foodListRecyclerViews.remove(idSender);
        foodListRecyclerViews.add(idSender, (RecyclerView) view);
        fabAttachRecyclerView();
    }

    @Override
    public void onFoodItemClick(final int id) { //Call coming from RecyclerView

        Food foodItemClicked = dataSource.getById(id);
        if (foodItemClicked.isUserCreated()) {
            showEditFoodDialog(foodItemClicked);
        }
    }

    private void showEditFoodDialog(@Nullable Food food) {

        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        Fragment previousFragment = getChildFragmentManager().findFragmentByTag(EditFoodDialogFragment.TAG);
        if (previousFragment != null) {
            fragmentTransaction.remove(previousFragment);
        }
        fragmentTransaction.addToBackStack(null);

        EditFoodDialogFragment editFoodDialogFragment = EditFoodDialogFragment.newInstance(food);
        editFoodDialogFragment.show(fragmentTransaction, EditFoodDialogFragment.TAG);
    }

    @Override
    public void onSave(Food food) {

        if(food.getId() == -1) {
            //Hasn't been added yet
            food.setId(((int) dataSource.addData(food))); //In Db
        } else {
            //Edited
            dataSource.updateDataById(food);
        }

        ArrayList<Food> foodList = getAllFoods();
        if (foodList == null) {
            //todo: Fehlerbehandlung
            return;
        }

        displayFoods(foodList); //todo: spezifisches notfiy könnte in FoodListFragment gemacht werden
        recyclerViewScrollToPos(foodList.indexOf(food));
    }

    @Override
    public void onCancel() {
        //Nicht speichern
    }

    @Override
    public void onDelete(int id) {
        //Noch nichts gespeichert -> muss nichts gelöscht werden
        if (id == -1) return;

        dataSource.deleteDataByID(id);
        displayFoods(null); //todo: spezifisches notfiy köönte in FoodListFragment gemacht werden
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (isAdded() && requestCode == SearchBox.VOICE_RECOGNITION_CODE && resultCode == Activity.RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            searchBox.populateEditText(matches);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
