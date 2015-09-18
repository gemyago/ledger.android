package com.infora.ledger.application.events;

import java.util.HashMap;

/**
 * Created by mye on 9/18/2015.
 */
public abstract class Event {
    public static final String HEADER_BROADCAST = "broadcast";

    public final HashMap<String, String> headers = new HashMap<>();
}
