package com.infora.ledger.application.di;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Qualifier;

/**
 * Created by mye on 9/16/2015.
 */
@Qualifier @Retention(RetentionPolicy.RUNTIME)
public @interface ForApplication {
}
