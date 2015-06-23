package de.everytap.broteinheiten_datenbank.database.db;

import android.content.Context;
import android.support.annotation.Nullable;

import com.google.gson.JsonArray;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import de.everytap.broteinheiten_datenbank.database.JsonDownloader;
import de.everytap.broteinheiten_datenbank.database.JsonParser;
import de.everytap.broteinheiten_datenbank.database.UmlauteChanger;
import de.everytap.broteinheiten_datenbank.model.Food;

/**
 * Created by randombyte on 20.12.2014.
 */
public class DatabaseManager {

    public interface ManagerWorkEventListener {
        public void onStartDownload();
        public void onDownloadFinished(JsonArray jsonArray);
        public void onDownloadError(@Nullable Exception e);
        public void onJsonParsedToList(List<Food> foodList);
        public void onJsonParseError();
        public void onDatabaseUpdated();
        public void onDatabaseError(Exception e);
    }

    //All-Inclusive Methode
    public static void downloadDatabaseAsync(final Context context, final String url, final ManagerWorkEventListener managerWorkEventListener) {

        //ASYNC

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                //DOWNLOAD
                managerWorkEventListener.onStartDownload();

                JsonArray result = null;
                try {
                    result = JsonDownloader.downloadBlocking(context, url);
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }

                if (result == null) {
                    managerWorkEventListener.onDownloadError(null);
                    return;
                }

                managerWorkEventListener.onDownloadFinished(result);
                List<Food> foodList = JsonParser.parseJson(result);

                if (foodList == null) {
                    managerWorkEventListener.onJsonParseError();
                    return;
                }

                //PARSE
                managerWorkEventListener.onJsonParsedToList(foodList);

                BeDataSource beDataSource = new BeDataSource(context);

                try {
                    beDataSource.open();
                } catch (SQLException e2) {
                    managerWorkEventListener.onDatabaseError(e2);
                    return;
                }

                //UMLAUTE
                UmlauteChanger.change(foodList); //&ouml; -> ö

                //DATENBANK
                beDataSource.addData(foodList);
                beDataSource.close();

                managerWorkEventListener.onDatabaseUpdated();
            }


        });

        thread.start();
    }

}
