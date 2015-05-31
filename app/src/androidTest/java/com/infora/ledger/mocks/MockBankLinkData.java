package com.infora.ledger.mocks;

/**
 * Created by jenya on 31.05.15.
 */
public class MockBankLinkData {
    public String login;
    public String password;

    public MockBankLinkData(String login, String password) {
        this.login = login;
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MockBankLinkData that = (MockBankLinkData) o;

        if (login != null ? !login.equals(that.login) : that.login != null) return false;
        return !(password != null ? !password.equals(that.password) : that.password != null);

    }

    @Override
    public int hashCode() {
        int result = login != null ? login.hashCode() : 0;
        result = 31 * result + (password != null ? password.hashCode() : 0);
        return result;
    }
}
