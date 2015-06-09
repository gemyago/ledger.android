package com.infora.ledger.banks;

import com.infora.ledger.application.banks.FetchStrategy;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.data.DatabaseContext;

import java.util.Date;

/**
 * Created by jenya on 07.06.15.
 */
public class PrivatBankFetchStrategy extends FetchStrategy {

    public PrivatBankApi getApi() {
        return null;
    }

    @Override
    public void fetchBankTransactions(DatabaseContext db, BankLink bankLink) {
        /* TODO
        set flags (inProgress=true and hasSucceed=false)
        starting from (exclusive) lastSyncDate till todayDate
        * if lastSyncDate is todayDate then fetch todays transactions again
        use api to fetch transactions
        use transactions read model(TODO) to get actual transactions for given bic
        start unit of work
        insert new transactions (that have not yet been saved)
        update flags (inProgress=false hasSucceed=true)
        update lastSyncDate to now
        commit unit of work
        * */
    }
}
