package com.infora.ledger.mocks;

import android.content.Context;
import android.os.Bundle;

import com.infora.ledger.application.synchronization.SynchronizationStrategiesFactory;
import com.infora.ledger.application.synchronization.SynchronizationStrategy;

/**
 * Created by mye on 9/23/2015.
 */
public class MockSynchronizationStrategiesFactory extends SynchronizationStrategiesFactory {
    public OnCreatingStrategy onCreatingStrategy;

    @Override public SynchronizationStrategy createStrategy(Context context, Bundle options) {
        return onCreatingStrategy.call(context, options);
    }

    public interface OnCreatingStrategy {
        SynchronizationStrategy call(Context context, Bundle options);
    }
}
