package com.infora.ledger.data;

import android.net.Uri;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;

import com.infora.ledger.BanksContract;

/**
 * Created by jenya on 30.05.15.
 */
public class BanksContentProviderTest extends ProviderTestCase2<BanksContentProvider> {

    private MockContentResolver resolver;

    public BanksContentProviderTest() {
        super(BanksContentProvider.class, BanksContentProvider.AUTHORITY);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        resolver = getMockContentResolver();
    }

    public void testGetType() throws Exception {
        Uri itemsUrl = Uri.parse("content://" + BanksContract.AUTHORITY + "/banks/bank-links");
        assertEquals(BanksContentProvider.BANK_LINKS_LIST_TYPE, resolver.getType(itemsUrl));

        Uri itemUrl = Uri.parse("content://" + BanksContract.AUTHORITY + "/banks/bank-links/100");
        assertEquals(BanksContentProvider.BANK_LINKS_ITEM_TYPE, resolver.getType(itemUrl));
    }
}