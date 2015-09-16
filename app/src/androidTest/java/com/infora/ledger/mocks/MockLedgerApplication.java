package com.infora.ledger.mocks;

import android.app.Application;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ProviderInfo;
import android.test.mock.MockContentResolver;

import com.infora.ledger.R;
import com.infora.ledger.application.di.InjectorProvider;
import com.infora.ledger.mocks.di.DaggerMockApplicationComponent;
import com.infora.ledger.mocks.di.TestApplicationModule;
import com.infora.ledger.mocks.di.TestDependenciesInjector;

/**
 * Created by jenya on 13.04.15.
 */
public class MockLedgerApplication extends Application implements InjectorProvider<TestDependenciesInjector> {
    public MockContentResolver mockContentResolver;
    private TestDependenciesInjector injector;
    private InjectorModuleInit injectorModuleInit;

    public MockLedgerApplication(Context baseContext) {
        attachBaseContext(baseContext);
        setTheme(R.style.AppTheme);
    }

    public MockLedgerApplication withInjectorModuleInit(InjectorModuleInit init) {
        this.injectorModuleInit = init;
        return this;
    }

    @Override
    public TestDependenciesInjector injector() {
        if (injector == null) {
            TestApplicationModule module = new TestApplicationModule(this);
            if (injectorModuleInit != null) injectorModuleInit.init(module);
            injector = DaggerMockApplicationComponent.builder()
                    .testApplicationModule(module)
                    .build();
        }
        return injector;
    }

    @Override
    public Context getApplicationContext() {
        return this;
    }

    @Override
    public ContentResolver getContentResolver() {
        if (mockContentResolver != null) return mockContentResolver;
        return super.getContentResolver();
    }

    public MockLedgerApplication withMockContentProvider(String authority, ContentProvider provider) {
        if (mockContentResolver == null) mockContentResolver = new MockContentResolver();
        provider.attachInfo(getBaseContext(), new ProviderInfo());
        mockContentResolver.addProvider(authority, provider);
        return this;
    }

    public interface InjectorModuleInit {
        void init(TestApplicationModule module);
    }
}
