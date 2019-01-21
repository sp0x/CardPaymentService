package bg.icafe;

import java.util.Map;

public class RecurringPaymentResult
{
    private String transactionId;
    private RecurringPaymentResultType result;
    private String resultCode;
    private String rrn;
    private String approvalCode;
    private boolean isInitial;

    public RecurringPaymentResult(){

    }

    public RecurringPaymentResultType getResult() {
        return result;
    }

    public String getTransactionId(){
        return this.transactionId;
    }

    public String getResultCode() {
        return resultCode;
    }

    public String getRrn() {
        return rrn;
    }

    public String getApprovalCode() {
        return approvalCode;
    }

    public boolean isInitial() {
        return isInitial;
    }

    /**
     * Parses a recurring payment request.
     * @param res
     * @param isInitial
     * @return
     */
    public static RecurringPaymentResult fromRecurringResult(Map<String,String> res, boolean isInitial) {
        if(res==null){
            return null;
        }
        String transactionId = res.get("TRANSACTION_ID");
        String result = res.get("RESULT");
//        for (String name: res.keySet()){
//            String key =name.toString();
//            String value = res.get(name).toString();
//            System.out.println(key + " " + value);
//        }

        String resultCode = res.get("RESULT_CODE");
        String rrn = res.get("RRN");
        String approvalCode = res.get("APPROVAL_CODE");
        RecurringPaymentResult paymentResult = new RecurringPaymentResult();
        paymentResult.transactionId = transactionId;
        paymentResult.resultCode = resultCode;
        paymentResult.rrn = rrn;
        paymentResult.approvalCode = approvalCode;
        paymentResult.result = parseRecurringResultType(result);
        paymentResult.isInitial = isInitial;
        return paymentResult;
    }

    public static RecurringPaymentResultType parseRecurringResultType(String result){
        if(result!=null && result.equals("OK")) return RecurringPaymentResultType.Ok;
        else  return RecurringPaymentResultType.Failed;
    }
}