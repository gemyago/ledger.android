package com.infora.ledger.application.di;

import com.infora.ledger.AddBankLinkActivity;
import com.infora.ledger.BankLinksActivity;
import com.infora.ledger.EditBankLinkActivity;
import com.infora.ledger.GlobalActivityLifecycleCallbacks;
import com.infora.ledger.LedgerApplication;
import com.infora.ledger.LoginActivity;
import com.infora.ledger.Privat24BankLinkFragment;
import com.infora.ledger.ReportActivity;
import com.infora.ledger.application.PendingTransactionsSyncAdapter;
import com.infora.ledger.ipc.EventBroadcastsReceiver;

/**
 * Created by mye on 9/16/2015.
 */
public interface DependenciesInjector {
    void inject(LedgerApplication application);

    void inject(ReportActivity reportActivity);

    void inject(LoginActivity loginActivity);

    void inject(PendingTransactionsSyncAdapter pendingTransactionsSyncAdapter);

    void inject(AddBankLinkActivity addBankLinkActivity);

    void inject(BankLinksActivity bankLinksActivity);

    void inject(EditBankLinkActivity editBankLinkActivity);

    void inject(GlobalActivityLifecycleCallbacks globalActivityLifecycleCallbacks);

    void inject(Privat24BankLinkFragment privat24BankLinkFragment);

    void inject(EventBroadcastsReceiver.Dependencies dependencies);
}
