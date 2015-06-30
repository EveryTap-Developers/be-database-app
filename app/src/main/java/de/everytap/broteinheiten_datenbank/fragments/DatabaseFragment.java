package de.everytap.broteinheiten_datenbank.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.melnykov.fab.FloatingActionButton;

import java.sql.SQLException;
import java.util.List;

import de.everytap.broteinheiten_datenbank.R;
import de.everytap.broteinheiten_datenbank.Utils.Utils;
import de.everytap.broteinheiten_datenbank.adapter.BeAdapter;
import de.everytap.broteinheiten_datenbank.interfaces.ImplMangerWorkEventListener;
import de.everytap.broteinheiten_datenbank.interfaces.ManagerWorkUiListener;
import de.everytap.broteinheiten_datenbank.interfaces.OnCategoryChangedListener;
import de.everytap.broteinheiten_datenbank.interfaces.OnEditFoodDialogListener;
import de.everytap.broteinheiten_datenbank.interfaces.OnFoodItemClickListener;
import de.everytap.broteinheiten_datenbank.database.db.BeDataSource;
import de.everytap.broteinheiten_datenbank.database.db.DatabaseManager;
import de.everytap.broteinheiten_datenbank.dialogs.EditFoodDialog;
import de.everytap.broteinheiten_datenbank.model.Food;
import roboguice.fragment.provided.RoboFragment;
import roboguice.inject.InjectView;

/**
 * Created by randombyte on 18.12.2014.
 */
public class DatabaseFragment extends RoboFragment implements DrawerLayout.DrawerListener, OnCategoryChangedListener {

    public static final String ARG_QUERY = "arg_query";

    @InjectView(R.id.recycler_view) RecyclerView recyclerView;
    @InjectView(R.id.search_view) SearchView searchView;
    @InjectView(R.id.fab) FloatingActionButton fab;

    private static final String URL_TO_DATABASE_JSON = "http://everytap.de/datenbank.txt";

    private BeDataSource dataSource;

    private boolean onlyUserCreatedItems = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dataSource = new BeDataSource(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_database, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //todo: recyclerView.setHasFixedSize(true);

        //LayoutManager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        /*todo: SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));*/
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                databaseWork(newText, onlyUserCreatedItems);

                return true;
            }
        });
        searchView.setIconifiedByDefault(false);
        searchView.clearFocus(); //Fokus weg, damit Tastatur ausgeblendet wird

        fab.attachToRecyclerView(recyclerView);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditFoodDialog.newInstance(getActivity(), null, new OnEditFoodDialogListener() {
                    @Override
                    public void onSave(Food food) {
                        int idInsertedTo = ((int) dataSource.addData(food)); //In Db
                        List<Food> foodList = dataSource.getData(null, false);

                        int posOfNewElement = foodList.indexOf(new Food(idInsertedTo));

                        recyclerView.scrollToPosition(posOfNewElement);

                        refreshAdapter(foodList, false)
                                .notifyItemInserted(posOfNewElement); //refreshen, selber refreshen
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
                        .show();
            }
        });
    }

    /**
     *
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

    @Override
    public void onResume() {
        super.onResume();

        if (openDb()) { //Daten anzeigen
            databaseWork(getArguments() == null ? "" : getArguments().getString(ARG_QUERY), onlyUserCreatedItems); //Like a boss!
        } else {
            Utils.makeOkDialog(getActivity(), "Konnte Datenbank nicht öffnen!");
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        dataSource.close();
    }

    private void databaseWork(String querySearchString, final boolean onlyUserCreatedItems) {

        //Datenbank offen?
        if (!dataSource.isDbOpen()) {
            //Orientation Change
            Log.i("Database", "Reopen Database");
            if (!openDb()) { //Wieder öffnen, sollte nur einmal ausgeführt werden
                //Nicht erfolgreich
                Utils.makeOkDialog(getActivity(), "Konnte Datenbank nicht öffnen!");
                return;
            }
        }

        //Database schon runtergeladen?
        List<Food> foodList = dataSource.getData(null, false);
        if (foodList.size() == 0) {

            refreshAdapter(foodList, true); //Nichts anzeigen

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
                            databaseWork(null, onlyUserCreatedItems); //Alles neu laden aus Datenbank
                        }
                    });
                }
            });

            DatabaseManager.downloadDatabaseAsync(getActivity(), URL_TO_DATABASE_JSON, implMangerWorkEventListener);

        } else {

            //Die eigentliche Arbeit:
            foodList = dataSource.getData(querySearchString, onlyUserCreatedItems);

            //Daten anzeigen
            refreshAdapter(foodList, true);
        }
    }

    /**
     * @return Aktueller Adapter
     */
    private BeAdapter refreshAdapter(List<Food> foodList, boolean notifyDataSetChanged) {
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        if (adapter != null && adapter instanceof BeAdapter) {
            //Adapter da
            ((BeAdapter) adapter).setFoodList(foodList, notifyDataSetChanged);
        } else {
            //Kein Adapter/Nicht der richtige Adapter
            recyclerView.setAdapter(new BeAdapter(foodList, new OnFoodItemClickListener() {
                @Override
                public void onFoodItemClick(final int id) {
                    //OnFoodItemClickListener
                    Food foodItemClicked = dataSource.getById(id);
                    if (foodItemClicked.isUserCreated()) {
                        EditFoodDialog.newInstance(getActivity(), foodItemClicked, new OnEditFoodDialogListener() {
                            @Override
                            public void onSave(Food food) {
                                food.setId(id); //Die id des gerade bearbeiteten Elements
                                dataSource.updateDataById(food);
                                BeAdapter adapter1 = refreshAdapter(dataSource.getData(null, false), false); //Selber refreshen

                                adapter1.notifyItemChanged(adapter1.getFoodList().indexOf(food));

                                //Toast.makeText(getActivity(), food.toString(), Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onCancel() {
                                //.makeText(getActivity(), "Cancel", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onDelete(int id) {
                                //Toast.makeText(getActivity(), "Delete", Toast.LENGTH_SHORT).show();

                                BeAdapter adapter = ((BeAdapter) recyclerView.getAdapter());
                                int pos = adapter.getFoodList().indexOf(new Food(id)); //Nach id suchen

                                dataSource.deleteDataByID(id);
                                refreshAdapter(dataSource.getData(null, false), false); //selber refreshen
                                adapter.notifyItemRemoved(pos);
                            }
                        })
                                .show();
                    }
                }
            }));
        }
        return (BeAdapter) recyclerView.getAdapter();
    }

    @Override
    public void onChanged(int positionOfCategorieItemInSpinner) {
        //0 => Alle; 1 => Eigene

        boolean oldOnlyUserCreatedItems = onlyUserCreatedItems;

        switch (positionOfCategorieItemInSpinner) {
            case 0:
                //Alle
                    onlyUserCreatedItems = false;
                break;

            case 1:
                onlyUserCreatedItems = true;
                break;
        }

        if (oldOnlyUserCreatedItems != onlyUserCreatedItems) {
            //Hat sich verändert, also refreshen
            databaseWork(searchView.getQuery().toString(), onlyUserCreatedItems);
        }
    }

    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {
        //egal
    }

    @Override
    public void onDrawerOpened(View drawerView) {
        if (searchView != null) searchView.clearFocus(); //Kann vor OnViewCreated sein
    }

    @Override
    public void onDrawerClosed(View drawerView) {
        //egal
    }

    @Override
    public void onDrawerStateChanged(int newState) {
        //egal
    }
}
