package com.infora.ledger.api;

import java.util.Date;
import java.util.List;

import retrofit.http.DELETE;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;

/**
 * Created by jenya on 11.03.15.
 */
public interface LedgerApi {
    @FormUrlEncoded
    @POST("/api/sessions.json")
    AuthenticityToken authenticateByIdToken(@Field("google_id_token") String googleIdToken);

    @GET("/accounts.json")

    List<LedgerAccountDto> getAccounts();

    @GET("/pending-transactions.json")
    List<PendingTransactionDto> getPendingTransactions();

    @FormUrlEncoded
    @POST("/pending-transactions")
    Void reportPendingTransaction(
            @Field("id") String transactionId,
            @Field("amount") String amount,
            @Field("date") Date date,
            @Field("comment") String comment,
            @Field("account_id") String accountId);

    @FormUrlEncoded
    @PUT("/pending-transactions/{id}")
    Void adjustPendingTransaction(
            @Path("id") String transactionId,
            @Field("amount") String amount,
            @Field("comment") String comment,
            @Field("account_id") String accountId);

    @DELETE("/pending-transactions/{id}")
    Void rejectPendingTransaction(@Path("id") String transactionId);
}
