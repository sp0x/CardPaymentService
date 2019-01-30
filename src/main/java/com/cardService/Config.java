package com.cardService;

import com.cardService.network.mq.MqConfig;
import com.cardService.network.http.HttpConfig;
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
    static final String CURRENCY_BG = "975";
    static final String CURRENCY_USD = "840";
    static final String CURRENCY_EUR = "978";
    /**
     * Get the configured currency code.
     * @return
     */
    public static String getCurrency(){
        return CURRENCY_BG;
    }

    public static Properties getGeneralSettings() throws IOException {
        Properties props = new Properties();
        String filename = "settings.properties";
        InputStream input = Main.class.getClassLoader().getResourceAsStream(filename);
        props.load(input);
        return props;
    }

    /**
     *
     * @return
     * @throws IOException
     * @throws ConfigurationException
     */
    public static HttpConfig getHttpConfiguration() throws IOException, ConfigurationException {
        Properties props = getGeneralSettings();
        String host = System.getenv("HTTP_HOST");
        String port = System.getenv("HTTP_PORT");
        if(host==null || host.length()==0){
            host = props.getProperty("http.host");
        }
        if(port==null || port.length()==0){
            port = props.getProperty("http.port");
        }
        if(host==null || host.length()==0){
            throw new ConfigurationException("Http host not set!");
        }
        if(port==null || port.length()==0){
            throw new ConfigurationException("Http host not set!");
        }
        HttpConfig httpConfig = new HttpConfig(host, Integer.parseInt(port));
        return httpConfig;
    }


    /**
     * Reads environment configuration.
     * @return
     * @throws Exception
     */
    public static MqConfig readMqConfiguration() throws Exception {
        Properties props = getGeneralSettings();
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

    public static String getDefaultLanguage() {
        return "bg_BG";
    }
}