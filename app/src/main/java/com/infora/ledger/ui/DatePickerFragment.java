package com.infora.ledger.ui;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.infora.ledger.support.BusUtils;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by jenya on 18.06.15.
 */
public class DatePickerFragment extends DialogFragment {
    private Date initialDate;

    public DatePickerFragment setArguments(Date initialDate) {
        this.initialDate = initialDate;
        return this;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        c.setTime(initialDate);
        int initialYear = c.get(Calendar.YEAR);
        int initialMonth = c.get(Calendar.MONTH);
        int initialDay = c.get(Calendar.DAY_OF_MONTH);
        return new DatePickerDialog(getActivity(), null, initialYear, initialMonth, initialDay) {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == BUTTON_POSITIVE) {
                    int year = getDatePicker().getYear();
                    int month = getDatePicker().getMonth();
                    int day = getDatePicker().getDayOfMonth();

                    BusUtils.post(getActivity(), new DateChanged(year, month, day));
                }
                super.onClick(dialog, which);
            }
        };
    }

    public static class DateChanged {
        public final int year;
        public final int month;
        public final int day;

        public DateChanged(Date date) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            year = cal.get(Calendar.YEAR);
            month = cal.get(Calendar.MONTH);
            day = cal.get(Calendar.DAY_OF_MONTH);
        }

        public DateChanged(int year, int month, int day) {
            this.year = year;
            this.month = month;
            this.day = day;
        }
    }
}
