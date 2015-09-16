package com.infora.ledger.mocks.di;

import com.infora.ledger.AddBankLinkActivityTest;
import com.infora.ledger.BankLinksActivityTest;
import com.infora.ledger.EditBankLinkActivityTest;
import com.infora.ledger.EditTransactionDialogTest;
import com.infora.ledger.LoginActivityTest;
import com.infora.ledger.ReportActivityTest;
import com.infora.ledger.application.di.DependenciesInjector;

/**
 * Created by mye on 9/16/2015.
 */
public interface TestDependenciesInjector extends DependenciesInjector {
    void inject(LoginActivityTest loginActivityTest);

    void inject(AddBankLinkActivityTest addBankLinkActivityTest);

    void inject(BankLinksActivityTest bankLinksActivityTest);

    void inject(EditBankLinkActivityTest editBankLinkActivityTest);

    void inject(EditTransactionDialogTest editTransactionDialogTest);

    void inject(ReportActivityTest reportActivityTest);
}
