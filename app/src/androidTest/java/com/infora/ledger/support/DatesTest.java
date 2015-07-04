package com.infora.ledger.support;

import com.infora.ledger.TestHelper;

import junit.framework.TestCase;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by jenya on 04.07.15.
 */
public class DatesTest extends TestCase {

    public void testStartOfDay() throws Exception {
        final Date date = TestHelper.randomDate();
        final Calendar startOfDay = Calendar.getInstance();
        startOfDay.setTime(date);
        startOfDay.set(Calendar.HOUR_OF_DAY, 0);
        startOfDay.set(Calendar.MINUTE, 0);
        startOfDay.set(Calendar.SECOND, 0);
        startOfDay.set(Calendar.MILLISECOND, 0);
        assertEquals(startOfDay.getTime(), Dates.startOfDay(date));
    }

    public void testEndOfDay() throws Exception {
        final Date date = TestHelper.randomDate();
        final Calendar endOfDay = Calendar.getInstance();
        endOfDay.setTime(date);
        endOfDay.set(Calendar.HOUR_OF_DAY, 23);
        endOfDay.set(Calendar.MINUTE, 59);
        endOfDay.set(Calendar.SECOND, 59);
        endOfDay.set(Calendar.MILLISECOND, 0);
        assertEquals(endOfDay.getTime(), Dates.endOfDay(date));
    }
}