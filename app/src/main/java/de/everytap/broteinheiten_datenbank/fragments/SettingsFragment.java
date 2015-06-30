package de.everytap.broteinheiten_datenbank.fragments;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import java.sql.SQLException;

import de.everytap.broteinheiten_datenbank.R;
import de.everytap.broteinheiten_datenbank.Utils.Utils;
import de.everytap.broteinheiten_datenbank.database.db.BeDataSource;

/**
 * Created by randombyte on 19.12.2014.
 */
public class SettingsFragment extends PreferenceFragment {

    public static final String DELETE_DATABASE_KEY = "delete_database";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        addPreferencesFromResource(R.xml.preferences);

        Preference deleteDatabase = findPreference(DELETE_DATABASE_KEY);
        if (deleteDatabase != null) deleteDatabase.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                //Alles löschen
                BeDataSource dataSource = new BeDataSource(getActivity());
                try {
                    dataSource.open();
                } catch (SQLException e) {
                    e.printStackTrace();
                    //todo: fehlerbehandlung
                    return true;
                }
                dataSource.deleteEverything();
                dataSource.close();

                //Erfolgsnachricht
                Toast.makeText(getActivity(), "Lokale Datenbank gelöscht", Toast.LENGTH_SHORT).show();

                return true;
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.settings_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_credits:
                AlertDialog creditsDialog = Utils.makeOkDialog(getActivity(), getResources().getString(R.string.credits));
                creditsDialog.setTitle("Credits");
                creditsDialog.show();

                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
