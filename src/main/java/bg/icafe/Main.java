package bg.icafe;

import bg.icafe.network.mq.TransactionClient;
import bg.icafe.network.http.HttpConfig;
import bg.icafe.network.http.TransactionCallbackServer;
import lv.tietoenator.cs.ecomm.merchant.ConfigurationException;

import java.io.IOException;
import java.util.concurrent.TimeoutException;


public class Main {

    public static void main(String[] args) {
        try{
            TransactionClient tListener = TransactionClient.getInstance();
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


