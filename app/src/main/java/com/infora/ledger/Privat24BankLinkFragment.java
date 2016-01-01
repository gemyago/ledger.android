package com.infora.ledger;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.infora.ledger.api.DeviceSecret;
import com.infora.ledger.application.di.DiUtils;
import com.infora.ledger.banks.ua.privatbank.Privat24BankLinkData;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.ui.BankLinkFragment;
import com.infora.ledger.ui.privat24.AddBankLinkFragmentModeState;
import com.infora.ledger.ui.privat24.BankLinkFragmentModeState;
import com.infora.ledger.ui.privat24.EditBankLinkFragmentModeState;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;

/**
 * Created by jenya on 01.06.15.
 */
public class Privat24BankLinkFragment extends BankLinkFragment<Privat24BankLinkData> {

    private static final String TAG = Privat24BankLinkFragment.class.getName();

    BankLinkFragmentModeState modeState;

    @Inject EventBus bus;

    @Override
    public void setMode(Mode mode) {
        switch (mode) {
            case Add:
                modeState = new AddBankLinkFragmentModeState();
                break;
            case Edit:
                modeState = new EditBankLinkFragmentModeState();
                break;
            default:
                throw new IllegalArgumentException("The mode '" + mode + "' is not supported.");
        }
        Log.d(TAG, "Mode state initialized.");
        super.setMode(mode);
    }

    @Override
    public void onBeforeAdd(Activity parent) {
        DiUtils.injector(parent).inject(this);
        bus.register(modeState);
        Log.d(TAG, "Fragment registered to handle events.");
    }

    @Override
    public void onBeforeRemove(Activity parent) {
        bus.unregister(modeState);
        Log.d(TAG, "Fragment unregistered from events handling.");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_privat24_bank_link, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "View created. Notifying mode state.");
        modeState.processViewCreated(view);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (bus != null) {
            bus.unregister(modeState);
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
}
