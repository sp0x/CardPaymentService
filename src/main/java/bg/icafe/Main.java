package bg.icafe;

import bg.icafe.network.Listener;
import bg.icafe.network.MqConfig;
import bg.icafe.network.TransactionListener;
import bg.icafe.network.http.HttpConfig;
import bg.icafe.network.http.TransactionCallbackServer;
import bg.icafe.payment.ECOMMHelper;
import lv.tietoenator.cs.ecomm.merchant.ConfigurationException;
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
        HttpConfig httpConfig = null;
        try {
            httpConfig = Config.getHttpConfiguration();
            TransactionCallbackServer callbackListener = new TransactionCallbackServer(httpConfig, Config.getGeneralSettings());
            callbackListener.listenAndBlock();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ConfigurationException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

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


