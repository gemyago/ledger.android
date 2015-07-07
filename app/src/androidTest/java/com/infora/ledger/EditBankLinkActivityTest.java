package com.infora.ledger;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Spinner;

import com.infora.ledger.api.LedgerAccountDto;
import com.infora.ledger.application.commands.UpdateBankLinkCommand;
import com.infora.ledger.application.events.BankLinkUpdated;
import com.infora.ledger.application.events.UpdateBankLinkFailed;
import com.infora.ledger.banks.ua.privatbank.PrivatBankLinkData;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.data.LedgerAccountsLoader;
import com.infora.ledger.data.LedgerDbHelper;
import com.infora.ledger.mocks.BarrierSubscriber;
import com.infora.ledger.mocks.MockDatabaseRepository;
import com.infora.ledger.mocks.MockLedgerApi;
import com.infora.ledger.mocks.MockLedgerApplication;
import com.infora.ledger.mocks.MockSubscriber;
import com.infora.ledger.support.Dates;
import com.infora.ledger.support.LogUtil;
import com.infora.ledger.ui.DatePickerFragment;

import java.util.Arrays;
import java.util.Date;

import de.greenrobot.event.EventBus;

/**
 * Created by jenya on 31.05.15.
 */
public class EditBankLinkActivityTest extends android.test.ActivityUnitTestCase<EditBankLinkActivity> {

    private EventBus bus;
    private MockDatabaseRepository mockBankLinksRepo;
    private BankLink bankLink;
    private LedgerAccountDto selectedAccount;

    public EditBankLinkActivityTest() {
        super(EditBankLinkActivity.class);
    }

    @Override
    protected void setActivity(Activity testActivity) {
        if (testActivity != null) testActivity.setTheme(R.style.AppTheme);
        super.setActivity(testActivity);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        final Context baseContext = getInstrumentation().getTargetContext();

        Instrumentation instrumentation = new Instrumentation() {
            @Override
            public void callActivityOnCreate(Activity activity, Bundle icicle) {
                ((EditBankLinkActivity)activity).setAccountsLoaderFactory(createAccountsLoaderFactory());
                super.callActivityOnCreate(activity, icicle);
            }

            @Override
            public Context getTargetContext() {
                return baseContext;
            }
        };
        injectInstrumentation(instrumentation);
        bus = new EventBus();
        final MockLedgerApplication app = new MockLedgerApplication(baseContext, bus);
        setActivityContext(app);
        selectedAccount = new LedgerAccountDto("account-1", "Account 1");
        bankLink = new BankLink()
                .setId(332)
                .setBic("BANK-100")
                .setAccountId(selectedAccount.id)
                .setLinkData(new PrivatBankLinkData("card-100", "marchant-100", "password-100"));
        mockBankLinksRepo = new MockDatabaseRepository(BankLink.class);
        mockBankLinksRepo.entityToGetById = bankLink;
        Intent intent = new Intent();
        intent.putExtra(BankLinksActivity.BANK_LINK_ID_EXTRA, (long) bankLink.id);
        startActivity(intent, null, null);
        getActivity().setBankLinksRepo(mockBankLinksRepo);
    }

    public void testLoadBankLink() {
        BarrierSubscriber<EditBankLinkActivity.BankLinkLoaded> loadedSubscriber = new BarrierSubscriber<>(EditBankLinkActivity.BankLinkLoaded.class);
        bus.register(loadedSubscriber);
        getInstrumentation().callActivityOnStart(getActivity());
        loadedSubscriber.await();

        PrivatBankLinkFragment bankLinkFragment = (PrivatBankLinkFragment) getActivity()
                .getSupportFragmentManager().findFragmentById(R.id.bank_link_fragment_container);
        PrivatBankLinkData bankLinkData = bankLinkFragment.getBankLinkData();
        assertEquals(bankLink.getLinkData(PrivatBankLinkData.class), bankLinkData);
    }

    public void testLoadAndSelectAccount() {
        getActivity().setAccountsLoaderFactory(createAccountsLoaderFactory());
        BarrierSubscriber<EditBankLinkActivity.BankLinkLoaded> bankLinkBarrier = new BarrierSubscriber<>(EditBankLinkActivity.BankLinkLoaded.class);
        BarrierSubscriber<EditBankLinkActivity.AccountsLoaded> accountsBarrier = new BarrierSubscriber<>(EditBankLinkActivity.AccountsLoaded.class);
        bus.register(bankLinkBarrier);
        bus.register(accountsBarrier);
        LogUtil.d(this, "Calling activity onStart");
        getInstrumentation().callActivityOnStart(getActivity());
        accountsBarrier.await();
        bankLinkBarrier.await();

        Spinner accountsSpinner = (Spinner) getActivity().findViewById(R.id.ledger_account_id);
        assertEquals(3, accountsSpinner.getAdapter().getCount());
        Cursor selectedItem = (Cursor) accountsSpinner.getSelectedItem();
        assertNotNull(selectedItem);
        assertEquals(selectedAccount.id, selectedItem.getString(selectedItem.getColumnIndexOrThrow(LedgerAccountsLoader.COLUMN_ACCOUNT_ID)));
    }

