package com.infora.ledger.banks.ua.privatbank;

import com.infora.ledger.TransactionContract;
import com.infora.ledger.banks.BankTransaction;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.data.PendingTransaction;
import com.infora.ledger.support.ObfuscatedString;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jenya on 23.05.15.
 */
public class PrivatBankTransaction implements com.infora.ledger.banks.BankTransaction {
    public static final String PRIVATBANK_BIC = "PBANUA2X";

    private static final Pattern AMOUNT_PATTERN = Pattern.compile("-?(\\d+\\.?\\d*) \\w{2,3}");
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public String card;
    public String trandate;
    public String trantime;
    public String amount;
    public String cardamount;
    public String rest;
    public String terminal;
    public String description;

    public PrivatBankTransaction() {
    }

    public PrivatBankTransaction setCard(String card) {
        this.card = card;
        return this;
    }

    public PrivatBankTransaction setTrandate(String trandate) {
        this.trandate = trandate;
        return this;
    }

    public PrivatBankTransaction setTrantime(String trantime) {
        this.trantime = trantime;
        return this;
    }

    public BankTransaction setAmount(String amount) {
        this.amount = amount;
        return this;
    }

    public PrivatBankTransaction setCardamount(String cardamount) {
        this.cardamount = cardamount;
        return this;
    }

    public BankTransaction setRest(String rest) {
        this.rest = rest;
        return this;
    }

    public PrivatBankTransaction setTerminal(String terminal) {
        this.terminal = terminal;
        return this;
    }

    public BankTransaction setDescription(String description) {
        this.description = description;
        return this;
    }

    public Date getDate() {
        try {
            return DATE_FORMAT.parse(trandate + " " + trantime);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public String getAmount() {
        Matcher matcher = AMOUNT_PATTERN.matcher(cardamount);
        matcher.find();
        return matcher.group(1);
    }

    public int getTypeId() {
        return cardamount.startsWith("-") ? TransactionContract.TRANSACTION_TYPE_EXPENSE : TransactionContract.TRANSACTION_TYPE_INCOME;
    }

    public String getTransactionId() {
        return PRIVATBANK_BIC + card + trandate.replace("-", "") + trantime.replace(":", "") + getAmount().replace(".", "P");
    }

    @Override
    public PendingTransaction toPendingTransaction(BankLink bankLink) {
        return new PendingTransaction()
                .setAccountId(bankLink.accountId)
                .setTransactionId(getTransactionId())
                .setAmount(getAmount())
                .setComment(terminal + " " + description)
                .setTimestamp(getDate())
                .setBic(bankLink.bic);
    }

    @Override
    public String toString() {
        return "PrivatBankTransaction{" +
                "card='" + ObfuscatedString.value(card) + '\'' +
                ", trandate='" + trandate + '\'' +
                ", trantime='" + trantime + '\'' +
                ", amount='" + amount + '\'' +
                ", cardamount='" + cardamount + '\'' +
                ", rest='" + rest + '\'' +
                ", terminal='" + terminal + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
