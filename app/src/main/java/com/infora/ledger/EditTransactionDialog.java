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

import com.infora.ledger.application.commands.AdjustTransactionCommand;
import com.infora.ledger.support.BusUtils;

import java.util.Objects;

/**
 * Created by jenya on 11.04.15.
 */
public class EditTransactionDialog extends DialogFragment {
    private static final String TAG = EditTransactionDialog.class.getName();

    public long transactionId;
    public String amount;
    public String comment;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.dialog_edit_transaction, null);
        final EditText etAmount = (EditText) view.findViewById(R.id.amount);
        final EditText etComment = (EditText) view.findViewById(R.id.comment);
        etAmount.setText(amount);
        etComment.setText(comment);
        builder.setView(view)
                // Add action buttons
                .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Log.d(TAG, "Posting adjust comment");
                        String newAmount = etAmount.getText().toString();
                        String newComment = etComment.getText().toString();
                        if(Objects.equals(newAmount, amount) && Objects.equals(newComment, comment)) {
                            Log.d(TAG, "No changes.");
                            return;
                        }
                        AdjustTransactionCommand cmd = new AdjustTransactionCommand(transactionId,
                                newAmount,
                                newComment);
                        BusUtils.post(getActivity(), cmd);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        Log.d(TAG, "Edit dialog created. Editing transaction: " + transactionId);
        return builder.create();
    }
}
