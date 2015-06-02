package com.infora.ledger;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.MatrixCursor;
import android.support.v4.app.Fragment;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import com.infora.ledger.api.LedgerAccountDto;
import com.infora.ledger.banks.PrivatBankLinkData;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.data.LedgerAccountsLoader;
import com.infora.ledger.mocks.BarrierSubscriber;
import com.infora.ledger.mocks.MockBankLinksRepository;
import com.infora.ledger.mocks.MockLedgerApplication;
import com.infora.ledger.mocks.MockSubscriber;
import com.infora.ledger.support.BusUtils;

import de.greenrobot.event.EventBus;

/**
 * Created by jenya on 31.05.15.
 */
public class EditBankLinkActivityTest extends android.test.ActivityUnitTestCase<EditBankLinkActivity> {

    private EventBus bus;
    private MockBankLinksRepository mockBankLinksRepo;
    private BankLink bankLink;

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
        bus = new EventBus();
        final MockLedgerApplication app = new MockLedgerApplication(baseContext, bus);
        setActivityContext(app);
        bankLink = new BankLink()
                .setId(332)
                .setBic("BANK-100")
                .setAccountId("account-1")
                .setLinkData(new PrivatBankLinkData("card-100", "marchant-100", "password-100"));
        mockBankLinksRepo = new MockBankLinksRepository();
        mockBankLinksRepo.bankLinkToGetById = bankLink;
        Intent intent = new Intent();
        intent.putExtra(BankLinksActivity.BANK_LINK_ID_EXTRA, (long)bankLink.id);
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

    private void populateLedgerAccounts(Spinner spinner, LedgerAccountDto... dtos) {
        SimpleCursorAdapter spinnerAdapter = new SimpleCursorAdapter(getActivity(),
                android.R.layout.simple_spinner_item,
                null,
                new String[]{LedgerAccountsLoader.COLUMN_NAME},
                new int[]{android.R.id.text1},
                0);

        final MatrixCursor cursor = new MatrixCursor(new String[]{
                LedgerAccountsLoader.COLUMN_ID,
                LedgerAccountsLoader.COLUMN_ACCOUNT_ID,
                LedgerAccountsLoader.COLUMN_NAME,
        });
        int id = 0;
        cursor.addRow(new Object[]{id++, null, null});
        for (LedgerAccountDto dto : dtos) {
            cursor.addRow(new Object[]{id++, dto.id, dto.name});
        }
        spinnerAdapter.swapCursor(cursor);
        spinner.setAdapter(spinnerAdapter);
    }
}