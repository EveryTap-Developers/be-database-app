package de.everytap.broteinheiten_datenbank.database;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonArray;
import com.koushikdutta.ion.Ion;

import java.util.concurrent.ExecutionException;

/**
 * Created by randombyte on 19.12.2014.
 */
public class JsonDownloader {

    public static JsonArray downloadBlocking(Context context, String url) throws ExecutionException, InterruptedException {
        //BLOCKING
        return Ion.with(context)
                .load(url)
                .noCache()
                .setLogging("Database Download", Log.DEBUG)
                .asJsonArray()
                .get();
    }
}
