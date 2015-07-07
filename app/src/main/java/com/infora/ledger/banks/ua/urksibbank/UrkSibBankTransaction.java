package com.infora.ledger.banks.ua.urksibbank;

import com.infora.ledger.banks.BankTransaction;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.data.PendingTransaction;

/**
 * Created by jenya on 06.07.15.
 */
public class UrksibBankTransaction implements BankTransaction {
    //http://www.theswiftcodes.com/ukraine/khabua2k/
    public static final String BIC = "KHABUA2K";

    public String trandate;
    public String commitDate;
    public String authCode;
    public String description;
    public String currency;
    public String amount;
    public String accountAmount;

    @Override
    public PendingTransaction toPendingTransaction(BankLink bankLink) {
        return null;
    }
}
