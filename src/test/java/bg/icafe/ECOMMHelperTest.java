package bg.icafe;

import bg.icafe.payment.ECOMMHelper;
import bg.icafe.payment.RecurringPaymentResult;
import bg.icafe.payment.TransactionResult;
import bg.icafe.payment.TransactionResultType;
import lv.tietoenator.cs.ecomm.merchant.Merchant;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertNotNull;

public class ECOMMHelperTest {

    private ECOMMHelper helper;
    private String test_ip = "85.85.85.85";

    @Before
    public void setUp() throws Exception {
        Pair<Merchant, Properties> props = Config.getMerchantConfiguration();
        helper = new ECOMMHelper("127.0.0.1", props.getLeft(), props.getRight());

    }

    @Test
    public void initializeRecurring() {
        RecurringPaymentResult result = helper.initializeRecurring(null, "1",test_ip, "Recurring payment init test.");
        assertNotNull(result);
        assertNotNull(result.getTransactionId());
        assertNotNull(result.getUrl());
    }

    @Test
    public void getTransactionStatus() {
        RecurringPaymentResult result = helper.initializeRecurring(null, "1",test_ip, "Recurring payment init test.");
        assertNotNull(result);
        assertNotNull(result.getTransactionId());
        TransactionResult status = helper.getTransactionStatus(result.getTransactionId(), true);
        assertNotNull(status);
        assertEquals(status.getResult(), TransactionResultType.Created);
    }
}