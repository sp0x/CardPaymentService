package bg.icafe.network.mq;

import bg.icafe.Config;
import bg.icafe.payment.ECOMMHelper;
import bg.icafe.payment.RecurringPaymentResult;
import com.rabbitmq.client.*;
import lv.tietoenator.cs.ecomm.merchant.Merchant;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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


    public static TransactionClient getInstance() throws Exception {
        if(_instance!=null) return _instance;
        Pair<Merchant, Properties> props = Config.getMerchantConfiguration();
        MqConfig mqconfig = Config.readMqConfiguration();
        Listener mqListener = new Listener(mqconfig);
        mqListener.connect(mqconfig.getPass());
        ECOMMHelper helper = new ECOMMHelper("", props.getLeft(), props.getRight());
        //TODO: Lock
        _instance = new TransactionClient(mqListener, helper);
        return _instance;
    }

    private TransactionClient(Listener base, ECOMMHelper helper) throws IOException {
        _base = base;
        _transactionHelper = helper;
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

    public RecurringPaymentResult handleTransactionRequest(String type, String recurringId, int amount, String ip, String description) throws Exception {
        //Handle the request
        RecurringPaymentResult result = null;
        if(type.equals("initial")){
            result = _transactionHelper.initializeRecurring(null, Integer.toString(amount), ip, description);
        }else if(type.equals("secondary")){
            if(recurringId==null || recurringId.length()==0){
                throw new Exception("Recurring id is missing.");
            }
            result = _transactionHelper.makeRecurring(recurringId, Double.toString(amount), ip, description);
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

        String type = bodyJson.getString("type");
        String description = bodyJson.getString("description");
        Object recId = bodyJson.get("recurringId");
        String recurringId = recId==null ? null : recId.toString();
        if(!bodyJson.has("ip")){
            throw new Exception("Client ip is required.");
        }else{
            String ip = bodyJson.getString("ip");
            int amount = bodyJson.getInt("amount");
            logger.info("Received transaction request [" + type + "] @" + ip + " - " + description);
            JSONObject reply = new JSONObject();
            try{
                RecurringPaymentResult result = handleTransactionRequest(type, recurringId, amount, ip, description);
                reply.put("url", result.getUrl());
                reply.put("transactionId", result.getTransactionId());
                reply.put("result", result.getResult());
                reply.put("resultCode", result.getResultCode());
            }catch(Exception ex){
                reply.put("resultCode", "err");
                reply.put("error", ex.getMessage());
            }

            sendReply(replyTo, reply, correlationId);
            channel.basicAck(deliveryTag, false);
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


    public void reportFailedTransaction(String transactionId, String error) {

    }

    public void reportSuccessfullTransaction(String transactionId) {

    }

}
