package com.infora.ledger;

import android.content.Intent;
import android.test.ActivityUnitTestCase;

import com.infora.ledger.api.DeviceSecret;
import com.infora.ledger.banks.ua.privatbank.Privat24BankLinkData;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.mocks.DummyBankLinkFragmentTestActivity;
import com.infora.ledger.mocks.MockLedgerApplication;
import com.infora.ledger.mocks.MockPrivat24BankService;
import com.infora.ledger.mocks.MockSubscriber;
import com.infora.ledger.mocks.di.TestApplicationModule;
import com.infora.ledger.ui.BankLinkFragment;
import com.infora.ledger.ui.privat24.AddBankLinkFragmentModeState;
import com.infora.ledger.ui.privat24.EditBankLinkFragmentModeState;
import com.infora.ledger.ui.privat24.messages.AuthenticateWithOtpAndCreateNewPrivat24Link;
import com.infora.ledger.ui.privat24.messages.AuthenticationRefreshed;
import com.infora.ledger.ui.privat24.messages.RefreshAuthentication;
import com.infora.ledger.ui.privat24.messages.RefreshAuthenticationFailed;

import java.sql.SQLException;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;

/**
 * Created by jenya on 01.06.15.
 */
public class Privat24BankLinkFragmentAddStateTest extends ActivityUnitTestCase<DummyBankLinkFragmentTestActivity> {

    @Inject EventBus bus;

    private Privat24BankLinkFragment fragment;
    private DeviceSecret secret;
    private MockPrivat24BankService mockPrivat24BankService;
    private AddBankLinkFragmentModeState subject;

    public Privat24BankLinkFragmentAddStateTest() {
        super(DummyBankLinkFragmentTestActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        final MockLedgerApplication app = new MockLedgerApplication(getInstrumentation().getTargetContext())
                .withInjectorModuleInit(new MockLedgerApplication.InjectorModuleInit() {
                    @Override public void init(TestApplicationModule module) {
                        mockPrivat24BankService = new MockPrivat24BankService();
                        module.privat24BankService = mockPrivat24BankService;
                    }
                });
        app.injector().inject(this);
        setActivityContext(app);
        startActivity(new Intent(getInstrumentation().getTargetContext(), DummyBankLinkFragmentTestActivity.class), null, null);
        getActivity().fragment = fragment = new Privat24BankLinkFragment();
        fragment.setMode(BankLinkFragment.Mode.Add);
        fragment.bus = bus;
        getInstrumentation().callActivityOnStart(getActivity());
        getActivity().getSupportFragmentManager().executePendingTransactions();
        subject = (AddBankLinkFragmentModeState) fragment.modeState;
        secret = DeviceSecret.generateNew();
    }

    public void testOnAuthenticateWithOtpAndCreateNewPrivat24Link() {
        final AuthenticateWithOtpAndCreateNewPrivat24Link cmd = new AuthenticateWithOtpAndCreateNewPrivat24Link(
                TestHelper.randomString("operation-id"),
                TestHelper.randomString("otp"),
                new BankLink());
        final boolean[] serviceCalled = {false};
        mockPrivat24BankService.onAuthenticateWithOtpAndCreateNewLink = new MockPrivat24BankService.OnAuthenticateWithOtpAndCreateNewLink() {
            @Override public void call(String operationId, String otp, BankLink bankLink) {
                assertEquals(cmd.operationId, operationId);
                assertEquals(cmd.otp, otp);
                assertSame(cmd.bankLink, bankLink);
                serviceCalled[0] = true;
            }
        };
        subject.onEventBackgroundThread(cmd);
        assertTrue(serviceCalled[0]);
    }
}