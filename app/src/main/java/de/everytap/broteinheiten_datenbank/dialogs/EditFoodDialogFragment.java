package de.everytap.broteinheiten_datenbank.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import de.everytap.broteinheiten_datenbank.R;
import de.everytap.broteinheiten_datenbank.interfaces.OnEditFoodDialogListener;
import de.everytap.broteinheiten_datenbank.model.Food;
import roboguice.fragment.RoboDialogFragment;

/**
 * Created by Admin on 07.04.2015.
 */

/**
 * ParentFragment has to implement OnEditFoodDialogListener
 */
public class EditFoodDialogFragment extends RoboDialogFragment {

    public static final String TAG = "EditFoodDialogFragment";

    public static final String ARG_FOOD = "arg_food";

    private Food originalFood;

    public static EditFoodDialogFragment newInstance(@Nullable Food food) {

        EditFoodDialogFragment editFoodDialogFragment = new EditFoodDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARG_FOOD, food);
        editFoodDialogFragment.setArguments(bundle);

        return editFoodDialogFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            unpackBundle(getArguments());
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View rootView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_edit_food_fragment, null, false);
        EditText foodName = (EditText) rootView.findViewById(R.id.food_name);
        EditText foodBe = (EditText) rootView.findViewById(R.id.food_be);

        if (originalFood != null) { //originalFood may be null when creating new Food
            builder.setTitle("Gericht bearbeiten");
            foodName.setText(originalFood.getName());
            foodBe.setText(Float.toString(originalFood.getBe()));
        } else {
            builder.setTitle("Neues Gericht");
        }


        builder.setView(rootView);

        builder
                .setPositiveButton("Speichern", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ((OnEditFoodDialogListener) getParentFragment()).onSave(createFoodFromDialog((AlertDialog) dialog, originalFood));
                    }
                })
                .setNeutralButton("Abbrechen", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ((OnEditFoodDialogListener) getParentFragment()).onCancel();
                    }
                })
                .setNegativeButton("Löschen", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ((OnEditFoodDialogListener) getParentFragment()).onDelete(originalFood != null ? originalFood.getId() : -1); //originalFood ist nur null, wenn ein neues gerade erstellt wird. wenn das gel�scht wird, braucht man keine id, weil es noch gar nicht gespeichert wurde
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        ((OnEditFoodDialogListener) getParentFragment()).onCancel();
                    }
                });

        final AlertDialog alertDialog = builder.create();

        //At time when Dialog is shown to get the instance of AlertDialg
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {

                final AlertDialog alertDialogRuntime = (AlertDialog) dialog;

                final EditText foodName = (EditText) alertDialogRuntime.findViewById(R.id.food_name);
                final EditText foodBe = (EditText) alertDialogRuntime.findViewById(R.id.food_be);

                TextWatcher textWatcher = new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        setPositiveAndNegativeButtonEnabled( alertDialogRuntime, checkButtonsEnabled(foodName, foodBe));
                    }
                };

                foodName.addTextChangedListener(textWatcher);
                foodBe.addTextChangedListener(textWatcher);

                //simulate OnTextChanged
                setPositiveAndNegativeButtonEnabled(alertDialogRuntime, checkButtonsEnabled(foodName, foodBe));
            }
        });

        foodBe.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    alertDialog.dismiss();
                    ((OnEditFoodDialogListener) getParentFragment()).onSave(createFoodFromDialog(alertDialog, originalFood));
                    return true;
                }

                return false;
            }
        });

        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE); //Keyboard geht auf

        return alertDialog;
    }

    private static Food createFoodFromDialog(AlertDialog alertDialog, Food originalFood) {

        EditText foodNameEditText = (EditText) alertDialog.findViewById(R.id.food_name);
        EditText foodBeEditText = (EditText) alertDialog.findViewById(R.id.food_be);

        if (foodNameEditText == null || foodBeEditText == null) {
            throw new RuntimeException("Views food_name or food_be are null. Wrong AlertDialog called with?");
        }

        String foodName = foodNameEditText.getText().toString();
        float foodBe = Float.parseFloat(foodBeEditText.getText().toString());

        Food foodToSend = new Food(foodName, foodBe, (originalFood == null || originalFood.isUserCreated()));
        if (originalFood != null) {
            foodToSend.setId(originalFood.getId());
        }
        return foodToSend;
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

    private void unpackBundle(@NonNull Bundle bundle) {
        originalFood = bundle.getParcelable(ARG_FOOD);
    }
}
