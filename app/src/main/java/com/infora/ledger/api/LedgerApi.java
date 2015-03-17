package com.infora.ledger.api;

import java.util.Date;

import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

/**
 * Created by jenya on 11.03.15.
 */
public interface LedgerApi {
    @FormUrlEncoded
    @POST("/api/sessions.json")
    AuthenticityToken authenticate(@Field("user[email]") String email, @Field("user[password]") String password);

    @FormUrlEncoded
    @POST("/pending-transactions")
    Void reportPendingTransaction(
            @Field("aggregate_id") String transactionId,
            @Field("amount") String amount,
            @Field("comment") String comment,
            @Field("date") Date date);
}