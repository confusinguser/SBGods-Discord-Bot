package com.confusinguser.sbgods.entities.banking;

import com.confusinguser.sbgods.SBGods;
import org.json.JSONObject;

public class BankTransaction {

    final double amount;
    final long timestamp;
    final TransactionType transactionType;
    String initiatorName;

    public BankTransaction(JSONObject json) {
        this.amount = json.getInt("amount");
        this.timestamp = json.getLong("timestamp");
        this.transactionType = TransactionType.getType(json.getString("action"));
        if (!json.getString("initiator_name").equals("Bank Interest")) {
            try {
                this.initiatorName = SBGods.getInstance().getUtil().stripColorCodes(json.getString("initiator_name").split(" ")[1]);
            } catch (IndexOutOfBoundsException e) {
                this.initiatorName = SBGods.getInstance().getUtil().stripColorCodes(json.getString("initiator_name"));
            }
        } else {
            this.initiatorName = "Bank Interest";
        }
    }

    public BankTransaction(double amount, long timestamp, TransactionType transactionType, String initiatorName) {
        this.amount = amount;
        this.timestamp = timestamp;
        this.transactionType = transactionType;
        this.initiatorName = initiatorName;
    }

    public double getAmount() {
        return amount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public TransactionType getType() {
        return transactionType;
    }

    public String getInitiatorName() {
        return initiatorName;
    }
}
