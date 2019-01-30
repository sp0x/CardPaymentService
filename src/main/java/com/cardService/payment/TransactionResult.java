package com.cardService.payment;

import java.util.Map;
import java.util.Objects;

public class TransactionResult
{
    private TransactionResultType result;
    private String code;
    private String rnn;
    private String recurringPaymentId;
    private String recurringExpiry;


    public TransactionResult(){

    }

    public String getCode() {
        return code;
    }

    public String getRnn() {
        return rnn;
    }

    public String getRecurringPaymentId() {
        return recurringPaymentId;
    }

    public String getRecurringExpiry() {
        return recurringExpiry;
    }
    public TransactionResultType getResult(){
        return this.result;
    }

    public static TransactionResult fromResult(Map<String, String> result, boolean isRecurring) {
        String transactionResult = result.get("RESULT");
        String code = result.get("RESULT_CODE");
        String rcc_pmt_id = result.get("RECC_PMNT_ID");
        String rcc_pmnt_expiry = result.get("RECC_PMNT_EXPIRY");
        String rnn = result.get("RNN");
        TransactionResult tres = new TransactionResult();
        tres.code = code;
        tres.result = TransactionResult.parseTransactionRequest(transactionResult, isRecurring);
        tres.rnn = rnn;
        tres.recurringPaymentId = rcc_pmt_id;
        tres.recurringExpiry = rcc_pmnt_expiry;
        return tres;
    }

    @Override
    public String toString() {
        return "TransactionResult{" +
                "result=" + result +
                ", code='" + code + '\'' +
                ", rnn='" + rnn + '\'' +
                ", recurringPaymentId='" + recurringPaymentId + '\'' +
                ", recurringExpiry='" + recurringExpiry + '\'' +
                '}';
    }

    public static TransactionResult fromResult(TransactionResultType restype) {
        TransactionResult res = new TransactionResult();
        res.result = restype;
        return res;
    }


    public static TransactionResultType parseTransactionRequest(String tres, boolean isRecurringSecondary)
    {
        if(Objects.equals(tres, "OK")) return TransactionResultType.Ok;
        else if(Objects.equals(tres, "FAILED")) return TransactionResultType.Failed;
        else if(Objects.equals(tres, "CREATED")) return TransactionResultType.Created;
        else if(Objects.equals(tres, "PENDING")) return TransactionResultType.Pending;
        else if(Objects.equals(tres, "DECLINED")) return TransactionResultType.Declined;
        else if(Objects.equals(tres, "REVERSED")) return TransactionResultType.Reversed;
        else if(Objects.equals(tres, "AUTOREVERSED")) return TransactionResultType.Autoreversed;
        else if(Objects.equals(tres, "TIMEOUT")) return TransactionResultType.Timeout;
        else return TransactionResultType.Unknown;
    }

}
