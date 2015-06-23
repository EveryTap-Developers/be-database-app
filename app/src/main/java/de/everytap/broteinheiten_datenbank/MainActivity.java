package de.everytap.broteinheiten_datenbank;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.quinny898.library.persistentsearch.SearchBox;

import de.everytap.broteinheiten_datenbank.fragments.DatabaseFragmentSliding;

/**
 * Created by randombyte on 18.12.2014.
 */

public class MainActivity extends RoboMaterialNavigationDrawer implements SearchBox.MenuListener {

    @Override
    public void init(Bundle savedInstance) {

        getWindow().setBackgroundDrawable(null);

        //Dinge, die im NavigationDrawer sein werden
        //final DatabaseFragment databaseFragment = new DatabaseFragment();
        final DatabaseFragmentSliding databaseFragmentSliding = new DatabaseFragmentSliding();
        Intent settingsIntent = new Intent(this, SettingsActivity.class);

        /*Toolbar*/
        Toolbar toolbar = getToolbar();

        //Spinner/Filter
        /*Spinner spinner = new Spinner(this);
        ArrayAdapter adapter = ArrayAdapter.createFromResource(getSupportActionBar().getThemedContext(), R.array.spinner_filter_entries, R.layout.toolbar_spinner_filter_item); //Item Text weiß
        adapter.setDropDownViewResource(R.layout.toolbar_spinner_filter_item_drop_down); //Item Text schwarz
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((OnCategoryChangedListener) databaseFragment).onChanged(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //egal
            }
        });

        toolbar.addView(spinner);*/

        setSupportActionBar(toolbar);
        getSupportActionBar().hide();

        disableLearningPattern();
        addSection(newSection("Datenbank", R.drawable.ic_launcher, databaseFragmentSliding));
        addBottomSection(newSection("Einstellungen", R.drawable.ic_action_settings, settingsIntent));
        setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                //Get Fragment by containerId
                ((DrawerLayout.DrawerListener) getSupportFragmentManager().findFragmentById(it.neokree.materialnavigationdrawer.R.id.frame_container)).onDrawerSlide(drawerView, slideOffset);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                ((DrawerLayout.DrawerListener) getSupportFragmentManager().findFragmentById(it.neokree.materialnavigationdrawer.R.id.frame_container)).onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                ((DrawerLayout.DrawerListener) getSupportFragmentManager().findFragmentById(it.neokree.materialnavigationdrawer.R.id.frame_container)).onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                ((DrawerLayout.DrawerListener) getSupportFragmentManager().findFragmentById(it.neokree.materialnavigationdrawer.R.id.frame_container)).onDrawerStateChanged(newState);
            }
        });
    }

    @Override
    public void onMenuClick() {
        this.openDrawer();
    }
}
