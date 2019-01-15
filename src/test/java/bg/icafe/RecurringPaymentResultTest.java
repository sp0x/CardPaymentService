package bg.icafe;

import org.junit.Test;

import static org.junit.Assert.*;

public class RecurringPaymentResultTest {

    @Test
    public void parseRecurringResultType() {
        RecurringPaymentResultType type = RecurringPaymentResult.parseRecurringResultType(null);
        assertEquals(type, RecurringPaymentResultType.Failed);
    }

    @Test
    public void fromRecurringResult() {
        RecurringPaymentResult result = RecurringPaymentResult.fromRecurringResult(null, true);
        assertNull(result);
    }
}