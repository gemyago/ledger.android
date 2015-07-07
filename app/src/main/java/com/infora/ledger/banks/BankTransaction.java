package com.infora.ledger.banks;

import com.infora.ledger.data.BankLink;
import com.infora.ledger.data.PendingTransaction;

/**
 * Created by jenya on 07.07.15.
 */
public interface BankTransaction {
    PendingTransaction toPendingTransaction(BankLink bankLink);
}