    public void testUpdateBankLink() {
        getActivity().setAccountsLoaderFactory(createAccountsLoaderFactory());
        BarrierSubscriber<EditBankLinkActivity.AccountsLoaded> accountsBarrier = new BarrierSubscriber<>(EditBankLinkActivity.AccountsLoaded.class);
        bus.register(accountsBarrier);
        LogUtil.d(this, "Calling activity onStart");
        getInstrumentation().callActivityOnStart(getActivity());
        accountsBarrier.await();

        PrivatBankLinkFragment bankLinkFragment = (PrivatBankLinkFragment) getActivity()
                .getSupportFragmentManager().findFragmentById(R.id.bank_link_fragment_container);

        PrivatBankLinkData newLinkData = new PrivatBankLinkData("new-card-100", "new-merchant-100", "new-password-100");
        bankLinkFragment.setBankLinkData(newLinkData);
        Spinner accountsSpinner = (Spinner) getActivity().findViewById(R.id.ledger_account_id);
        accountsSpinner.setSelection(2);

        MockSubscriber<UpdateBankLinkCommand> commandSubscriber = new MockSubscriber<>(UpdateBankLinkCommand.class);
        bus.register(commandSubscriber);

        getActivity().updateBankLink(null);

        assertEquals(1, commandSubscriber.getEvents().size());
        UpdateBankLinkCommand command = commandSubscriber.getEvent();
        assertEquals("a-200", command.accountId);
        assertEquals("A 200", command.accountName);
        assertEquals(newLinkData, command.bankLinkData);
        assertNull(command.fetchStartingFrom);

        Button updateButton = (Button) getActivity().findViewById(R.id.action_update_bank_link);
        assertFalse(updateButton.isEnabled());
    }

    public void testUpdateBankLinkWithChangedFetchFromDate() {
        getActivity().setAccountsLoaderFactory(createAccountsLoaderFactory());
        BarrierSubscriber<EditBankLinkActivity.BankLinkLoaded> bankLinkBarrier = new BarrierSubscriber<>(EditBankLinkActivity.BankLinkLoaded.class);
        bus.register(bankLinkBarrier);
        LogUtil.d(this, "Calling activity onStart");
        getInstrumentation().callActivityOnStart(getActivity());
        bankLinkBarrier.await();

        MockSubscriber<UpdateBankLinkCommand> commandSubscriber = new MockSubscriber<>(UpdateBankLinkCommand.class);
        bus.register(commandSubscriber);
        Date fetchFrom = Dates.startOfDay(TestHelper.randomDate());
        LogUtil.d(this, "Raising DateChanged event to assign fetchFrom to: " + fetchFrom);
        getActivity().onEvent(new DatePickerFragment.DateChanged(fetchFrom));

        getActivity().updateBankLink(null);

        assertEquals(1, commandSubscriber.getEvents().size());
        UpdateBankLinkCommand command = commandSubscriber.getEvent();
        assertEquals(LedgerDbHelper.toISO8601(fetchFrom), LedgerDbHelper.toISO8601(command.fetchStartingFrom));
    }

    public void testBankLinkUpdated() {
        Button updateButton = (Button) getActivity().findViewById(R.id.action_update_bank_link);
        updateButton.setEnabled(false);
        getActivity().onEventMainThread(new BankLinkUpdated(bankLink.id));
        assertTrue(updateButton.isEnabled());
    }

    public void testUpdateBankLinkFailed() {
        Button updateButton = (Button) getActivity().findViewById(R.id.action_update_bank_link);
        updateButton.setEnabled(false);
        getActivity().onEventMainThread(new UpdateBankLinkFailed(bankLink.id, new Exception()));
        assertTrue(updateButton.isEnabled());
    }

    private LedgerAccountsLoader.Factory createAccountsLoaderFactory() {
        return new LedgerAccountsLoader.Factory() {
            @Override
            public LedgerAccountsLoader createLoader(Context context) {
                MockLedgerApi mockApi = new MockLedgerApi();
                mockApi.setAccounts(Arrays.asList(
                        new LedgerAccountDto("a-100", "A 100"),
                        selectedAccount,
                        new LedgerAccountDto("a-200", "A 200")
                ));
                return new LedgerAccountsLoader(context, mockApi);
            }
        };
    }
}