package com.infora.ledger.mocks;

import com.infora.ledger.api.AuthenticityToken;
import com.infora.ledger.api.LedgerAccountDto;
import com.infora.ledger.api.LedgerApi;
import com.infora.ledger.api.PendingTransactionDto;
import com.infora.ledger.data.PendingTransaction;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit.http.Field;

/**
 * Created by jenya on 25.03.15.
 */
public class MockLedgerApi implements LedgerApi {
    private ArrayList<PendingTransactionDto> pendingTransactions;
    private ArrayList<ReportPendingTransactionArgs> reportedTransactions = new ArrayList<ReportPendingTransactionArgs>();
    private ArrayList<AdjustPendingTransactionArgs> adjustTransactions = new ArrayList<AdjustPendingTransactionArgs>();
    private ArrayList<String> rejectedPendingTrasnsactions = new ArrayList<String>();
    private String authenticatedGoogleIdToken;
    private AuthenticatingByTokenCallback authenticatingByTokenCallback;
    private AuthenticityToken authenticatedAuthenticityToken;
    private List<LedgerAccountDto> accounts;

    public ArrayList<ReportPendingTransactionArgs> getReportedTransactions() {
        return reportedTransactions;
    }

    public ArrayList<AdjustPendingTransactionArgs> getAdjustTransactions() {
        return adjustTransactions;
    }

    public ArrayList<String> getRejectedPendingTrasnsactions() {
        return rejectedPendingTrasnsactions;
    }

    public String getAuthenticatedGoogleIdToken() {
        return authenticatedGoogleIdToken;
    }

    public void setAuthenticatedAuthenticityToken(AuthenticityToken authenticatedAuthenticityToken) {
        this.authenticatedAuthenticityToken = authenticatedAuthenticityToken;
    }

    public void setAuthenticatingByTokenCallback(AuthenticatingByTokenCallback authenticatingByTokenCallback) {
        this.authenticatingByTokenCallback = authenticatingByTokenCallback;
    }

    @Override
    public AuthenticityToken authenticateByIdToken(@Field("google_id_token") String googleIdToken) {
        if(authenticatingByTokenCallback != null) authenticatingByTokenCallback.authenticating(googleIdToken);
        this.authenticatedGoogleIdToken = googleIdToken;
        return authenticatedAuthenticityToken;
    }

    public void setAccounts(List<LedgerAccountDto> accounts) {
        this.accounts = accounts;
    }

    @Override
    public List<LedgerAccountDto> getAccounts() {
        return accounts;
    }

    @Override
    public Void reportPendingTransaction(@Field("aggregate_id") String transactionId, @Field("amount") String amount, @Field("date") Date date, @Field("comment") String comment, @Field("accountId") String accountId) {
        reportedTransactions.add(new ReportPendingTransactionArgs(transactionId, amount, date, comment, accountId));
        return null;
    }

    @Override
    public Void adjustPendingTransaction(@Field("aggregate_id") String transactionId, @Field("amount") String amount, @Field("comment") String comment, @Field("accountId") String accountId) {
        adjustTransactions.add(new AdjustPendingTransactionArgs(transactionId, amount, comment, accountId));
        return null;
    }

    @Override
    public Void rejectPendingTransaction(@Field("aggregate_id") String transactionId) {
        rejectedPendingTrasnsactions.add(transactionId);
        return null;
    }

    @Override
    public List<PendingTransactionDto> getPendingTransactions() {
        return pendingTransactions;
    }

    public void setPendingTransactions(ArrayList<PendingTransactionDto> pendingTransactions) {
        this.pendingTransactions = pendingTransactions;
    }

    public interface AuthenticatingByTokenCallback {
        void authenticating(String googleIdToken);
    }

    public static class AdjustPendingTransactionArgs {
        public final String transactionId;
        public final String amount;
        public final String comment;
        public final String accountId;

        public AdjustPendingTransactionArgs(String transactionId,
                                            String amount,
                                            String comment,
                                            String accountId) {
            this.transactionId = transactionId;
            this.amount = amount;
            this.comment = comment;
            this.accountId = accountId;
        }
    }

    public static class ReportPendingTransactionArgs {
        public final String transactionId;
        public final String amount;
        public final String comment;
        public final String accountId;
        private final Date date;

        private ReportPendingTransactionArgs(String transactionId, String amount, Date date, String comment, String accountId) {
            this.transactionId = transactionId;
            this.amount = amount;
            this.comment = comment;
            this.accountId = accountId;
            this.date = date;
        }

        public String getTransactionId() {
            return transactionId;
        }

        public String getAmount() {
            return amount;
        }

        public String getComment() {
            return comment;
        }

        public Date getDate() {
            return date;
        }

        public PendingTransaction toTransaction() {
            return new PendingTransaction()
                    .setTransactionId(transactionId)
                    .setAmount(amount)
                    .setTimestamp(date)
                    .setComment(comment)
                    .setAccountId(accountId);
        }
    }
}
