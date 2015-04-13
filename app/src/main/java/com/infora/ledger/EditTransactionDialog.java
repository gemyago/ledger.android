package com.infora.ledger;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

/**
 * Created by jenya on 11.04.15.
 */
public class EditTransactionDialog extends DialogFragment {
    private static final String TAG = EditTransactionDialog.class.getName();

    public long id;
    public String amount;
    public String comment;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.dialog_edit_transaction, null);
        EditText etAmount = (EditText) view.findViewById(R.id.amount);
        EditText etComment = (EditText) view.findViewById(R.id.comment);
        etAmount.setText(amount);
        etComment.setText(comment);
        builder.setView(view)
                // Add action buttons
                .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        EditTransactionDialog.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }
}
