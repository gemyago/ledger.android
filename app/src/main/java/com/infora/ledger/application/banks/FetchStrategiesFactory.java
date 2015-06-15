package com.infora.ledger.application.banks;

import com.infora.ledger.banks.PrivatBankFetchStrategy;
import com.infora.ledger.banks.PrivatBankTransaction;

import java.util.HashMap;

/**
 * Created by jenya on 07.06.15.
 */
public class FetchStrategiesFactory {

    private final HashMap<String, FetchStrategy> fetchStrategies;

    public FetchStrategiesFactory() {
        fetchStrategies = new HashMap<>();
    }

    public FetchStrategy getStrategy(String bic) {
        if(!fetchStrategies.containsKey(bic))
            throw new IllegalArgumentException("Can not locate fetch strategy. Unknown BIC: " + bic);
        return fetchStrategies.get(bic);
    }

    public static FetchStrategiesFactory createDefault() {
        FetchStrategiesFactory factory = new FetchStrategiesFactory();
        factory.fetchStrategies.put(PrivatBankTransaction.PRIVATBANK_BIC, new PrivatBankFetchStrategy());
        return factory;
    }
}
