package com.infora.ledger.application.banks;

/**
 * Created by jenya on 07.06.15.
 */
public class FetchStrategiesFactory {
    public FetchStrategy getStrategy(String bic) {
        return null;
    }

    public static FetchStrategiesFactory createDefault() {
        FetchStrategiesFactory factory = new FetchStrategiesFactory();
        //TODO: Register strategies
        return factory;
    }
}
