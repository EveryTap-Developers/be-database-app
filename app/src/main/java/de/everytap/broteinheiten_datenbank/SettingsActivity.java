package de.everytap.broteinheiten_datenbank;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import de.everytap.broteinheiten_datenbank.fragments.SettingsFragment;
import roboguice.activity.RoboActionBarActivity;
import roboguice.inject.InjectView;

/**
 * Created by randombyte on 20.12.2014.
 */
public class SettingsActivity extends RoboActionBarActivity{

    @InjectView(R.id.toolbar) Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.primary_color_dark));
        }*/

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getFragmentManager().beginTransaction().replace(R.id.settings_fragment_space, new SettingsFragment()).commit();
    }
}
