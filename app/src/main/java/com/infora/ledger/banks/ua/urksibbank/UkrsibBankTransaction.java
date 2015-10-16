package com.infora.ledger.banks.ua.urksibbank;

import android.util.Base64;

import com.infora.ledger.TransactionContract;
import com.infora.ledger.banks.BankTransaction;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.data.PendingTransaction;
import com.infora.ledger.support.Dates;

import org.apache.commons.codec.digest.DigestUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jenya on 06.07.15.
 */
public class UkrsibBankTransaction implements BankTransaction {
    //http://www.theswiftcodes.com/ukraine/khabua2k/
    public static final String BIC = "KHABUA2K";
    private static final DateFormat DATE_FORMAT_FOR_TRANSACTION_ID = new SimpleDateFormat("yyyyMMdd");

    //This field is used to indicate unique transactions with the same amount for the same date
    public int sequence;

    public Date trandate;
    public Date commitDate;
    public String authCode;
    public String description;
    public String currency;
    public String amount;
    public String accountAmount;

    public UkrsibBankTransaction setSequence(int sequence) {
        this.sequence = sequence;
        return this;
    }

    public UkrsibBankTransaction setTrandate(Date trandate) {
        this.trandate = trandate;
        return this;
    }

    public UkrsibBankTransaction setCommitDate(Date commitDate) {
        this.commitDate = commitDate;
        return this;
    }

    public UkrsibBankTransaction setAuthCode(String authCode) {
        this.authCode = authCode;
        return this;
    }

    public UkrsibBankTransaction setDescription(String description) {
        this.description = description;
        return this;
    }

    public UkrsibBankTransaction setCurrency(String currency) {
        this.currency = currency;
        return this;
    }

    public UkrsibBankTransaction setAmount(String amount) {
        this.amount = amount;
        return this;
    }

    public UkrsibBankTransaction setAccountAmount(String accountAmount) {
        this.accountAmount = accountAmount;
        return this;
    }

    @Override
    public PendingTransaction toPendingTransaction(BankLink bankLink) {
        int typeId = accountAmount.startsWith("-") ? TransactionContract.TRANSACTION_TYPE_EXPENSE : TransactionContract.TRANSACTION_TYPE_INCOME;

        StringBuilder transactionIdBuilder = new StringBuilder()
                .append(bankLink.accountId).append(trandate.getTime()).append(amount.replace(" ", "").replace("-", "").replace(".", "P"));
        if(sequence > 0) transactionIdBuilder.append("S").append(sequence);

        //Using sha256 and Base64 to have transactionId no longer than 50 chars.
        String transactionId = Base64.encodeToString(DigestUtils.sha256(transactionIdBuilder.toString()), Base64.NO_PADDING + Base64.NO_WRAP + Base64.URL_SAFE);

        String actualAmount = accountAmount.replace(" ", "").replace("-", "");
        return new PendingTransaction(
                transactionId,
                actualAmount,
                description,
                false,
                false,
                trandate,
                BIC).setAccountId(bankLink.accountId).setTypeId(typeId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UkrsibBankTransaction that = (UkrsibBankTransaction) o;

        if (!Dates.areEqual(trandate, that.trandate))
            return false;
        if (!Dates.areEqual(commitDate, that.commitDate))
            return false;
        if (authCode != null ? !authCode.equals(that.authCode) : that.authCode != null)
            return false;
        if (description != null ? !description.equals(that.description) : that.description != null)
            return false;
        if (currency != null ? !currency.equals(that.currency) : that.currency != null)
            return false;
        if (amount != null ? !amount.equals(that.amount) : that.amount != null) return false;
        return !(accountAmount != null ? !accountAmount.equals(that.accountAmount) : that.accountAmount != null);

    }

    @Override
    public int hashCode() {
        int result = trandate != null ? trandate.hashCode() : 0;
        result = 31 * result + (commitDate != null ? commitDate.hashCode() : 0);
        result = 31 * result + (authCode != null ? authCode.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (currency != null ? currency.hashCode() : 0);
        result = 31 * result + (amount != null ? amount.hashCode() : 0);
        result = 31 * result + (accountAmount != null ? accountAmount.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "UkrsibBankTransaction{" +
                "trandate='" + trandate + '\'' +
                ", commitDate='" + commitDate + '\'' +
                ", authCode='" + authCode + '\'' +
                ", description='" + description + '\'' +
                ", currency='" + currency + '\'' +
                ", amount='" + amount + '\'' +
                ", accountAmount='" + accountAmount + '\'' +
                '}';
    }
}
