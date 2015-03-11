package com.infora.ledger.api;

import com.google.gson.annotations.SerializedName;

/**
 * Created by jenya on 12.03.15.
 */
public class AuthenticityToken {
    @SerializedName("form_authenticity_token")
    private String value;

    public String getValue() {
        return value;
    }
}
