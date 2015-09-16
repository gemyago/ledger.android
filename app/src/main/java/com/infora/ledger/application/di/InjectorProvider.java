package com.infora.ledger.application.di;

/**
 * Created by mye on 9/16/2015.
 */
public interface InjectorProvider<TInjector extends DependenciesInjector> {
    TInjector injector();
}
