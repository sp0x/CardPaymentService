package bg.icafe;

import bg.icafe.network.MqConfig;
import lv.tietoenator.cs.ecomm.merchant.ConfigurationException;
import lv.tietoenator.cs.ecomm.merchant.Merchant;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config
{
    /**
     * Get the configured currency code.
     * @return
     */
    public static String getCurrency(){
        return "975";
    }
    /**
     * Reads environment configuration.
     * @return
     * @throws Exception
     */
    public static MqConfig readMqConfiguration() throws Exception {
        Properties props = new Properties();
        String filename = "settings.properties";
        InputStream input = Main.class.getClassLoader().getResourceAsStream(filename);
        props.load(input);
        String host = System.getenv("MQ_HOST");

        if(host==null || host.length()==0){
            host = props.getProperty("mq.host");
        }
        if(host==null || host.length()==0){
            throw new Exception("MQ_HOST Is required");
        }
        String username = System.getenv("MQ_USER");
        if(username==null){
            username = props.getProperty("mq.user");
        }
        String port = System.getenv("MQ_PORT");
        String pass = System.getenv("MQ_PASS");
        if(pass==null){
            pass = props.getProperty("mq.pass");
        }
        MqConfig mqConfig = new MqConfig(host, port, username, pass);
        return mqConfig;
    }
    /**
     *
     * @return
     */
    public static Pair<Merchant, Properties> getMerchantConfiguration(){
        Merchant merchant;
        Properties props = new Properties();
        try{
            String filename = "merchant.properties";
            InputStream input = Main.class.getClassLoader().getResourceAsStream(filename);
            props.load(input);
            String envKeystore = System.getenv("KEYSTORE");
            String envKeystorePass = System.getenv("KEYSTORE_PASS");
            if(envKeystore!=null) props.setProperty("keystore.file", envKeystore);
            if(envKeystorePass!=null) props.setProperty("keystore.password", envKeystorePass);

            String keystorePath = props.getProperty("keystore.file");
            keystorePath = new File(keystorePath).getCanonicalPath();
            System.out.println("Using keystore: " + keystorePath);
            if(!(new File(keystorePath).exists())){
                throw new FileNotFoundException("Keystore file not found.");
            }
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