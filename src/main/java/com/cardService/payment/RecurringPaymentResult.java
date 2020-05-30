package com.cardService.payment;

import java.net.URLEncoder;
import java.util.Map;
import java.util.Properties;

public class RecurringPaymentResult
{
    private String transactionId;
    private String url;
    private RecurringPaymentResultType result;
    private String recurringId;
    private String resultCode;
    private String rrn;
    private String approvalCode;
    /**
     * Whether this payment result was from a recurring payment, or an initial one that's used for registration.
     */
    private boolean isRegistrationPayment;

    public RecurringPaymentResult(){

    }

    public String getUrl() {
        return url;
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

    /**
     * Whether this payment result was from a recurring payment, or an initial one that's used for registration.
     * @return
     */
    public boolean isRegistrationPayment() {
        return isRegistrationPayment;
    }

    public String getRecurringId() {
        return recurringId;
    }

    public boolean isOk() {
        return this.getResult()== RecurringPaymentResultType.Ok;
    }

    public static class Factory {
        private final Properties props;

        public Factory(Properties props) {
            this.props = props;
        }

        public RecurringPaymentResult fromRecurringResult(Map<String, String> res, boolean isInitial) {
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
            paymentResult.recurringId = res.get("recurringPaymentId");
            if(!res.containsKey("RESULT") && isInitial){
                paymentResult.result = RecurringPaymentResultType.Ok;
            }
            paymentResult.isRegistrationPayment = isInitial;
            paymentResult.url = getClientRedirectionUrl(transactionId);
            return paymentResult;
        }

        private RecurringPaymentResultType parseRecurringResultType(String result){
            if(result!=null && result.equals("OK")) return RecurringPaymentResultType.Ok;
            else  return RecurringPaymentResultType.Failed;
        }


        private String getClientRedirectionUrl(String transactionId){
            String clientUrl = props.getProperty("bank.server.clienturl").trim();
            String output = clientUrl + "?trans_id=" +  URLEncoder.encode(transactionId);
            return output;
        }
    }

}