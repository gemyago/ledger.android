package com.infora.ledger.mocks;

import android.content.Context;
import android.content.SharedPreferences;

import com.infora.ledger.data.SharedPreferencesProvider;

import java.util.UUID;

/**
 * Created by jenya on 21.03.15.
 */
public class MockSharedPreferencesProvider extends SharedPreferencesProvider {
    private final UUID randomPart;

    public MockSharedPreferencesProvider(Context context) {
        super(context);
        randomPart = UUID.randomUUID();
    }

    @Override
    public SharedPreferences getSharedPreferences(String name, int mode) {
        return context.getSharedPreferences(name + randomPart, mode);
    }
}
