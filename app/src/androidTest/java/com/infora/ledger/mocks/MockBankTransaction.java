package com.infora.ledger.mocks;

import com.infora.ledger.TransactionContract;
import com.infora.ledger.banks.BankTransaction;
import com.infora.ledger.banks.ua.privatbank.Privat24Transaction;
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
public class MockBankTransaction implements BankTransaction {

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

    public MockBankTransaction() {
    }

    public MockBankTransaction setCard(String card) {
        this.card = card;
        return this;
    }

    public MockBankTransaction setTrandate(String trandate) {
        this.trandate = trandate;
        return this;
    }

    public MockBankTransaction setTrantime(String trantime) {
        this.trantime = trantime;
        return this;
    }

    public MockBankTransaction setAmount(String amount) {
        this.amount = amount;
        return this;
    }

    public MockBankTransaction setCardamount(String cardamount) {
        this.cardamount = cardamount;
        return this;
    }

    public BankTransaction setRest(String rest) {
        this.rest = rest;
        return this;
    }

    public MockBankTransaction setTerminal(String terminal) {
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
        String lastDigitsOfCard = card.substring(card.length() - 4, card.length());
        Matcher amountMatcher = AMOUNT_PATTERN.matcher(amount);
        amountMatcher.find();
        return Privat24Transaction.PRIVATBANK_BIC + lastDigitsOfCard + trandate.replace("-", "") + trantime.replace(":", "") + amountMatcher.group(1).replace(".", "P");
    }

    @Override
    public PendingTransaction toPendingTransaction(BankLink bankLink) {
        return new PendingTransaction()
                .setTypeId(getTypeId())
                .setAccountId(bankLink.accountId)
                .setTransactionId(getTransactionId())
                .setAmount(getAmount())
                .setComment(terminal + " " + description)
                .setTimestamp(getDate())
                .setBic(bankLink.bic);
    }

    @Override
    public String toString() {
        return "MockBankTransaction{" +
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
