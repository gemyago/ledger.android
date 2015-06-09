package com.infora.ledger.support;

import java.util.Date;

/**
 * Created by jenya on 06.06.15.
 */
public class SystemDate {
    private static Date date;

    public static Date setNow(Date date) {
        SystemDate.date = date;
        return date;
    }

    public static Date now() {
        return date == null ? new Date() : date;
    }
}
