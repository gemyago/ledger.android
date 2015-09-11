package com.infora.ledger.mocks;

import android.app.Activity;

import com.infora.ledger.api.DeviceSecret;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.ui.BankLinkFragment;
import com.infora.ledger.ui.BankLinkFragmentsFactory;

import java.util.concurrent.Callable;

/**
 * Created by mye on 7/7/2015.
 */
public class MockBankLinkFragment extends BankLinkFragment<MockBankLinkData> {
    private MockBankLinkData mockBankLinkData;
    public String bic;

    public boolean beforeAddCalled;

    @Override
    public void onBeforeAdd(Activity parent) {
        beforeAddCalled = true;
    }

    public boolean beforeRemoveCalled;

    @Override
    public void onBeforeRemove(Activity parent) {
        beforeRemoveCalled = true;
    }

    @Override
    public MockBankLinkData getBankLinkData() {
        return mockBankLinkData;
    }

    @Override
    public void setBankLinkData(BankLink bankLink, DeviceSecret secret) {
        assignValues(bankLink, secret   );
    }

    @Override
    public void assignValues(BankLink bankLink, DeviceSecret secret) {
        this.mockBankLinkData = bankLink.getLinkData(MockBankLinkData.class, secret);
    }

    @Override
    public void clearLinkData() {
        mockBankLinkData = null;
    }

    public static void registerMockFragment(BankLinkFragmentsFactory factory, final String bic, final BankLink bankLink, final DeviceSecret secret) {
        factory.register(bic, new Callable<BankLinkFragment>() {
            @Override
            public BankLinkFragment call() throws Exception {
                MockBankLinkFragment fragment = new MockBankLinkFragment();
                fragment.bic = bic;
                if(bankLink != null) fragment.setBankLinkData(bankLink, secret);
                return fragment;
            }
        });
    }
}
