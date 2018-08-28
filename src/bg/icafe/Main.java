package bg.icafe;

import bg.icafe.network.Listener;
import bg.icafe.network.MqConfig;
import bg.icafe.network.TransactionListener;
import lv.tietoenator.cs.ecomm.merchant.ConfigurationException;
import lv.tietoenator.cs.ecomm.merchant.Merchant;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.tuple.Pair;

public class Main {

    public static void main(String[] args) {
	// write your code here
        try {
            MqConfig mqconfig = readConfiguration();
            Listener mqListener = new Listener(mqconfig);
            Pair<Merchant, Properties> props = getMerchant();
            ECOMMHelper helper = new ECOMMHelper("", props.getLeft(), props.getRight());

            mqListener.connect(mqconfig.getPass());
            TransactionListener tListener = new TransactionListener(mqListener, helper);
            hang();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
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

    private static MqConfig readConfiguration() throws Exception {
        String host = System.getenv("MQ_HOST");
        if(host==null || host.length()==0) throw new Exception("MQ_HOST Is required");
        String username = System.getenv("MQ_USER");
        String port = System.getenv("MQ_PORT");
        String pass = System.getenv("MQ_PASS");
        MqConfig mqConfig = new MqConfig(host, port, username, pass);
        return mqConfig;
    }


    private static Pair<Merchant, Properties> getMerchant(){
        Merchant merchant;
        Properties props = new Properties();
        try{
            String filename = "merchant.properties";
            InputStream input = Main.class.getClassLoader().getResourceAsStream(filename);
            props.load(input);

            merchant = new Merchant(props);
        }catch(ConfigurationException e){
            System.err.println("Error: " + e.getMessage());
            return null;
        } catch (FileNotFoundException e) {
            System.err.println("Error: " + e.getMessage());
            return null;
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            return null;
        }
        return Pair.of(merchant, props);
    }
}


