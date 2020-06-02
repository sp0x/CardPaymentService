package com.cardService;

import lv.tietoenator.cs.ecomm.merchant.Merchant;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConfigTest {

    @Test
    public void getMerchant() {
        Properties mockProperties = mock(Properties.class);
        when((mockProperties.getProperty("keystore.file"))).thenReturn("");
        when((mockProperties.getProperty("keystore.type"))).thenReturn("JKS");
        when((mockProperties.getProperty("keystore.password"))).thenReturn("password");
        when((mockProperties.getProperty("bank.server.url"))).thenReturn("https://bank.com");
        when((mockProperties.getProperty("returnOkUrl"))).thenReturn("https://us.com/ok");
        when((mockProperties.getProperty("returnFailUrl"))).thenReturn("https://us.com/fail");
        Pair<Merchant, Properties> merchantConfig = Config.getMerchantConfiguration(mockProperties);
        assertNotNull(merchantConfig.getLeft());
        assertNotNull(merchantConfig.getRight());
    }

    @Ignore
    @Test
    public void getMerchantConfigurationReturnsNullWithoutConfig() {
        Pair<Merchant, Properties> props = Config.getMerchantConfiguration();
        assertNull(props);
    }
}