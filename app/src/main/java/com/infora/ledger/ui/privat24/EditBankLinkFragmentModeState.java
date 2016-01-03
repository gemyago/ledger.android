package com.infora.ledger.ui.privat24;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
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
import com.infora.ledger.banks.ua.privatbank.messages.AskPrivat24OtpToRefreshAuthentication;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.support.ObfuscatedString;
import com.infora.ledger.ui.privat24.messages.AuthenticateWithOtpToRefreshAuthentication;
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

    public void onEventMainThread(final AskPrivat24OtpToRefreshAuthentication cmd) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Please provide OTP password");

        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String otpPassword = input.getText().toString();
                bus.post(new AuthenticateWithOtpToRefreshAuthentication(cmd.operationId, otpPassword, cmd.bankLink));
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.show();

    }

    public void onEventBackgroundThread(AuthenticateWithOtpToRefreshAuthentication cmd) {
        try {
            bankService.authenticateWithOtpToRefreshAuthentication(cmd.operationId, cmd.otp, cmd.bankLink);
        } catch(IOException e) {
            bus.post(new RefreshAuthenticationFailed(e));
        } catch(PrivatBankException e) {
            bus.post(new RefreshAuthenticationFailed(e));
        }
    }

    public void onEventMainThread(RefreshAuthenticationFailed evt) {
        Log.e(TAG, "Failed to refresh authentication.", evt.exception);
        Toast.makeText(getContext(), "Failure refreshing authentication: " + evt.exception.getMessage(), Toast.LENGTH_LONG).show();
        getView().findViewById(R.id.privat24_refresh_authentication).setEnabled(true);
    }

    public void onEventMainThread(AuthenticationRefreshed evt) {
        Log.d(TAG, "Refresh authentication sequence completed.");
        getView().findViewById(R.id.privat24_refresh_authentication).setEnabled(true);
    }
}
