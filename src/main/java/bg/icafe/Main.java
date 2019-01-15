package bg.icafe;

import bg.icafe.network.Listener;
import bg.icafe.network.MqConfig;
import bg.icafe.network.TransactionListener;
import lv.tietoenator.cs.ecomm.merchant.Merchant;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.tuple.Pair;

public class Main {

    public static void main(String[] args) {
        ECOMMHelper helper = null;
        try {
            Pair<Merchant, Properties> props = Config.getMerchantConfiguration();
            helper = new ECOMMHelper("", props.getLeft(), props.getRight());
        } catch (Exception e) {
            e.printStackTrace();
        }
        try{
            MqConfig mqconfig = Config.readMqConfiguration();
            Listener mqListener = new Listener(mqconfig);
            mqListener.connect(mqconfig.getPass());
            TransactionListener tListener = new TransactionListener(mqListener, helper);
        }catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        hang();
    }

    private static void hang(){
        while(true){
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }




}


