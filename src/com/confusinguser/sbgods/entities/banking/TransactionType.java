package com.confusinguser.sbgods.entities.banking;

public enum TransactionType {
    WITHDRAW,
    DEPOSIT,
    NONE;

    public static TransactionType getType(String action) {
        switch (action) {
            case "DEPOSIT":
                return DEPOSIT;
            case "WITHDRAW":
                return WITHDRAW;
            default:
                return NONE;
        }
    }
}
