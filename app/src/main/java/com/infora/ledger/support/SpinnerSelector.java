package com.infora.ledger.support;

import android.database.Cursor;
import android.widget.Spinner;

import java.util.Objects;

/**
 * Created by mye on 7/27/2015.
 */
public class SpinnerSelector {
    public static final void select(Spinner spinner, String columnName, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            Cursor item = (Cursor) spinner.getItemAtPosition(i);
            String itemValue = item.getString(item.getColumnIndexOrThrow(columnName));
            if (Objects.equals(value, itemValue)) {
                spinner.setSelection(i);
                break;
            }
        }
    }
}
