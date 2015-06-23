package de.everytap.broteinheiten_datenbank.interfaces;

/**
 * Created by randombyte on 04.01.2015.
 */

public interface ManagerWorkUiListener {

    public void onTextForProgressDialog(String text);

    /**
     *
     * @param progress -1 when intermediate, 100 when finished
     */
    public void onProgressUpdate(int progress);

    public void onTextForSimpleAlertDialog(String text);

    public void progressDialogDismiss();

}
