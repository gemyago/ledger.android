package com.infora.ledger.banks;

import com.infora.ledger.TransactionContract;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jenya on 23.05.15.
 */
public class PrivatBankTransaction {
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

    @Override
    public String toString() {
        return "PrivatBankTransaction{" +
                "card='" + card + '\'' +
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
