package com.infora.ledger.banks;

import com.infora.ledger.banks.ua.privatbank.Privat24AddBankLinkStrategy;
import com.infora.ledger.banks.ua.privatbank.PrivatBankTransaction;

import java.util.HashMap;
import java.util.concurrent.Callable;

/**
 * Created by mye on 9/11/2015.
 */
public class AddBankLinkStrategiesFactory {
    private final HashMap<String, Callable<AddBankLinkStrategy>> strategies;

    public AddBankLinkStrategiesFactory() {
        strategies = new HashMap<>();
    }

    public AddBankLinkStrategy getStrategy(String bic) {
        if (!strategies.containsKey(bic))
            return new DefaultAddBankLinkStrategy();
        final Callable<AddBankLinkStrategy> strategyFactory = strategies.get(bic);
        try {
            return strategyFactory.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static AddBankLinkStrategiesFactory createDefault() {
        AddBankLinkStrategiesFactory factory = new AddBankLinkStrategiesFactory();
        factory.strategies.put(PrivatBankTransaction.PRIVATBANK_BIC, new Callable<AddBankLinkStrategy>() {
            @Override
            public AddBankLinkStrategy call() throws Exception {
                return new Privat24AddBankLinkStrategy();
            }
        });
        return factory;
    }
}
