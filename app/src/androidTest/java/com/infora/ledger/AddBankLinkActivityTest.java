package com.infora.ledger;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.infora.ledger.api.LedgerAccountDto;
import com.infora.ledger.application.commands.AddBankLinkCommand;
import com.infora.ledger.application.events.AddBankLinkFailed;
import com.infora.ledger.application.events.BankLinkAdded;
import com.infora.ledger.banks.PrivatBankLinkData;
import com.infora.ledger.banks.PrivatBankTransaction;
import com.infora.ledger.data.LedgerAccountsLoader;
import com.infora.ledger.mocks.MockLedgerAccountsLoader;
import com.infora.ledger.mocks.MockLedgerApi;
import com.infora.ledger.mocks.MockLedgerApplication;
import com.infora.ledger.mocks.MockSubscriber;
import com.infora.ledger.ui.DatePickerFragment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import de.greenrobot.event.EventBus;

/**
 * Created by jenya on 31.05.15.
 */
public class AddBankLinkActivityTest extends android.test.ActivityUnitTestCase<AddBankLinkActivity> {

    private EventBus bus;

    public AddBankLinkActivityTest() {
        super(AddBankLinkActivity.class);
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
                ((AddBankLinkActivity)activity).setAccountsLoaderFactory(createAccountsLoaderFactory());
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
        startActivity(new Intent(), null, null);
    }

    public void testInitialFetchDateIsSetToNow() {
        SimpleDateFormat dateOnlyFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date now = new Date();
        assertEquals(dateOnlyFormat.format(now), dateOnlyFormat.format(getActivity().getInitialFetchDate()));
    }

    public void testAddBankLink() {
        Window wnd = getActivity().getWindow();
        Spinner ledgerAccountId = (Spinner) wnd.findViewById(R.id.ledger_account_id);
        populateLedgerAccounts(ledgerAccountId, new LedgerAccountDto("a-1", "Account 1"), new LedgerAccountDto("a-2", "Account 2"));
        ledgerAccountId.setSelection(2);
        EditText merchantId = (EditText) wnd.findViewById(R.id.privat_bank_merchant_id);
        merchantId.setText("merchant-100");
        EditText merchantPassword = (EditText) wnd.findViewById(R.id.privat_bank_merchant_password);
        merchantPassword.setText("password-100");
        EditText cardNumber = (EditText) wnd.findViewById(R.id.privat_bank_card_number);
        cardNumber.setText("card-100");

        Button addButton = (Button) wnd.findViewById(R.id.action_add_bank_link);

        MockSubscriber<AddBankLinkCommand> addHandler = new MockSubscriber<>(AddBankLinkCommand.class);
        bus.register(addHandler);

        getActivity().addBankLink(null);

        assertEquals(1, addHandler.getEvents().size());
        AddBankLinkCommand cmd = addHandler.getEvent();
        assertEquals(PrivatBankTransaction.PRIVATBANK_BIC, cmd.bic);
        assertEquals(getActivity().getInitialFetchDate(), cmd.initialFetchDate);
        assertEquals("a-2", cmd.accountId);
        assertEquals("Account 2", cmd.accountName);
        PrivatBankLinkData linkData = (PrivatBankLinkData) cmd.linkData;
        assertEquals("merchant-100", linkData.merchantId);
        assertEquals("password-100", linkData.password);
        assertEquals("card-100", linkData.card);
        assertFalse(addButton.isEnabled());
    }

    public void testLinkAdded() {
        Window wnd = getActivity().getWindow();
        Spinner ledgerAccountId = (Spinner) wnd.findViewById(R.id.ledger_account_id);
        populateLedgerAccounts(ledgerAccountId, new LedgerAccountDto("a-1", "Account 1"), new LedgerAccountDto("a-2", "Account 2"));
        ledgerAccountId.setSelection(1);
        EditText merchantId = (EditText) wnd.findViewById(R.id.privat_bank_merchant_id);
        merchantId.setText("merchant-100");
        EditText merchantPassword = (EditText) wnd.findViewById(R.id.privat_bank_merchant_password);
        merchantPassword.setText("password-100");
        EditText cardNumber = (EditText) wnd.findViewById(R.id.privat_bank_card_number);
        cardNumber.setText("card-100");

        Button addButton = (Button) wnd.findViewById(R.id.action_add_bank_link);
        addButton.setEnabled(false);

        getActivity().onEventMainThread(new BankLinkAdded("a-2", "bic"));

        assertEquals(0, ledgerAccountId.getSelectedItemId());
        assertEquals("", merchantId.getText().toString());
        assertEquals("", merchantPassword.getText().toString());
        assertEquals("", cardNumber.getText().toString());
        assertTrue(addButton.isEnabled());
    }

    public void testAddBankLinkFailed() {
        Window wnd = getActivity().getWindow();

        Button addButton = (Button) wnd.findViewById(R.id.action_add_bank_link);
        addButton.setEnabled(false);

        getActivity().onEventMainThread(new AddBankLinkFailed(new Exception("Some exception")));

        assertTrue(addButton.isEnabled());
    }

    public void testChangeInitialFetchDate() {
        SimpleDateFormat dateOnlyFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, -5);

        getActivity().onEventMainThread(new DatePickerFragment.DateChanged(
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
        ));

        assertEquals(dateOnlyFormat.format(c.getTime()), dateOnlyFormat.format(getActivity().getInitialFetchDate()));

        TextView initialFetchDate = (TextView) getActivity().findViewById(R.id.initial_fetch_date);
        assertEquals(DateFormat.getDateInstance().format(c.getTime()), initialFetchDate.getText().toString());
    }

    private LedgerAccountsLoader.Factory createAccountsLoaderFactory() {
        return new LedgerAccountsLoader.Factory() {
            @Override
            public LedgerAccountsLoader createLoader(Context context) {
                return new MockLedgerAccountsLoader();
            }
        };
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