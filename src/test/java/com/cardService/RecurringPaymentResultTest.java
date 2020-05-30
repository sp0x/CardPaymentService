package com.cardService;

import com.cardService.payment.ECOMMResponseParser;
import com.cardService.payment.RecurringPaymentResult;
import lv.tietoenator.cs.ecomm.merchant.Merchant;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RecurringPaymentResultTest {

    private RecurringPaymentResult.Factory _factory;

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
        okResponse.put("RESULT", "OK");
        okResponse.put("RESULT_CODE", "200");
        okResponse.put("recurringPaymentId", "");
        when(mockBankResponseParser.parse(anyString())).thenReturn(okResponse);

        Pair<Merchant, Properties> props = Config.getMerchantConfiguration(mockProperties);
        _factory = new RecurringPaymentResult.Factory(props.getRight());
    }

    @Test
    public void fromRecurringResult() {
        RecurringPaymentResult res = _factory.fromRecurringResult(null, false);
        assertNull(res);
    }
}