package com.infora.ledger.application.synchronization;

import android.content.SyncResult;

import com.infora.ledger.banks.FetchException;
import com.infora.ledger.mocks.MockBankLinksService;

import junit.framework.TestCase;

/**
 * Created by mye on 9/23/2015.
 */
public class FetchBankLinksSynchronizationStrategyTest extends TestCase {

    private MockBankLinksService bankLinksService;
    private FetchBankLinksSynchronizationStrategy subject;
    private SyncResult syncResult;

    @Override protected void setUp() throws Exception {
        super.setUp();
        syncResult = new SyncResult();
        bankLinksService = new MockBankLinksService();
        subject = new FetchBankLinksSynchronizationStrategy(bankLinksService);
    }

    public void testSynchronize() throws Exception {
        final boolean[] allFetched = {false};
        bankLinksService.onFetchAllBankLinks = new MockBankLinksService.OnFetchAllBankLinks() {
            @Override public void call() throws FetchException {
                allFetched[0] = true;
            }
        };
        subject.synchronize(null, null, syncResult);
        assertTrue(allFetched[0]);
    }

    public void testSynchronizeWithError() throws Exception {
        final FetchException fetchException = new FetchException("Fetch failed");
        bankLinksService.onFetchAllBankLinks = new MockBankLinksService.OnFetchAllBankLinks() {
            @Override public void call() throws FetchException {
                throw fetchException;
            }
        };
        subject.synchronize(null, null, syncResult);
        assertEquals(1, syncResult.stats.numIoExceptions);
    }
}