package com.infora.ledger;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

/**
 * Created by jenya on 04.06.15.
 */
public class TestHelper {
    private static final Random RANDOM = new Random(new Date().getTime());

    public static int randomInt() {
        return RANDOM.nextInt();
    }

    public static Date randomDate() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, -randomInt());
        return cal.getTime();
    }

    public static String randomString(String prefix) {
        return prefix + randomInt();
    }

    public static boolean randomBool() {
        return RANDOM.nextBoolean();
    }
}
