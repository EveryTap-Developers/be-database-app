package de.everytap.broteinheiten_datenbank.Utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * Created by randombyte on 06.01.2015.
 */
public class Utils {

    public static void makeOkDialog(String text, Context context) {
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setMessage(text)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alert.create().show();
    }

}
