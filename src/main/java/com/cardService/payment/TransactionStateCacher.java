package com.cardService.payment;

import redis.clients.jedis.Jedis;

import java.util.Set;

public class TransactionStateCacher {
    private final Jedis _jedis;
    public TransactionStateCacher(){
        _jedis = new Jedis("localhost");
    }

    public void create(RecurringPaymentResult result,
                       String correlationId,
                       String replyTo,
                       String redirectOnError,
                       String redirectOnOk,
                       String expirationDate){
        //Create the transaction cache
        _jedis.set(getKey(result.getTransactionId(), "correlationId"), correlationId);
        _jedis.set(getKey(result.getTransactionId(), "from"), replyTo);
        if(!result.isRegistrationPayment() && result.isOk()){
            _jedis.set(getKey(result.getTransactionId(), "state"), TransactionState.Resolved.toString());
        }else{
            _jedis.set(getKey(result.getTransactionId(), "state"), TransactionState.Open.toString());
        }
        _jedis.set(getKey(result.getTransactionId(), "redirectOnError"), redirectOnError);
        _jedis.set(getKey(result.getTransactionId(), "redirectOnOk"), redirectOnOk);
        _jedis.set(getKey(result.getTransactionId(), "expirationDate"), expirationDate);
        _jedis.set(getKey(result.getTransactionId(), "isRegistrationPayment"), result.isRegistrationPayment() ? "true" : "false");
        String recurringId = result.getRecurringId();
        //If it's a recurring one, log it in it's registered transaction
        if(recurringId!=null && recurringId.length()>0){
            _jedis.sadd("transactions:recurring:" + result.getRecurringId(), result.getTransactionId());
        }
    }

    public String getTransactionExpiry(String transactionId) {
        return _jedis.get(getKey(transactionId, "expirationDate"));
    }

    /**
     * Gets the transaction id's linked to a recurring payment.
     * @param recurringPaymentId
     * @return
     */
    public Set<String> getRecurringTransactions(String recurringPaymentId){
        return _jedis.smembers("transactions:recurring:" + recurringPaymentId);
    }

    /**
     * Marks a transaction as finished
     * @param transactionId
     */
    public void closeSuccessfull(String transactionId) {
        _jedis.set(getKey(transactionId, "state"), TransactionState.Resolved.toString());
    }

    public void closeFailed(String transactionId, String error, TransactionResult statusResult) {
        _jedis.set(getKey(transactionId, "state"), TransactionState.Failed.toString());
        _jedis.set(getKey(transactionId, "error"), error);
        _jedis.set(getKey(transactionId, "errorState"), statusResult.getResult().toString());
    }

    private String getKey(String transactionId, String property){
        return String.format("transactions:%s:%s", transactionId, property);
    }

    /**
     *
     * @param transactionId
     * @return
     */
    public TransactionOrigin getTransactionOriginInfo(String transactionId) {
        String correlationId = _jedis.get(getKey(transactionId, "correlationId"));
        if(correlationId==null || correlationId.length()==0) return null;
        String from = _jedis.get(getKey(transactionId, "from"));
        if(from==null || from.length()==0) return null;
        TransactionOrigin orig = new TransactionOrigin(from, correlationId);
        return orig;
    }


    /**
     * Gets the url to which the client is redirected after a transaction.
     * @param transactionId
     * @return
     */
    public TransactionRedirections getRedirectionsForTransaction(String transactionId) {
        String onFailed = _jedis.get(getKey(transactionId, "redirectOnError"));
        String onOk = _jedis.get(getKey(transactionId, "redirectOnOk"));
        TransactionRedirections output = new TransactionRedirections(onFailed, onOk);
        return output;
    }

}
