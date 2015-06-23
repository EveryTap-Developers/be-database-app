package de.everytap.broteinheiten_datenbank.interfaces;

import android.support.annotation.Nullable;

import com.google.gson.JsonArray;

import java.util.List;

import de.everytap.broteinheiten_datenbank.database.db.DatabaseManager;
import de.everytap.broteinheiten_datenbank.model.Food;

/**
 * Created by randombyte on 04.01.2015.
 */

public class ImplMangerWorkEventListener implements DatabaseManager.ManagerWorkEventListener{

    private final ManagerWorkUiListener managerWorkUiListener;
    private int progress = -1;

    public ImplMangerWorkEventListener(ManagerWorkUiListener managerWorkUiListener) {
        this.managerWorkUiListener = managerWorkUiListener;
    }

    @Override
    public void onStartDownload() {
        managerWorkUiListener.onProgressUpdate(-1);
    }

    @Override
    public void onDownloadFinished(JsonArray jsonArray) {
        managerWorkUiListener.onTextForProgressDialog("Lese Daten...");
        managerWorkUiListener.onProgressUpdate(-1);
        this.progress = -1;
    }

    @Override
    public void onDownloadError(@Nullable Exception e) {
        managerWorkUiListener.onTextForSimpleAlertDialog("Fehler beim Herunterladen der Datenbank!");
    }

    @Override
    public void onJsonParsedToList(List<Food> foodList) {
        managerWorkUiListener.onTextForProgressDialog("Datenbank aktualisieren...");
    }

    @Override
    public void onJsonParseError() {
        managerWorkUiListener.onTextForSimpleAlertDialog("Fehler beim Parsen der externen Datenbank!");
    }

    @Override
    public void onDatabaseUpdated() {
        managerWorkUiListener.progressDialogDismiss();
    }

    @Override
    public void onDatabaseError(Exception e) {
        managerWorkUiListener.onTextForSimpleAlertDialog("Fehler beim Aktualisieren der Datenbank!");
    }

}
