package com.infora.ledger.ui;

import android.support.v4.app.Fragment;

/**
 * Created by mye on 7/7/2015.
 */
public abstract class BankLinkFragment<TLinkData> extends Fragment {
    public abstract TLinkData getBankLinkData();

    public abstract void setBankLinkData(TLinkData linkData);

    public abstract void clearLinkData();
}
