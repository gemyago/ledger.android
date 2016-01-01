package com.infora.ledger.ui.privat24;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.infora.ledger.banks.ua.privatbank.Privat24BankLinkData;
import com.infora.ledger.data.BankLink;

/**
 * Created by jenya on 01.01.16.
 */
public abstract class BankLinkFragmentModeState {
    private Context context;
    private View view;

    public abstract Privat24BankLinkData getBankLinkData(View view);

    public abstract void assignValues(View view, BankLink bankLink, Privat24BankLinkData linkData);

    public final void processViewCreated(View view) {
        this.view = view;
        context = view.getContext();
        onViewCreated(view);
    }

    protected abstract void onViewCreated(View view);

    public Context getContext() {
        return context;
    }

    public View getView() {
        return view;
    }
}
