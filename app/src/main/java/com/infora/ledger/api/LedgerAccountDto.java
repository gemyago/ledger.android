package com.infora.ledger.api;

import com.google.gson.annotations.SerializedName;

/**
 * Created by jenya on 31.05.15.
 */
public class LedgerAccountDto {
    @SerializedName("aggregate_id")
    public String id;
    @SerializedName("name")
    public String name;

    public LedgerAccountDto() {
    }

    public LedgerAccountDto(String id, String name) {
        this.id = id;
        this.name = name;
    }
}
