package com.infora.ledger.ipc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;
import com.infora.ledger.application.di.DiUtils;
import com.infora.ledger.application.events.Event;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;

/**
 * Created by mye on 9/18/2015.
 */
public class EventBroadcastsReceiver extends BroadcastReceiver {
    private static final String TAG = EventBroadcastsReceiver.class.getName();

    @Override public void onReceive(Context context, Intent intent) {
        int senderPid = intent.getExtras().getInt(EventsBroadcaster.EVENT_SENDER_PID);
        int myPid = android.os.Process.myPid();
        if (myPid == senderPid) return;

        String eventType = intent.getExtras().getString(EventsBroadcaster.EVENT_TYPE);
        String eventData = intent.getExtras().getString(EventsBroadcaster.EVENT_DATA);
        Log.d(TAG, "Broadcast event '" + eventType + "' received. Posting to local bus.");
        Event event;
        try {
            event = (Event) new Gson().fromJson(eventData, Class.forName(eventType));
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Failed to deserialize event.", e);
            throw new RuntimeException(e);
        }
        event.headers.put(Event.HEADER_BROADCAST, "true");
        Dependencies dependencies = new Dependencies();
        DiUtils.injector(context).inject(dependencies);
        dependencies.bus.post(event);
    }

    public static final class Dependencies {
        @Inject EventBus bus;
    }
}
