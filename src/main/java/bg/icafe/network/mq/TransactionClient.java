package bg.icafe.network.mq;

import bg.icafe.Config;
import bg.icafe.Helpers;
import bg.icafe.payment.*;
import com.rabbitmq.client.*;
import lv.tietoenator.cs.ecomm.merchant.Merchant;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 *
 */
public class TransactionClient
{
    private static final String QUEUE_TRANSACTIONS = "sales.transaction_request";
    private static final String EXCHANGE_SALES = "sales";

    private final Listener _base;
    private final ECOMMHelper _transactionHelper;
    private static TransactionClient _instance;
    private static final Logger logger = LoggerFactory.getLogger(TransactionClient.class);
    private final TransactionStateCacher _transactionCacher;
    private boolean _isTestMode=false;


    public static TransactionClient getInstance() throws Exception {
        if(_instance!=null) return _instance;
        Pair<Merchant, Properties> props = Config.getMerchantConfiguration();
        Properties generalSettings = Config.getGeneralSettings();

        MqConfig mqconfig = Config.readMqConfiguration();
        Listener mqListener = new Listener(mqconfig);
        mqListener.connect(mqconfig.getPass());
        ECOMMHelper helper = new ECOMMHelper("", props.getLeft(), props.getRight());
        //TODO: Lock
        _instance = new TransactionClient(mqListener, helper);
        _instance._isTestMode = generalSettings.getProperty("isTestmode").toLowerCase().equals("true");
        helper.enableLogging(_instance._isTestMode);
        return _instance;
    }

