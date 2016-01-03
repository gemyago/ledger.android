package com.infora.ledger.ui.privat24;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;

import com.infora.ledger.R;
import com.infora.ledger.application.di.DiUtils;
import com.infora.ledger.banks.ua.privatbank.Privat24BankLinkData;
import com.infora.ledger.banks.ua.privatbank.messages.AskPrivat24OtpToCreateNewLink;
import com.infora.ledger.ui.privat24.messages.AuthenticateWithOtpAndCreateNewPrivat24Link;
import com.infora.ledger.data.BankLink;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;

/**
 * Created by jenya on 01.01.16.
 */
public class AddBankLinkFragmentModeState extends BankLinkFragmentModeState {
    @Inject EventBus bus;

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

    @Override
    protected void onViewCreated(View view) {
        DiUtils.injector(getContext()).inject(this);
        view.findViewById(R.id.privat24_refresh_authentication).setVisibility(View.GONE);
    }

    public void onEventMainThread(final AskPrivat24OtpToCreateNewLink cmd) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Please provide OTP password");

        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String otpPassword = input.getText().toString();
                bus.post(new AuthenticateWithOtpAndCreateNewPrivat24Link(cmd.operationId, otpPassword, cmd.bankLink));
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.show();
    }
}
