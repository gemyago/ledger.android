package com.infora.ledger;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.os.Bundle;
import android.widget.Spinner;

import com.infora.ledger.api.LedgerAccountDto;
import com.infora.ledger.banks.PrivatBankLinkData;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.data.LedgerAccountsLoader;
import com.infora.ledger.mocks.BarrierSubscriber;
import com.infora.ledger.mocks.MockBankLinksRepository;
import com.infora.ledger.mocks.MockLedgerApi;
import com.infora.ledger.mocks.MockLedgerApplication;
import com.infora.ledger.support.LogUtil;

import java.util.Arrays;

import de.greenrobot.event.EventBus;

/**
 * Created by jenya on 31.05.15.
 */
public class EditBankLinkActivityTest extends android.test.ActivityUnitTestCase<EditBankLinkActivity> {

    private EventBus bus;
    private MockBankLinksRepository mockBankLinksRepo;
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
        mockBankLinksRepo = new MockBankLinksRepository();
        mockBankLinksRepo.bankLinkToGetById = bankLink;
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
                .getSupportFragmentManager().findFragmentById(R.id.bank_link_fragment);
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