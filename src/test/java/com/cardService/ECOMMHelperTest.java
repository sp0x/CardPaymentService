package com.cardService;


import com.cardService.payment.*;
import junit.framework.Assert;
import lv.tietoenator.cs.ecomm.merchant.Merchant;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertNotNull;
import static org.mockito.Mockito.*;

public class ECOMMHelperTest {

    private ECOMMHelper helper;
    private String test_ip = "85.85.85.85";

    @Before
    public void setUp() throws Exception {
        Properties mockProperties = mock(Properties.class);
        when((mockProperties.getProperty("keystore.file"))).thenReturn("");
        when((mockProperties.getProperty("keystore.type"))).thenReturn("JKS");
        when((mockProperties.getProperty("keystore.password"))).thenReturn("password");
        when((mockProperties.getProperty("bank.server.url"))).thenReturn("https://bank.com");
        when((mockProperties.getProperty("bank.server.clienturl"))).thenReturn("https://bank.com");
        when((mockProperties.getProperty("returnOkUrl"))).thenReturn("https://us.com/ok");
        when((mockProperties.getProperty("returnFailUrl"))).thenReturn("https://us.com/fail");
        ECOMMResponseParser mockBankResponseParser = mock(ECOMMResponseParser.class);
        Map<String, String> okResponse = new HashMap<String, String>();
        okResponse.put("TRANSACTION_ID", "123");
        okResponse.put("RESULT", "CREATED");
        okResponse.put("RESULT_CODE", "200");
        okResponse.put("recurringPaymentId", "");
        when(mockBankResponseParser.parse(anyString())).thenReturn(okResponse);

        Pair<Merchant, Properties> props = Config.getMerchantConfiguration(mockProperties);
        helper = new ECOMMHelper("127.0.0.1", props.getLeft(), mockProperties);
        helper.setResponseParser(mockBankResponseParser);
    }

    @Test
    public void initializeRecurring() {
        RecurringPaymentResult result = null;
        try {
            String expiry = "1019";
            result = helper.initializeRecurring(null, "1",test_ip, "Recurring payment init test.",
                    Config.getDefaultLanguage(), null, expiry, null);
        } catch (TransactionException e) {
            e.printStackTrace();
        }
        assertNotNull(result);
        assertNotNull(result.getTransactionId());
        assertNotNull(result.getUrl());
        assertEquals("", result.getRecurringId());
    }
//
//    @Test
//    public void makeRecurring() {
//        RecurringPaymentResult result = null;
//        try {
//            String expiry = "1019";
//            result = helper.initializeRecurring(null, "1",test_ip, "Recurring payment init test.",
//                    Config.getDefaultLanguage(), null, expiry, null);
//            String recurringId = result.getTransactionId();
//            RecurringPaymentResult res = helper.makeRecurring(recurringId, "100", "85.85.85.85", "Some description", null);
//            System.out.println(res.getResult());
//            System.out.println(res.getResultCode());
//        } catch (TransactionException e) {
//            e.printStackTrace();
//        }
//        assertNotNull(result);
//        assertNotNull(result.getTransactionId());
//        assertNotNull(result.getUrl());
//    }

    @Test
    public void getTransactionStatus() {
        RecurringPaymentResult result = null;
        try {
            String expiry = "1019";
            result = helper.initializeRecurring(null, "1",test_ip, "Recurring payment init test.", Config.getDefaultLanguage(),
                    null, expiry, null);
        } catch (TransactionException e) {
            e.printStackTrace();
        }
        assertNotNull(result);
        assertNotNull(result.getTransactionId());
        TransactionResult status = helper.getTransactionStatus(result.getTransactionId(), true);
        assertNotNull(status);
        Assert.assertEquals(status.getResult(), TransactionResultType.Created);
    }
}