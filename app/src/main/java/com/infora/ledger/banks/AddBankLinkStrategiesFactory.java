package com.infora.ledger.banks;

import android.content.Context;

import com.infora.ledger.application.di.DiUtils;
import com.infora.ledger.banks.ua.privatbank.Privat24Transaction;

import java.util.HashMap;
import java.util.concurrent.Callable;

/**
 * Created by mye on 9/11/2015.
 */
public class AddBankLinkStrategiesFactory {
    private final HashMap<String, Callable<AddBankLinkStrategy>> strategies;
    private final AddBankLinkStrategy defaultStrategy;

    public AddBankLinkStrategiesFactory(AddBankLinkStrategy defaultStrategy) {
        this.defaultStrategy = defaultStrategy;
        strategies = new HashMap<>();
    }

    public AddBankLinkStrategy getStrategy(String bic) {
        if (!strategies.containsKey(bic)) return defaultStrategy;
        final Callable<AddBankLinkStrategy> strategyFactory = strategies.get(bic);
        try {
            return strategyFactory.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static AddBankLinkStrategiesFactory createDefault(final Context context) {
        AddBankLinkStrategiesFactory factory = new AddBankLinkStrategiesFactory(DiUtils.injector(context).provideDefaultAddBankLinkStrategy());
        factory.strategies.put(Privat24Transaction.PRIVATBANK_BIC, new Callable<AddBankLinkStrategy>() {
            @Override
            public AddBankLinkStrategy call() throws Exception {
                return DiUtils.injector(context).providePrivat24AddBankLinkStrategy();
            }
        });
        return factory;
    }
}
