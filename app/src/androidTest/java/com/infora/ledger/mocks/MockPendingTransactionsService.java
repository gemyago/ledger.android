package com.infora.ledger.mocks;

import com.infora.ledger.application.PendingTransactionsService;
import com.infora.ledger.application.commands.DeleteTransactionsCommand;

import java.sql.SQLException;
import java.util.ArrayList;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;

/**
 * Created by mye on 9/24/2015.
 */
public class MockPendingTransactionsService extends PendingTransactionsService {

    public final ArrayList<DeleteTransactionsCommand> deleteCommands = new ArrayList<>();

    @Inject public MockPendingTransactionsService() {
        super(new MockDatabaseContext(), new EventBus());
    }

    @Override
    public void deleteTransactions(DeleteTransactionsCommand command) throws SQLException {
        deleteCommands.add(command);
    }
}
