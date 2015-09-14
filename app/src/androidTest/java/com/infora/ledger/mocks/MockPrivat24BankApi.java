package com.infora.ledger.mocks;

import com.infora.ledger.banks.FetchException;
import com.infora.ledger.banks.ua.privatbank.PrivatBankCard;
import com.infora.ledger.banks.ua.privatbank.api.Privat24BankApi;

import java.io.IOException;
import java.util.List;

/**
 * Created by mye on 9/11/2015.
 */
public class MockPrivat24BankApi extends Privat24BankApi {
    public MockPrivat24BankApi() {
        super(null, null);
    }

    public GetCardsCall onGetCards;

    @Override
    public List<PrivatBankCard> getCards() throws IOException, FetchException {
        return onGetCards.call();
    }

    public interface GetCardsCall {
        List<PrivatBankCard> call();
    }
}
