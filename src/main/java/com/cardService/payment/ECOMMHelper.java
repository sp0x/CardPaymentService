package com.cardService.payment;

import com.cardService.Config;
import lv.tietoenator.cs.ecomm.merchant.Merchant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.Properties;

public class ECOMMHelper
{
    private final Merchant merch;
    private final String clientIp;
    private final Properties props;
    private IBankResponseParser ecomParser;
    private final TransactionStateCacher _transactionStateCacher;
    private String currency;
    private RecurringPaymentResult.Factory _resultFactory;
    private static final Logger logger = LoggerFactory.getLogger(ECOMMHelper.class);
    private boolean _loggingEnabled;

    public ECOMMHelper(String clientIp, Merchant merchant, Properties props){
        this.merch = merchant;
        this.clientIp = clientIp;
        this.props = props;
        this.ecomParser = new ECOMMResponseParser();
        this.currency = Config.getCurrency();
        _resultFactory = new RecurringPaymentResult.Factory(props);
        _transactionStateCacher = new TransactionStateCacher();
    }

    public void setResponseParser(IBankResponseParser parser){
        this.ecomParser = parser;
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
    public RecurringPaymentResult initializeRecurring(String paymentId,
                                                      String amount,
                                                      String clientIp,
                                                      String description,
                                                      String language,
                                                      String recurringPaymentId,
                                                      String expires,
                                                      Properties recurringProperties) throws TransactionException {
        Payment p = createPayment(paymentId);
        p.setDescription(description);
        String recurringResult = this.merch.startSMSTransRP(amount, currency, clientIp, p.getDescription(),
                language, recurringPaymentId, expires, props);
       // String rp_registry = this.merch.registerRP(currency, clientIp, p.getDescription(), language, recurringPaymentId, expires, props);
        if(_loggingEnabled){
            logger.info("InitialRecurring Payment response: " + recurringResult);
        }
        //System.out.println("RP reg: " + rp_registry);
        Map<String,String> parsedResult = this.ecomParser.parse(recurringResult);
        if(parsedResult.containsKey("error")){
            throw new TransactionException(parsedResult.get("error"));
        }
        RecurringPaymentResult result = _resultFactory.fromRecurringResult(parsedResult, true);
        if(recurringPaymentId==null && result.getTransactionId()!=null && result.getResult() == RecurringPaymentResultType.Ok){
            recurringPaymentId = result.getTransactionId();
        }
        parsedResult.put("recurringPaymentId", recurringPaymentId);
        //System.out.println("Recurring id: " + recurringPaymentId);
        ///System.out.println("Expires: " + expires);
        return result;
    }

    /**
     * Makes a recurring payment request.
     * @param recurringPaymentId
     * @param amount
     * @param clientIp
     * @return
     */
    public RecurringPaymentResult makeRecurring(String recurringPaymentId,
                                                String amount,
                                                String clientIp,
                                                String description,
                                                String params) throws TransactionException {
        Payment p = createPayment(recurringPaymentId);
        p.setDescription(description);
        //Set<String> transactions = _transactionStateCacher.getRecurringTransactions(recurringPaymentId);
        //String firstTransaction = transactions.stream().findFirst().get();
        // String expires = _transactionStateCacher.getTransactionExpiry(firstTransaction);
        //Properties recurringProps = new Properties();
        //recurringProps.setProperty("rec_pmnt_id", recurringPaymentId);
        //recurringProps.setProperty("language", Config.getDefaultLanguage());
        //recurringProps.setProperty("perspayee_expiry", expires);
        //System.out.println("Expires: " + expires);

        String recurringResult = this.merch.makeRP(recurringPaymentId, amount, currency, clientIp, p.getDescription(), null);
        if(_loggingEnabled){
            logger.info("Recurring id: " + recurringPaymentId);
            logger.info("Recurring Payment response: " + recurringResult);
        }

        Map<String,String> parsedResult = this.ecomParser.parse(recurringResult);
        parsedResult.put("recurringPaymentId", recurringPaymentId);
        if(parsedResult.containsKey("error")){
            throw new TransactionException(parsedResult.get("error"));
        }
        return _resultFactory.fromRecurringResult(parsedResult, false);
    }

    public TransactionResult issueRefund(String transactionId){
        String result = this.merch.refund(transactionId);
        Map<String, String> parsedResult = this.ecomParser.parse(result);
        TransactionResultType restype = parseTransactionResult(parsedResult.get("RESULT"));
        return TransactionResult.fromResult(restype);
    }

    public TransactionResult getTransactionStatus(String transactionId, boolean isRecurring){
        String transactionResult = this.merch.getTransResult(transactionId, clientIp);

        Map<String,String> result = this.ecomParser.parse(transactionResult);
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

    public void enableLogging(boolean enabled) {
        _loggingEnabled = enabled;
    }
}