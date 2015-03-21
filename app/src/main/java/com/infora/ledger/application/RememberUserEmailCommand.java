package com.infora.ledger.application;

/**
 * Created by jenya on 21.03.15.
 */
public class RememberUserEmailCommand {
    private String email;

    public RememberUserEmailCommand(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
