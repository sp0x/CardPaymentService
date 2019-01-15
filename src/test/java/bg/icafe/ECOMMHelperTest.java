package bg.icafe;

import lv.tietoenator.cs.ecomm.merchant.Merchant;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static junit.framework.TestCase.assertNotNull;

public class ECOMMHelperTest {

    private ECOMMHelper helper;

    @Before
    public void setUp() throws Exception {
        Pair<Merchant, Properties> props = Config.getMerchantConfiguration();
        helper = new ECOMMHelper("127.0.0.1", props.getLeft(), props.getRight());

    }

    @Test
    public void initializeRecurring() {
        RecurringPaymentResult result = helper.initializeRecurring(null, "1","85.217.220.130", "Recurring payment init test.");
        assertNotNull(result);
    }
}