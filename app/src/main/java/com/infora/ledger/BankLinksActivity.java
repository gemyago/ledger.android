package com.infora.ledger;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.infora.ledger.BanksContract.BankLinks;
import com.infora.ledger.application.commands.DeleteBankLinksCommand;
import com.infora.ledger.application.commands.FetchBankTransactionsCommand;
import com.infora.ledger.application.events.BankLinksDeletedEvent;
import com.infora.ledger.application.events.BankTransactionsFetched;
import com.infora.ledger.application.events.FetchBankTransactionsFailed;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.support.BusUtils;
import com.infora.ledger.support.EventHandler;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by jenya on 30.05.15.
 */
public class BankLinksActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = BankLinksActivity.class.getName();
    private static final int BANK_LINKS_LOADER_ID = 1;
    public static final String BANK_LINK_ID_EXTRA = "BANK_LINK_ID";
    private SimpleCursorAdapter bankLinksAdapter;
    @Bind(R.id.bank_links_list)
    ListView lvBankLinks;
    ListView.MultiChoiceModeListener bankLinksChoiceListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank_links);
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        bankLinksAdapter = new SimpleCursorAdapter(this, R.layout.banks_links_list,
                null,
                new String[]{BankLinks.COLUMN_BIC, BankLinks.COLUMN_ACCOUNT_NAME},
                new int[]{R.id.bank_link_data, R.id.ledger_account_name}, 0);

        lvBankLinks.setAdapter(bankLinksAdapter);
        lvBankLinks.setEmptyView(findViewById(android.R.id.empty));
        lvBankLinks.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        bankLinksChoiceListener = new BankLinksChoiceListener();
        lvBankLinks.setMultiChoiceModeListener(bankLinksChoiceListener);
        lvBankLinks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "Editing bank link: " + id);
                Intent intent = new Intent(BankLinksActivity.this, EditBankLinkActivity.class);
                intent.putExtra(BANK_LINK_ID_EXTRA, id);
                startActivity(intent);
            }
        });


        getLoaderManager().initLoader(BANK_LINKS_LOADER_ID, null, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        BusUtils.register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        BusUtils.unregister(this);
    }


    @EventHandler
    public void onEventMainThread(BankLinksDeletedEvent event) {
        int removedLength = event.ids.length;
        String message = getResources().getQuantityString(R.plurals.bank_links_removed, removedLength, removedLength);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void onEventMainThread(BankTransactionsFetched event) {
        Log.d(TAG, "Handling fetch success event. BankLinkId: " + event.bankLink.id);
        BankLink bankLink = event.bankLink;
        Toast.makeText(this, bankLink.bic + "(" + bankLink.accountName + ") transactions fetched.", Toast.LENGTH_LONG).show();
    }

    public void onEventMainThread(FetchBankTransactionsFailed event) {
        Log.d(TAG, "Handling fetch failure event. BankLinkId: " + event.bankLinkId);
        Toast.makeText(this, "Failed to fetch transactions: " + event.error.getMessage(), Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_bank_links, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_add_bank_link) {
            startActivity(new Intent(this, AddBankLinkActivity.class));
            return true;
        }

        if (id == R.id.action_fetch_all_bank_links) {
            for (int i = 0; i < bankLinksAdapter.getCount(); i++) {
                 BusUtils.post(this, new FetchBankTransactionsCommand((int) bankLinksAdapter.getItemId(i)));
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (BANK_LINKS_LOADER_ID == id) {
            return new CursorLoader(this, BankLinks.CONTENT_URI, null, null, null, null);
        } else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        bankLinksAdapter.swapCursor(data);
        BusUtils.post(this, new BankLinksLoaded());
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        bankLinksAdapter.swapCursor(null);
    }

    private class BankLinksChoiceListener implements ListView.MultiChoiceModeListener {
        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            setSubtitle(mode);
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.bank_links_actions, menu);
            mode.setTitle(getString(R.string.select_bank_links));
            setSubtitle(mode);
            return true;

        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            long[] checkedItemIds = lvBankLinks.getCheckedItemIds();
            switch (item.getItemId()) {
                case R.id.menu_delete:
                    BusUtils.post(BankLinksActivity.this, new DeleteBankLinksCommand(checkedItemIds));
                    mode.finish();
                    break;
                case R.id.menu_fetch_bank_transactions:
                    mode.finish();
                    for (int i = 0; i < checkedItemIds.length; i++) {
                        long checkedItemId = checkedItemIds[i];
                        BusUtils.post(BankLinksActivity.this, new FetchBankTransactionsCommand((int) checkedItemId));
                    }
                    break;
                default:
                    throw new UnsupportedOperationException("Action item " + item.getTitle() + " is not supported.");
            }
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {

        }

        private void setSubtitle(ActionMode mode) {
            final int checkedCount = lvBankLinks.getCheckedItemCount();
            if (checkedCount == 0) {
                mode.setSubtitle(null);
            } else {
                String selectedString = getResources().getQuantityString(R.plurals.number_of_selected_bank_links, checkedCount, checkedCount);
                mode.setSubtitle(selectedString);
            }
        }
    }

    public static class BankLinksLoaded {
    }
}
