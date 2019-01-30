package com.cardService.payment;

public class TransactionOrigin {
    private String correlationId;
    private String from;
    public TransactionOrigin(String from, String correlation){
        this.from = from;
        this.correlationId = correlation;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public String getFrom() {
        return from;
    }
}