    private TransactionClient(Listener base, ECOMMHelper helper) throws IOException {
        _base = base;
        _transactionHelper = helper;
        _transactionCacher = new TransactionStateCacher();

        Channel chan = base.getChannel();
        chan.exchangeDeclare(EXCHANGE_SALES, BuiltinExchangeType.TOPIC, true);
        chan.queueDeclare(QUEUE_TRANSACTIONS, true, false, false, null);
        chan.queueBind(QUEUE_TRANSACTIONS, EXCHANGE_SALES, QUEUE_TRANSACTIONS);
        chan.basicConsume(QUEUE_TRANSACTIONS, false, new DefaultConsumer(chan){
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                super.handleDelivery(consumerTag, envelope, properties, body);
                try {
                    handleTransactionRequest(consumerTag, envelope, properties, body);
                } catch (Exception e) {
                    e.printStackTrace();
                    chan.basicAck(envelope.getDeliveryTag(), false);
                }
            }
        });
        /**
         * Workflow:
         * This service gets a payment request(with callback urls)
         * it issues it to the bank ECOMM
         * we get a transaction id and send it back to the caller.
         * The caller comes back after making/declining/timing out the purchase and calls us to finalize.
         * We finalize the transaction payment and log it.
         */
    }


    public ECOMMHelper getECOMM() {
        return _transactionHelper;
    }

    /**
     *
     * @param type
     * @param recurringId
     * @param amount
     * @param ip
     * @param description
     * @param expirationDate
     * @param params
     * @return
     * @throws Exception
     */
    public RecurringPaymentResult handleTransactionRequest(String type,
                                                           String recurringId,
                                                           int amount, String ip,
                                                           String description,
                                                           String expirationDate,
                                                           String params) throws Exception {
        //Handle the request
        RecurringPaymentResult result = null;
        if(type.equals("initial")){
            //SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            //Date date = new Date();
            result = _transactionHelper.initializeRecurring(null, Integer.toString(amount), ip, description,
                    Config.getDefaultLanguage(), recurringId, expirationDate, null);
        }else if(type.equals("secondary")){
            if(recurringId==null || recurringId.length()==0){
                throw new Exception("Recurring id is missing.");
            }
            result = _transactionHelper.makeRecurring(recurringId, Integer.toString(amount), ip, description, params);
        }
        if(result==null){
            throw new Exception("Transaction type not supported.");
        }
        return result;
    }


    /**
     *
     * @param consumerTag
     * @param envelope
     * @param properties
     * @param body
     * @throws Exception
     */
    private void handleTransactionRequest(String consumerTag,
                                          Envelope envelope,
                                          AMQP.BasicProperties properties,
                                          byte[] body) throws Exception {
        Channel channel = _base.getChannel();
        long deliveryTag = envelope.getDeliveryTag();
        String replyTo = properties.getReplyTo();
        String correlationId = properties.getCorrelationId();
        JSONObject bodyJson = new JSONObject(new String(body));

        String type = null;
        String description = null;
        Object redirectOnError = null;
        Object redirectOnOk = null;
        Object recId = null;
        Object paramsObj = null;
        String params = null;
        String recurringId = null;
        String expirationDate=null;
        JSONObject reply = new JSONObject();
        reply.put("type", "transactionCreation");
        Object objExpiration = null;
        try{
            type = bodyJson.getString("type");
            if(!bodyJson.has("expirationDate") && !type.equals("secondary"))  throw new Exception("expirationDate is required.");
            else{
                if(!type.equals("secondary")){
                    objExpiration = bodyJson.get("expirationDate");
                }
            }
            if((objExpiration==null || objExpiration.toString().length()==0) && !type.equals("secondary")){
                throw new Exception("expirationDate is required.");
            }
            expirationDate = objExpiration!=null ? objExpiration.toString() : "";
            description = bodyJson.getString("description");
            redirectOnError = bodyJson.has("redirectOnError") ? bodyJson.get("redirectOnError") : null;
            redirectOnOk = bodyJson.has("redirectOnOk") ? bodyJson.get("redirectOnOk") : null;
            recId = bodyJson.has("paymentId") ? bodyJson.get("paymentId"):null;
            paramsObj = bodyJson.has("params") ? bodyJson.get("params") : null;
            params = paramsObj!=null ? paramsObj.toString() : "";
            recurringId = recId==null ? null : recId.toString();
        }catch(Exception ex){
            fillExceptionReply(reply, ex);
            sendReply(replyTo, reply, correlationId);
            channel.basicAck(deliveryTag, false);
            return;
        }

        if(!bodyJson.has("ip")){
            throw new Exception("Client ip is required.");
        }else{
            String ip = bodyJson.getString("ip");
            int amount = bodyJson.getInt("amount");
            logger.info("Received transaction request [" + type + "] @" + ip + " - " + description);
            try{
                RecurringPaymentResult result = handleTransactionRequest(type, recurringId, amount, ip, description, expirationDate, params);
                reply.put("url", result.getUrl());
                reply.put("transactionId", result.getTransactionId());
                reply.put("result", result.getResult().toString().toLowerCase());
                reply.put("resultCode", result.getResultCode());
                reply.put("isRegistrationPayment", result.isRegistrationPayment());
                String transId = result.getTransactionId();
                if(transId!=null && transId.length()>0){
                    saveOpenTransaction(result, correlationId, replyTo, redirectOnError, redirectOnOk, expirationDate);
                }
            }catch(TransactionException transEx){
                fillExceptionReply(reply, transEx);
            }
            catch(Exception ex){
                fillExceptionReply(reply, ex);
            }
            sendReply(replyTo, reply, correlationId);
            channel.basicAck(deliveryTag, false);
        }

    }

    private void fillExceptionReply(JSONObject reply, Exception ex) {
        reply.put("result", "error");
        reply.put("resultCode", "err");
        reply.put("error", ex.getMessage());
        if(_isTestMode){
            reply.put("stack", Helpers.getStackTrace(ex));
        }
    }

    /**
     *
     * @param replyTo
     * @param reply
     * @throws IOException
     */
    private void sendReply(String replyTo, JSONObject reply, String correlationId) throws IOException {
        Channel chan = _base.getChannel();
        AMQP.BasicProperties props = new AMQP.BasicProperties();
        if(correlationId!=null) {
            props = props.builder().correlationId(correlationId).build();
        }
        byte[] body = reply.toString().getBytes(StandardCharsets.UTF_8);
        chan.basicPublish("", replyTo,props, body);
    }


    public void reportFailedTransaction(String transactionId, String error, TransactionResult statusResult) throws IOException {
        _transactionCacher.closeFailed(transactionId, error, statusResult);
        TransactionOrigin origin = _transactionCacher.getTransactionOriginInfo(transactionId);
        if(origin!=null){
            JSONObject reply = new JSONObject();
            reply.put("success", false);
            reply.put("error", error);
            reply.put("errorStatus", statusResult.getResult().toString());
            reply.put("transactionId", transactionId);
            reply.put("type", "transactionFinished");
            sendReply(origin.getFrom(), reply, origin.getCorrelationId());
        }
    }

    public void reportSuccessfullTransaction(String transactionId) throws IOException {
        TransactionOrigin origin = _transactionCacher.getTransactionOriginInfo(transactionId);
        _transactionCacher.closeSuccessfull(transactionId);
        if(origin!=null){
            JSONObject reply = new JSONObject();
            reply.put("success", true);
            reply.put("transactionId", transactionId);
            reply.put("type", "transactionFinished");
            sendReply(origin.getFrom(), reply, origin.getCorrelationId());
        }
    }


    /**
     * Saves an open transaction
     * @param result
     * @param correlationId
     * @param replyTo
     */
    private void saveOpenTransaction(RecurringPaymentResult result,
                                     String correlationId,
                                     String replyTo,
                                     Object redirectOnError,
                                     Object redirectOnOk,
                                     String expirationDate) {
        _transactionCacher.create(result, correlationId, replyTo, redirectOnError!=null ? redirectOnError.toString(): "",
                redirectOnOk!=null ? redirectOnOk.toString() : "", expirationDate);
    }

    public TransactionRedirections getRedirectionsForTransaction(String transactionId) {
        return _transactionCacher.getRedirectionsForTransaction(transactionId);
    }
}
