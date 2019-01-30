package com.cardService.payment;

public class TransactionException extends Exception {
    public TransactionException(String error) {
        super(error);
    }
}
