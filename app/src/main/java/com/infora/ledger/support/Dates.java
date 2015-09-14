package com.infora.ledger.support;

import com.infora.ledger.data.LedgerDbHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by jenya on 16.06.15.
 */
public class Dates {
    public static Date monthAgo(Date date) {
        Calendar cal = getCalendar(date);
        cal.add(Calendar.MONTH, -1);
        return cal.getTime();
    }

    public static Date create(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DATE, day);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static Date startOfDay(Date original) {
        Calendar cal = getCalendar(original);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static Date endOfDay(Date original) {
        Calendar cal = getCalendar(original);
        cal.set(Calendar.HOUR_OF_DAY, 23);
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

    public static Date addMinutes(Date original, int count) {
        Calendar cal = getCalendar(original);
        cal.add(Calendar.MINUTE, count);
        return cal.getTime();
    }

    public static Date addDays(Date original, int count) {
        Calendar cal = getCalendar(original);
        cal.add(Calendar.DAY_OF_MONTH, count);
        return cal.getTime();
    }

    public static int weeksBetween(Date start, Date end) {
        return (int) ((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24 * 7));
    }

    private static Calendar getCalendar(Date original) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(original);
        return cal;
    }

    public static boolean areEqual(Date left, Date right) {
        return (left == null && right == null) ||
                (left != null && right != null && LedgerDbHelper.toISO8601(left).equals(LedgerDbHelper.toISO8601(right)));
    }

    public static Date set(Date date, int year, int month, int day, int hour, int minute, int second) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(year, month, day, hour, minute, second);
        return cal.getTime();
    }

    public static String format(Date date, String formatString) {
        return new SimpleDateFormat(formatString).format(date);
    }

    public static Date parse(String formatString, String dateString) throws ParseException {
        return new SimpleDateFormat(formatString).parse(dateString);
    }
}
