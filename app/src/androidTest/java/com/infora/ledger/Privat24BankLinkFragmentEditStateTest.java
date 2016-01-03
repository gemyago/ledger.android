package com.infora.ledger;

import android.content.Intent;
import android.test.ActivityUnitTestCase;
import android.view.View;

import com.infora.ledger.api.DeviceSecret;
import com.infora.ledger.banks.ua.privatbank.Privat24BankLinkData;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.mocks.DummyBankLinkFragmentTestActivity;
import com.infora.ledger.mocks.MockLedgerApplication;
import com.infora.ledger.mocks.MockPrivat24BankService;
import com.infora.ledger.mocks.MockSubscriber;
import com.infora.ledger.mocks.di.TestApplicationModule;
import com.infora.ledger.ui.BankLinkFragment;
import com.infora.ledger.ui.privat24.EditBankLinkFragmentModeState;
import com.infora.ledger.ui.privat24.messages.AuthenticateWithOtpToRefreshAuthentication;
import com.infora.ledger.ui.privat24.messages.AuthenticationRefreshed;
import com.infora.ledger.ui.privat24.messages.RefreshAuthentication;
import com.infora.ledger.ui.privat24.messages.RefreshAuthenticationFailed;

import java.io.IOError;
import java.io.IOException;
import java.sql.SQLException;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;

/**
 * Created by jenya on 01.06.15.
 */
public class Privat24BankLinkFragmentEditStateTest extends ActivityUnitTestCase<DummyBankLinkFragmentTestActivity> {

    @Inject EventBus bus;

    private Privat24BankLinkFragment fragment;
    private DeviceSecret secret;
    private MockPrivat24BankService mockPrivat24BankService;
    private EditBankLinkFragmentModeState subject;
    private View refreshButton;

    public Privat24BankLinkFragmentEditStateTest() {
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
        fragment.setMode(BankLinkFragment.Mode.Edit);
        fragment.bus = bus;
        getInstrumentation().callActivityOnStart(getActivity());
        getActivity().getSupportFragmentManager().executePendingTransactions();
        subject = (EditBankLinkFragmentModeState) fragment.modeState;
        secret = DeviceSecret.generateNew();
        refreshButton = fragment.getView().findViewById(R.id.privat24_refresh_authentication);
    }

    public void testRefreshAuthenticationButtonClick() {
        final BankLink bankLink = new BankLink().setId(3432234).setLinkData(new Privat24BankLinkData(), secret);
        fragment.setBankLinkData(bankLink, secret);

        final MockSubscriber<RefreshAuthentication> refreshSubscriber =
                new MockSubscriber<>(RefreshAuthentication.class);
        bus.register(refreshSubscriber);

        refreshButton.callOnClick();
        assertEquals(3432234, refreshSubscriber.getEvent().bankLinkId);
        assertFalse(refreshButton.isEnabled());
    }

    public void testOnRefreshAuthentication() {
        final MockSubscriber<AuthenticationRefreshed> refreshedSubscriber =
                new MockSubscriber<>(AuthenticationRefreshed.class);
        bus.register(refreshedSubscriber);

        int bankLinkId = TestHelper.randomInt();
        subject.onEventBackgroundThread(new RefreshAuthentication(bankLinkId));
        assertEquals(bankLinkId, mockPrivat24BankService.refreshAuthenticationCall.bankLinkId);

        assertNotNull(refreshedSubscriber.getEvent());
    }

    public void testOnRefreshAuthenticationExceptionHandling() {
        final MockSubscriber<RefreshAuthenticationFailed> refreshedSubscriber =
                new MockSubscriber<>(RefreshAuthenticationFailed.class);
        bus.register(refreshedSubscriber);

        final SQLException exception = new SQLException();

        mockPrivat24BankService.onRefreshAuthentication = new MockPrivat24BankService.OnRefreshAuthentication() {
            @Override public void call(int bankLinkId) throws SQLException {
                throw exception;
            }
        };

        subject.onEventBackgroundThread(new RefreshAuthentication(TestHelper.randomInt()));

        assertSame(exception, refreshedSubscriber.getEvent().exception);
    }

    public void testOnAuthenticateWithOtpToRefreshAuthentication() {
        final AuthenticateWithOtpToRefreshAuthentication cmd = new AuthenticateWithOtpToRefreshAuthentication(
                TestHelper.randomString("operation-id"),
                TestHelper.randomString("otp"),
                new BankLink()
        );
        final boolean[] serviceCalled = {false};
        mockPrivat24BankService.onAuthenticateWithOtpToRefreshAuthentication = new MockPrivat24BankService.OnAuthenticateWithOtpToRefreshAuthentication() {
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

    public void testOnAuthenticateWithOtpToRefreshAuthenticationExceptionHandling() {
        final AuthenticateWithOtpToRefreshAuthentication cmd = new AuthenticateWithOtpToRefreshAuthentication(
                TestHelper.randomString("operation-id"),
                TestHelper.randomString("otp"),
                new BankLink()
        );
        final MockSubscriber<RefreshAuthenticationFailed> refreshedSubscriber =
                new MockSubscriber<>(RefreshAuthenticationFailed.class);
        bus.register(refreshedSubscriber);

        final IOException exception = new IOException();

        mockPrivat24BankService.onAuthenticateWithOtpToRefreshAuthentication = new MockPrivat24BankService.OnAuthenticateWithOtpToRefreshAuthentication() {
            @Override public void call(String operationId, String otp, BankLink bankLink) throws IOException {
                throw exception;
            }
        };
        subject.onEventBackgroundThread(cmd);
        assertSame(exception, refreshedSubscriber.getEvent().exception);
    }

    public void testOnRefreshAuthenticationFailed() {
        refreshButton.setEnabled(false);
        subject.onEventMainThread(new RefreshAuthenticationFailed(new Exception("Refresh failed")));
        assertTrue(refreshButton.isEnabled());
    }

    public void testOnAuthenticationRefreshed() {
        refreshButton.setEnabled(false);
        subject.onEventMainThread(new AuthenticationRefreshed());
        assertTrue(refreshButton.isEnabled());
    }
}