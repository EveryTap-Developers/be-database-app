package de.everytap.broteinheiten_datenbank;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import com.quinny898.library.persistentsearch.SearchBox;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import de.everytap.broteinheiten_datenbank.adapter.BeAdapter;
import de.everytap.broteinheiten_datenbank.database.db.BeDataSource;
import de.everytap.broteinheiten_datenbank.database.db.DatabaseManager;
import de.everytap.broteinheiten_datenbank.dialogs.EditFoodDialogFragment;
import de.everytap.broteinheiten_datenbank.interfaces.ImplMangerWorkEventListener;
import de.everytap.broteinheiten_datenbank.interfaces.ManagerWorkUiListener;
import de.everytap.broteinheiten_datenbank.interfaces.OnFoodItemClickListener;
import de.everytap.broteinheiten_datenbank.model.Food;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_new_main)
public class NewMainActivity extends RoboAppCompatActivity implements OnFoodItemClickListener, SearchBox.SearchListener, PopupMenu.OnMenuItemClickListener {

    private static final String URL_TO_DATABASE_JSON = "http://everytap.de/datenbank.txt";

    @InjectView(R.id.search_view) SearchBox searchBox;
    @InjectView(R.id.recycler_view) RecyclerView recyclerView;

    BeDataSource source;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        source = new BeDataSource(this);

        searchBox.enableVoiceRecognition(this);
        searchBox.setLogoText("Be-Datenbank");
        searchBox.setMenuVisibility(View.INVISIBLE);
        searchBox.setSearchListener(this);
        searchBox.setOverflowMenu(R.menu.main_activity_menu);
        searchBox.setOverflowMenuItemClickListener(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            source.open();
        } catch (SQLException e) {
            e.printStackTrace();

            buildTextAlertDialog("Konnte Datenbank nicht öffnen!").show();
        }

        displayFoods(false);
    }

    @Override
    protected void onPause() {
        super.onPause();

        source.close();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SearchBox.VOICE_RECOGNITION_CODE && resultCode == Activity.RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            searchBox.populateEditText(matches);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
        }

        return false;
    }

    /**
     * @param calledByUpdater true if method is called by db downloader callback
     */
    private void displayFoods(boolean calledByUpdater) {

        if (source != null && source.isDbOpen()) {
            String searchTerm = searchBox.getSearchText();
            ArrayList<Food> foodList = source.getData(searchTerm, false);
            if (foodList == null) {
                buildTextAlertDialog("Konnte Daten nicht lesen!").show();
                return;
            }
            if (searchTerm.isEmpty() && foodList.size() == 0) {
                if (calledByUpdater) {
                    buildTextAlertDialog("Datenbank heruntergeladen, konnte aber nicht gelesen werden!").show();
                    return;
                }
                //Not downloaded yet(or someone deleted the db entries from outside :D)
                updateDb();
                return;
            }

            updateRecyclerViewData(foodList);
        }
    }

    private void updateRecyclerViewData(List<Food> foods) {
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        if (adapter == null) {
            BeAdapter newAdapter = new BeAdapter(foods, this);
            recyclerView.setAdapter(newAdapter);
        } else {
            ((BeAdapter) adapter).setFoodList(foods, true);
        }
    }

    private void updateDb() {

        //download database
        final ProgressDialog progressDialog = ProgressDialog.show(this, "Bitte warten...", "Lade Datenbank herunter...", false, false);
        progressDialog.setMax(100);

        ImplMangerWorkEventListener implMangerWorkEventListener = new ImplMangerWorkEventListener(new ManagerWorkUiListener() {
            @Override
            public void onTextForProgressDialog(final String text) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.setMessage(text);
                    }
                });
            }

            @Override
            public void onProgressUpdate(final int progress) {
                runOnUiThread(new Runnable() {
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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog alertDialog = buildTextAlertDialog(text);
                        progressDialog.dismiss();
                        alertDialog.show();
                    }
                });
            }

            @Override
            public void progressDialogDismiss() { //Finished
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        displayFoods(false);
                    }
                });
            }
        });

        DatabaseManager.downloadDatabaseAsync(this, URL_TO_DATABASE_JSON, implMangerWorkEventListener);
    }

    @Override
    public void onFoodItemClick(int id) {
        Food food = source.getById(id);
        if (food.isUserCreated()) {
            showEditFoodDialog(food);
        }
    }

    private void showEditFoodDialog(@Nullable Food food) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        Fragment previousFragment = getSupportFragmentManager().findFragmentByTag(EditFoodDialogFragment.TAG);
        if (previousFragment != null) {
            fragmentTransaction.remove(previousFragment);
        }
        fragmentTransaction.addToBackStack(null);

        EditFoodDialogFragment editFoodDialogFragment = EditFoodDialogFragment.newInstance(food);
        editFoodDialogFragment.show(fragmentTransaction, EditFoodDialogFragment.TAG);
    }

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
        displayFoods(false);
    }

    @Override
    public void onSearch(String s) {

    }

    private AlertDialog buildTextAlertDialog(String text) {
        return new AlertDialog.Builder(this)
                .setMessage(text)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
    }
}