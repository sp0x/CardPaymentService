package bg.icafe.payment;

import bg.icafe.*;
import lv.tietoenator.cs.ecomm.merchant.Merchant;

import java.util.Map;
import java.util.Objects;
import java.util.Properties;

public class ECOMMHelper
{
    private final Merchant merch;
    private final String clientIp;
    private final Properties props;
    private final ECOMMResponseParser parser;
    private String currency;
    private RecurringPaymentResult.Factory _resultFactory;

    public ECOMMHelper(String clientIp, Merchant merchant, Properties props){
        this.merch = merchant;
        this.clientIp = clientIp;
        this.props = props;
        this.parser = new ECOMMResponseParser();
        this.currency = Config.getCurrency();
        _resultFactory = new RecurringPaymentResult.Factory(props);
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    private Payment createPayment(String id){
        Payment p = new Payment(id);
        return p;
    }

    /**
     * Initialize a recurring transaction
     * @param paymentId
     * @param amount
     * @param clientIp
     * @param description
     * @return
     */
    public RecurringPaymentResult initializeRecurring(String paymentId, String amount, String clientIp, String description) throws TransactionException {
        Payment p = createPayment(paymentId);
        p.setDescription(description);
        String recurringResult = this.merch.startSMSTrans(amount, currency, clientIp, description);
        //System.out.println("Payment response: " + recurringResult);
        Map<String,String> parsedResult = this.parser.parse(recurringResult);
        if(parsedResult.containsKey("error")){
            throw new TransactionException(parsedResult.get("error"));
        }
        RecurringPaymentResult result = _resultFactory.fromRecurringResult(parsedResult, true);
        return result;
    }

    /**
     * Makes a recurring payment request.
     * @param recurringId
     * @param amount
     * @param clientIp
     * @return
     */
    public RecurringPaymentResult makeRecurring(String recurringId, String amount, String clientIp, String description){
        Payment p = createPayment(recurringId);
        p.setDescription(description);
        String currency = Config.getCurrency();
        String recurringResult = this.merch.makeRP(recurringId, amount, currency, clientIp, description, props);
        Map<String,String> result = this.parser.parse(recurringResult);
        return _resultFactory.fromRecurringResult(result, false);
    }

    public TransactionResult issueRefund(String transactionId){
        String result = this.merch.refund(transactionId);
        Map<String, String> parsedResult = this.parser.parse(result);
        TransactionResultType restype = parseTransactionResult(parsedResult.get("RESULT"));
        return TransactionResult.fromResult(restype);
    }

    public TransactionResult getTransactionStatus(String transactionId, boolean isRecurring){
        String transactionResult = this.merch.getTransResult(transactionId, clientIp);
        Map<String,String> result = this.parser.parse(transactionResult);
        return (TransactionResult) TransactionResult.fromResult(result, isRecurring);
    }



    public void delete(String recurringId){
        this.merch.deleteRecurring(recurringId, props);
    }

    private static TransactionResultType parseTransactionResult(String tres){
        if(Objects.equals(tres, "OK")) return TransactionResultType.Ok;
        else if(Objects.equals(tres, "FAILED")) return TransactionResultType.Failed;
        else return TransactionResultType.Unknown;
    }

}