package de.everytap.broteinheiten_datenbank.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import de.everytap.broteinheiten_datenbank.R;
import de.everytap.broteinheiten_datenbank.interfaces.OnEditFoodDialogListener;
import de.everytap.broteinheiten_datenbank.model.Food;

/**
 * Created by randombyte on 27.01.2015.
 */
public class EditFoodDialog {

    public static AlertDialog newInstance(final Context context, @Nullable final Food originalFood, final OnEditFoodDialogListener listener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        View rootView = View.inflate(context, R.layout.dialog_edit_food_fragment, null);
        TextView name = ((EditText) rootView.findViewById(R.id.food_name));
        TextView be = ((EditText) rootView.findViewById(R.id.food_be));

        if (originalFood != null) { //originalFood ist null, wenn ein neues Gericht erstellt wird
            builder.setTitle("Gericht bearbeiten");
            name.setText(originalFood.getName());
            be.setText(originalFood.getBe());
        } else {
            builder.setTitle("Neues Gericht");
        }

        builder
                .setView(rootView)
                .setPositiveButton("Speichern", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        listener.onSave(createFoodFromDialog((AlertDialog) dialog, originalFood));
                    }
                })
                .setNeutralButton("Abbrechen", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        listener.onCancel();
                    }
                })
                .setNegativeButton("Löschen", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        listener.onDelete(originalFood != null ? originalFood.getId() : -1); //originalFood ist nur null, wenn ein neues gerade erstellt wird. wenn das gelöscht wird, braucht man keine id, weil es noch gar nicht gespeichert wurde
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        listener.onCancel();
                    }
                });

        final AlertDialog dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {

                final AlertDialog alertDialog = ((AlertDialog) dialog);

                final EditText name = (EditText) alertDialog.findViewById(R.id.food_name);
                final EditText be = (EditText) alertDialog.findViewById(R.id.food_be);

                //Buttons disablen, wenn nichts eingetragen

                TextWatcher textWatcher = new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                        //Ausführen
                        setPositiveAndNegativeButtonEnabled(alertDialog, checkButtonsEnabled(name, be));
                    }
                };

                name.addTextChangedListener(textWatcher);
                be.addTextChangedListener(textWatcher);

                //OnTextChanged simulieren
                setPositiveAndNegativeButtonEnabled(alertDialog, checkButtonsEnabled(name, be));
            }
        });

        be.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    dialog.dismiss();
                    listener.onSave(createFoodFromDialog(dialog, originalFood));
                    return true;
                }

                return false;
            }
        });

        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE); //Keyboard geht auf

        return dialog;
    }

    private static void setPositiveAndNegativeButtonEnabled(AlertDialog alertDialog, boolean enabled) {
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(enabled);
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(enabled);
    }

    /**
     * @return Buttons enabled, wenn kein EditText leer
     */
    private static boolean checkButtonsEnabled(EditText... editTexts) {
        for (EditText editText : editTexts) {
            if (editText != null && editText.getText().toString().isEmpty()) { //null ignorieren
                //Mindestens eins ist leer -> raus und return false
                return false;
            }
        }
        //Bis hierhin gekommen -> keins ist leer oder alle null
        return true;
    }

    private static Food createFoodFromDialog(AlertDialog alertDialog, Food originalFood) {

        EditText foodNameEditText = (EditText) alertDialog.findViewById(R.id.food_name);
        EditText foodBeEditText = (EditText) alertDialog.findViewById(R.id.food_be);

        if (foodNameEditText == null || foodBeEditText == null) {
            throw new RuntimeException("Views food_name or food_be is null. Impossible!");
        }

        String foodName = foodNameEditText.getText().toString();
        String foodBe = foodBeEditText.getText().toString();

        return new Food(foodName, foodBe, originalFood == null || originalFood.isUserCreated()); //Wenn originalFood null dann ergebnis null, wenn nicht null, dann zur nächsten bedingung LIKE A BOSS!
    }
}