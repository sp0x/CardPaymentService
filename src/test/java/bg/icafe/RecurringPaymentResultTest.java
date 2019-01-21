package bg.icafe;

import bg.icafe.payment.RecurringPaymentResult;
import lv.tietoenator.cs.ecomm.merchant.Merchant;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.*;

public class RecurringPaymentResultTest {

    private RecurringPaymentResult.Factory _factory;

    @Before
    public void setUp() throws Exception {
        Pair<Merchant, Properties> props = Config.getMerchantConfiguration();
        _factory = new RecurringPaymentResult.Factory(props.getRight());
    }

    @Test
    public void fromRecurringResult() {
        RecurringPaymentResult res = _factory.fromRecurringResult(null, false);
        assertNull(res);
    }
}