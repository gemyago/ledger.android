package com.infora.ledger.application.di;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by mye on 9/15/2015.
 */
@Singleton
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent extends DependenciesInjector {
}
