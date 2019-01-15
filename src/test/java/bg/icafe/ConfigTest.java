package bg.icafe;

import lv.tietoenator.cs.ecomm.merchant.Merchant;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.*;

public class ConfigTest {

    @Test
    public void getMerchant() {
        Pair<Merchant, Properties> props = Config.getMerchantConfiguration();
        assertNotNull(props);
        assertNotNull(props.getLeft());
        assertNotNull(props.getRight());
    }
}