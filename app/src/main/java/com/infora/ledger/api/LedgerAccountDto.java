package com.infora.ledger.api;

/**
 * Created by jenya on 31.05.15.
 */
public class LedgerAccountDto {
    public String id;
    public String name;

    public LedgerAccountDto() {
    }

    public LedgerAccountDto(String id, String name) {
        this.id = id;
        this.name = name;
    }
}
