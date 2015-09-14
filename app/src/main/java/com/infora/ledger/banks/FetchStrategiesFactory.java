package com.infora.ledger.banks;

import com.infora.ledger.banks.ua.privatbank.api.Privat24ApiAdapterForDefaultFetchStrategy;
import com.infora.ledger.banks.ua.privatbank.PrivatBankTransaction;
import com.infora.ledger.banks.ua.urksibbank.UkrsibBankApi;
import com.infora.ledger.banks.ua.urksibbank.UkrsibBankTransaction;

import java.util.HashMap;
import java.util.concurrent.Callable;

/**
 * Created by jenya on 07.06.15.
 */
public class FetchStrategiesFactory {

    private final HashMap<String, Callable<FetchStrategy>> fetchStrategies;

    public FetchStrategiesFactory() {
        fetchStrategies = new HashMap<>();
    }

    public FetchStrategy getStrategy(String bic) {
        if (!fetchStrategies.containsKey(bic))
            throw new IllegalArgumentException("Can not locate fetch strategy. Unknown BIC: " + bic);
        final Callable<FetchStrategy> strategyFactory = fetchStrategies.get(bic);
        try {
            return strategyFactory.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static FetchStrategiesFactory createDefault() {
        FetchStrategiesFactory factory = new FetchStrategiesFactory();
        factory.fetchStrategies.put(PrivatBankTransaction.PRIVATBANK_BIC, new Callable<FetchStrategy>() {
            @Override
            public FetchStrategy call() throws Exception {
                return new DefaultFetchStrategy(new Privat24ApiAdapterForDefaultFetchStrategy());
            }
        });
        factory.fetchStrategies.put(UkrsibBankTransaction.BIC, new Callable<FetchStrategy>() {
            @Override
            public FetchStrategy call() throws Exception {
                return new DefaultFetchStrategy(new UkrsibBankApi());
            }
        });
        return factory;
    }
}
