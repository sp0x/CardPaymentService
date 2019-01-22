package bg.icafe.payment;

import redis.clients.jedis.Jedis;

public class TransactionStateCacher {
    private final Jedis _jedis;
    public TransactionStateCacher(){
        _jedis = new Jedis("localhost");
    }

    public void create(RecurringPaymentResult result,
                       String correlationId,
                       String replyTo,
                       String redirectOnError,
                       String redirectOnOk){
        _jedis.set(getKey(result.getTransactionId(), "correlationId"), correlationId);
        _jedis.set(getKey(result.getTransactionId(), "from"), replyTo);
        _jedis.set(getKey(result.getTransactionId(), "state"), TransactionState.Open.toString());
        _jedis.set(getKey(result.getTransactionId(), "redirectOnError"), redirectOnError);
        _jedis.set(getKey(result.getTransactionId(), "redirectOnOk"), redirectOnOk);
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
