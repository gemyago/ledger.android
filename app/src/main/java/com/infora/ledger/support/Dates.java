package com.infora.ledger.support;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by jenya on 16.06.15.
 */
public class Dates {
    public static Date startOfDay(Date original) {
        Calendar cal = getCalendar(original);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static Date endOfDay(Date original) {
        Calendar cal = getCalendar(original);
        cal.set(Calendar.HOUR, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static Date addHours(Date original, int count) {
        Calendar cal = getCalendar(original);
        cal.add(Calendar.HOUR, count);
        return cal.getTime();
    }

    public static Date addDays(Date original, int count) {
        Calendar cal = getCalendar(original);
        cal.add(Calendar.DAY_OF_MONTH, count);
        return cal.getTime();
    }

    private static Calendar getCalendar(Date original) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(original);
        return cal;
    }
}
