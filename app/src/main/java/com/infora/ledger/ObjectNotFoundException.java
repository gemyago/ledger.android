package com.infora.ledger;

/**
 * Created by jenya on 01.03.15.
 */
public class ObjectNotFoundException extends RuntimeException {
    public ObjectNotFoundException() {
    }

    public ObjectNotFoundException(String detailMessage) {
        super(detailMessage);
    }
}
