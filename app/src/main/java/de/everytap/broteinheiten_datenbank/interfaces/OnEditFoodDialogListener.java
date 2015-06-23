package de.everytap.broteinheiten_datenbank.interfaces;

import de.everytap.broteinheiten_datenbank.model.Food;

/**
 * Created by randombyte on 27.01.2015.
 */
public interface OnEditFoodDialogListener {

    void onSave(Food food);

    void onCancel();

    void onDelete(int id);

}
