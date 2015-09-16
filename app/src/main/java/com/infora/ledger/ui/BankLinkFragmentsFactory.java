package com.infora.ledger.ui;

import com.infora.ledger.Privat24BankLinkFragment;
import com.infora.ledger.UkrsibBankLinkFragment;
import com.infora.ledger.banks.ua.privatbank.PrivatBankTransaction;
import com.infora.ledger.banks.ua.urksibbank.UkrsibBankTransaction;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.inject.Inject;

/**
 * Created by mye on 7/7/2015.
 */
public class BankLinkFragmentsFactory {

    private final HashMap<String, Callable<BankLinkFragment>> fragmentsMap;

    @Inject public BankLinkFragmentsFactory() {
        fragmentsMap = new HashMap<>();
    }

    public BankLinkFragmentsFactory register(String bic, Callable<BankLinkFragment> fragmentFactory) {
        fragmentsMap.put(bic, fragmentFactory);
        return this;
    }

    public Set<String> knownBics() {
        return fragmentsMap.keySet();
    }

    public boolean isKnown(String bic) {
        return fragmentsMap.containsKey(bic);
    }

    public BankLinkFragment get(String bic) {
        if (!fragmentsMap.containsKey(bic))
            throw new IllegalArgumentException("Fragment for bank bic='" + bic + "' has not been registered");
        Callable<BankLinkFragment> bankLinkFragment = fragmentsMap.get(bic);
        try {
            return bankLinkFragment.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static BankLinkFragmentsFactory createDefault() {
        return new BankLinkFragmentsFactory()
                .register(PrivatBankTransaction.PRIVATBANK_BIC, new Callable<BankLinkFragment>() {
                    @Override
                    public BankLinkFragment call() throws Exception {
                        return new Privat24BankLinkFragment();
                    }
                })
                .register(UkrsibBankTransaction.BIC, new Callable<BankLinkFragment>() {
                    @Override
                    public BankLinkFragment call() throws Exception {
                        return new UkrsibBankLinkFragment();
                    }
                });
    }
}
