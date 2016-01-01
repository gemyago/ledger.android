package com.infora.ledger.ui.privat24;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.infora.ledger.R;
import com.infora.ledger.application.di.DiUtils;
import com.infora.ledger.banks.ua.privatbank.Privat24BankLinkData;
import com.infora.ledger.banks.ua.privatbank.Privat24BankService;
import com.infora.ledger.banks.ua.privatbank.PrivatBankException;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.support.ObfuscatedString;
import com.infora.ledger.ui.privat24.messages.AuthenticationRefreshed;
import com.infora.ledger.ui.privat24.messages.RefreshAuthentication;
import com.infora.ledger.ui.privat24.messages.RefreshAuthenticationFailed;

import java.io.IOException;
import java.sql.SQLException;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;

/**
 * Created by jenya on 01.01.16.
 */
public class EditBankLinkFragmentModeState extends BankLinkFragmentModeState {
    private static final String TAG = EditBankLinkFragmentModeState.class.getName();

    @Inject EventBus bus;
    @Inject Privat24BankService bankService;

    private Privat24BankLinkData linkData;
    private BankLink bankLink;

    @Override
    public Privat24BankLinkData getBankLinkData(View view) {
        return linkData;
    }

    @Override
    public void assignValues(View view, BankLink bankLink, Privat24BankLinkData linkData) {
        EditText login = (EditText) view.findViewById(R.id.privat24_login);
        EditText password = (EditText) view.findViewById(R.id.privat24_password);
        EditText card = (EditText) view.findViewById(R.id.privat24_card_number);
        login.setText(linkData.login);
        login.setEnabled(false);
        password.setText(ObfuscatedString.value(linkData.password));
        password.setEnabled(false);
        card.setText(ObfuscatedString.value(linkData.cardNumber));
        card.setEnabled(false);
        this.bankLink = bankLink;
        this.linkData = linkData;
    }

    @Override
    protected void onViewCreated(final View view) {
        DiUtils.injector(getContext()).inject(this);
        final Button button = (Button) view.findViewById(R.id.privat24_refresh_authentication);
        button.setEnabled(true);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Posting command to refresh authentication...");
                bus.post(new RefreshAuthentication(bankLink.id));
                button.setEnabled(false);
            }
        });
    }

    public void onEventBackgroundThread(RefreshAuthentication cmd) {
        Log.d(TAG, "Refreshing authentication...");
        try {
            bankService.refreshAuthentication(cmd.bankLinkId);
            bus.post(new AuthenticationRefreshed());
        } catch (SQLException e) {
            bus.post(new RefreshAuthenticationFailed(e));
        } catch (IOException e) {
            bus.post(new RefreshAuthenticationFailed(e));
        } catch (PrivatBankException e) {
            bus.post(new RefreshAuthenticationFailed(e));
        }
    }

    private void onEventMainThread(RefreshAuthenticationFailed evt) {
        Log.e(TAG, "Failed to refresh authentication.", evt.exception);
        Toast.makeText(getContext(), "Failure adding bank link: " + evt.exception.getMessage(), Toast.LENGTH_LONG).show();
    }

    private void onEventMainThread(AuthenticationRefreshed evt) {
        Log.d(TAG, "Refresh command completed.");
        final Button button = (Button) getView().findViewById(R.id.privat24_refresh_authentication);
        button.setEnabled(true);
    }
}
