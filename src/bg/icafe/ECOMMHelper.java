package bg.icafe;

import lv.tietoenator.cs.ecomm.merchant.Merchant;

import java.net.URLEncoder;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

public class ECOMMHelper
{
    private final Merchant merch;
    private final String clientIp;
    private final Properties props;
    private final ECOMMResponseParser parser;

    public ECOMMHelper(String clientIp, Merchant merchant, Properties props){
        this.merch = merchant;
        this.clientIp = clientIp;
        this.props = props;
        this.parser = new ECOMMResponseParser();
    }

    private Payment createPayment(){
        Payment p = new Payment();
        return p;
    }

    public RecurringPaymentResult initializeRecurring(String amount, String clientIp){
        Payment p = createPayment();
        String description = p.getDescription();
        String currency = Config.getCurrency();
        String recurringResult = this.merch.startRP(Long.toString(p.getId()),amount, currency, clientIp, description, props);
        Map<String,String> parsedResult = this.parser.parse(recurringResult);
        return RecurringPaymentResult.fromRecurringResult(parsedResult, true);
    }

    public RecurringPaymentResult makeRecurring(String recurringId, String amount, String clientIp){
        Payment p = createPayment();
        String description = p.getDescription();
        String currency = Config.getCurrency();
        String recurringResult = this.merch.makeRP(recurringId, amount, currency, clientIp, description, props);
        Map<String,String> result = this.parser.parse(recurringResult);
        return RecurringPaymentResult.fromRecurringResult(result, false);
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

    public String getClientRedirectionUrl(String transactionId){
        String clientUrl = props.getProperty("bank.server.clienturl");
        String output = clientUrl + "?trans_id=" +  URLEncoder.encode(transactionId);
        return output;
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