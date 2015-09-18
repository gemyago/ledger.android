package com.infora.ledger.ipc;

import android.content.ContextWrapper;
import android.content.Intent;

import com.google.gson.Gson;
import com.infora.ledger.application.events.Event;

import junit.framework.TestCase;

import java.util.ArrayList;

import static com.infora.ledger.ipc.EventsBroadcaster.ACTION_BROADCAST_EVENT;
import static com.infora.ledger.ipc.EventsBroadcaster.EVENT_DATA;
import static com.infora.ledger.ipc.EventsBroadcaster.EVENT_SENDER_PID;
import static com.infora.ledger.ipc.EventsBroadcaster.EVENT_TYPE;

/**
 * Created by mye on 9/18/2015.
 */
public class EventsBroadcasterTest extends TestCase {

    private ArrayList<Intent> sentBroadcasts;
    private EventsBroadcaster subject;

    @Override protected void setUp() throws Exception {
        super.setUp();
        sentBroadcasts = new ArrayList<>();
        subject = new EventsBroadcaster(new ContextWrapper(null) {
            @Override public void sendBroadcast(Intent intent) {
                sentBroadcasts.add(intent);
            }
        });
    }

    public void testBroadcastsEvent() throws Exception {
        subject.onEvent(new DummyEvent("Prop 100"));
        assertEquals(1, sentBroadcasts.size());

        Intent broadcast = sentBroadcasts.get(0);
        assertEquals(ACTION_BROADCAST_EVENT, broadcast.getAction());
        assertEquals("com.infora.ledger", broadcast.getPackage());
        String eventType = broadcast.getExtras().getString(EVENT_TYPE);
        assertEquals(DummyEvent.class.getName(), eventType);
        assertEquals(android.os.Process.myPid(), broadcast.getExtras().getInt(EVENT_SENDER_PID));
        DummyEvent event = (DummyEvent) new Gson().fromJson(broadcast.getExtras().getString(EVENT_DATA), Class.forName(eventType));
        assertEquals("Prop 100", event.prop);
    }

    public void testDoNotBroadcastsIfEventWasBroadcast() throws Exception {
        DummyEvent originalEvent = new DummyEvent("Prop 100");
        originalEvent.headers.put(Event.HEADER_BROADCAST, "true");
        subject.onEvent(originalEvent);
        assertEquals(0, sentBroadcasts.size());
    }

    private static class DummyEvent extends Event {
        public String prop;

        public DummyEvent(String prop) {
            this.prop = prop;
        }
    }
}