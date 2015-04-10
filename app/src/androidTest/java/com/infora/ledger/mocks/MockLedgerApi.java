package com.infora.ledger.mocks;

import com.infora.ledger.api.AuthenticityToken;
import com.infora.ledger.api.LedgerApi;
import com.infora.ledger.api.PendingTransactionDto;

import java.util.ArrayList;
import java.util.Date;

import retrofit.http.Field;

/**
 * Created by jenya on 25.03.15.
 */
public class MockLedgerApi implements LedgerApi {
    private ArrayList<PendingTransactionDto> pendingTransactions;
    private ArrayList<ReportPendingTransactionArgs> reportedTransactions = new ArrayList<ReportPendingTransactionArgs>();
    private ArrayList<String> rejectedPendingTrasnsactions = new ArrayList<String>();

    public ArrayList<ReportPendingTransactionArgs> getReportedTransactions() {
        return reportedTransactions;
    }

    public ArrayList<String> getRejectedPendingTrasnsactions() {
        return rejectedPendingTrasnsactions;
    }

    @Override
    public AuthenticityToken authenticateByIdToken(@Field("google_id_token") String googleIdToken) {
        return null;
    }

    @Override
    public Void reportPendingTransaction(@Field("aggregate_id") String transactionId, @Field("amount") String amount, @Field("comment") String comment, @Field("date") Date date) {
        reportedTransactions.add(new ReportPendingTransactionArgs(transactionId, amount, comment, date));
        return null;
    }

    @Override
    public Void rejectPendingTransaction(@Field("aggregate_id") String transactionId) {
        rejectedPendingTrasnsactions.add(transactionId);
        return null;
    }

    @Override
    public ArrayList<PendingTransactionDto> getPendingTransactions() {
        return pendingTransactions;
    }

    public void setPendingTransactions(ArrayList<PendingTransactionDto> pendingTransactions) {
        this.pendingTransactions = pendingTransactions;
    }

    public static class ReportPendingTransactionArgs {
        private final String transactionId;
        private final String amount;
        private final String comment;
        private final Date date;

        private ReportPendingTransactionArgs(String transactionId, String amount, String comment, Date date) {
            this.transactionId = transactionId;
            this.amount = amount;
            this.comment = comment;
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
    }
}
