package com.infora.ledger;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.infora.ledger.api.DeviceSecret;
import com.infora.ledger.application.di.DiUtils;
import com.infora.ledger.banks.ua.privatbank.Privat24BankLinkData;
import com.infora.ledger.banks.ua.privatbank.Privat24BankService;
import com.infora.ledger.banks.ua.privatbank.PrivatBankException;
import com.infora.ledger.banks.ua.privatbank.messages.AskPrivat24Otp;
import com.infora.ledger.banks.ua.privatbank.messages.AuthenticateWithOtp;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.support.ObfuscatedString;
import com.infora.ledger.ui.BankLinkFragment;

import java.io.IOException;
import java.sql.SQLException;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;

/**
 * Created by jenya on 01.06.15.
 */
public class Privat24BankLinkFragment extends BankLinkFragment<Privat24BankLinkData> {

    private static final String TAG = Privat24BankLinkFragment.class.getName();

    private ModeState modeState;

    @Inject EventBus bus;

    @Override
    public void setMode(Mode mode) {
        switch (mode) {
            case Add:
                modeState = new AddModeState();
                break;
            case Edit:
                modeState = new EditModeState();
                break;
            default:
                throw new IllegalArgumentException("The mode '" + mode + "' is not supported.");
        }
        super.setMode(mode);
    }

    @Override
    public void onBeforeAdd(Activity parent) {
        DiUtils.injector(parent).inject(this);
        bus.register(this);
        Log.d(TAG, "Fragment registered to handle events.");
    }

    @Override
    public void onBeforeRemove(Activity parent) {
        bus.unregister(this);
        Log.d(TAG, "Fragment unregistered from events handling.");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_privat24_bank_link, container, false);
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        modeState.onViewCreated(view);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        if (bus != null) {
            bus.unregister(this);
            Log.d(TAG, "Fragment unregistered from events handling.");
        }
    }

    @Override
    public Privat24BankLinkData getBankLinkData() {
        return modeState.getBankLinkData(getView());
    }

    @Override
    public void assignValues(BankLink bankLink, DeviceSecret secret) {
        modeState.assignValues(getView(), bankLink, bankLink.getLinkData(Privat24BankLinkData.class, secret));
    }

    @Override
    public void clearLinkData() {
        EditText login = (EditText) getView().findViewById(R.id.privat24_login);
        EditText password = (EditText) getView().findViewById(R.id.privat24_password);
        EditText card = (EditText) getView().findViewById(R.id.privat24_card_number);
        login.setText("");
        password.setText("");
        card.setText("");
    }

    public void onEventMainThread(final AskPrivat24Otp cmd) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Please provide OTP password");

        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String otpPassword = input.getText().toString();
                bus.post(new AuthenticateWithOtp(cmd.operationId, otpPassword));
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.show();
    }

    private interface ModeState {
        Privat24BankLinkData getBankLinkData(View view);

        void assignValues(View view, BankLink bankLink, Privat24BankLinkData linkData);

        void onViewCreated(View view);
    }

    private static class AddModeState implements ModeState {
        @Override
        public Privat24BankLinkData getBankLinkData(View view) {
            EditText login = (EditText) view.findViewById(R.id.privat24_login);
            EditText password = (EditText) view.findViewById(R.id.privat24_password);
            EditText card = (EditText) view.findViewById(R.id.privat24_card_number);
            return new Privat24BankLinkData()
                    .setLogin(login.getText().toString())
                    .setPassword(password.getText().toString())
                    .setCardNumber(card.getText().toString());
        }

        @Override
        public void assignValues(View view, BankLink bankLink, Privat24BankLinkData linkData) {
            EditText login = (EditText) view.findViewById(R.id.privat24_login);
            EditText password = (EditText) view.findViewById(R.id.privat24_password);
            EditText card = (EditText) view.findViewById(R.id.privat24_card_number);
            login.setText(linkData.login);
            password.setText(linkData.password);
            card.setText(linkData.cardNumber);
        }

        @Override public void onViewCreated(View view) {
            view.findViewById(R.id.privat24_refresh_authentication).setVisibility(View.GONE);
        }
    }

    public static class EditModeState implements ModeState {
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

        @Override public void onViewCreated(final View view) {
            DiUtils.injector(view.getContext()).inject(this);
            Button button = (Button) view.findViewById(R.id.privat24_refresh_authentication);
            button.setEnabled(true);
            button.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    Log.d(TAG, "Refreshing authentication...");
                    try {
                        bankService.refreshAuthentication(bankLink.id);
                    } catch (SQLException e) {
                        notifyRefreshError(view.getContext(), e);
                    } catch (IOException e) {
                        notifyRefreshError(view.getContext(), e);
                    } catch (PrivatBankException e) {
                        notifyRefreshError(view.getContext(), e);
                    }
                }
            });
        }

        private void notifyRefreshError(Context context, Exception e) {
            Log.e(TAG, "Failed to refresh authentication.", e);
            Toast.makeText(context, "Failure adding bank link: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
