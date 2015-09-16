package com.infora.ledger.mocks.di;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by mye on 9/16/2015.
 */
@Singleton
@Component(modules = TestApplicationModule.class)
public interface MockApplicationComponent extends TestDependenciesInjector {
}
