package com.infora.ledger.application;

/**
 * Created by jenya on 10.03.15.
 */
public class RemoveTransactionCommand {
    private Long id;

    public RemoveTransactionCommand(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
