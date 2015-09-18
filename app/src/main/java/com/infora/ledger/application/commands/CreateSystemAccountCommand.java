package com.infora.ledger.application.commands;

/**
 * Created by jenya on 21.03.15.
 */
public class CreateSystemAccountCommand extends Command {
    private String email;

    public CreateSystemAccountCommand(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
