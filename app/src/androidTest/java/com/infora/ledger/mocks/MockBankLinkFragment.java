package com.infora.ledger.mocks;

import com.infora.ledger.ui.BankLinkFragment;
import com.infora.ledger.ui.BankLinkFragmentsFactory;

import java.util.concurrent.Callable;

/**
 * Created by mye on 7/7/2015.
 */
public class MockBankLinkFragment extends BankLinkFragment<MockBankLinkData> {
    private MockBankLinkData mockBankLinkData;

    @Override
    public MockBankLinkData getBankLinkData() {
        return mockBankLinkData;
    }

    @Override
    public void setBankLinkData(MockBankLinkData mockBankLinkData) {

        this.mockBankLinkData = mockBankLinkData;
    }

    @Override
    public void clearLinkData() {
        mockBankLinkData = null;
    }

    public static void registerMockFragment(BankLinkFragmentsFactory factory, String bic, final MockBankLinkData data) {
        factory.register(bic, new Callable<BankLinkFragment>() {
            @Override
            public BankLinkFragment call() throws Exception {
                MockBankLinkFragment fragment = new MockBankLinkFragment();
                fragment.setBankLinkData(data);
                return fragment;
            }
        });
    }
}
