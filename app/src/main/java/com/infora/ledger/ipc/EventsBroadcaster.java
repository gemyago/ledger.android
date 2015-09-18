package com.infora.ledger.ipc;

import android.content.Context;
import android.content.Intent;
import android.os.*;
import android.util.Log;

import com.google.gson.Gson;
import com.infora.ledger.application.events.Event;

/**
 * Created by mye on 9/18/2015.
 */
public class EventsBroadcaster {
    private static final String TAG = EventsBroadcaster.class.getName();
    public static final String ACTION_BROADCAST_EVENT = "com.infora.ledger.ACTION_BROADCAST_EVENT";
    public static final String EVENT_TYPE = "event_type";
    public static final String EVENT_DATA = "event_data";
    public static final String EVENT_SENDER_PID = "event_sender_pid";
    private Context context;

    public EventsBroadcaster(Context context) {
        this.context = context;
    }

    public void onEventBackgroundThread(Event event) {
        if(event.headers.containsKey(Event.HEADER_BROADCAST)) return;
        Log.d(TAG, "Broadcasting event: " + event);
        Intent broadcast = new Intent();
        broadcast.setAction(ACTION_BROADCAST_EVENT);
        broadcast.setPackage("com.infora.ledger");
        broadcast.putExtra(EVENT_TYPE, event.getClass().getName());
        broadcast.putExtra(EVENT_DATA, new Gson().toJson(event));
        broadcast.putExtra(EVENT_SENDER_PID, android.os.Process.myPid());
        context.sendBroadcast(broadcast);
    }
}
