package com.infora.ledger.application.di;

import android.content.Context;

/**
 * Created by mye on 9/16/2015.
 */
public class DiUtils {
    public static DependenciesInjector injector(Context context) {
        return ((InjectorProvider) context.getApplicationContext()).injector();
    }
}
