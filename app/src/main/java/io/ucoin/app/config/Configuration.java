package io.ucoin.app.config;


import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import io.ucoin.app.model.Identity;
import io.ucoin.app.model.Wallet;
import io.ucoin.app.technical.UCoinTechnicalException;
import io.ucoin.app.technical.crypto.CryptoUtils;
import io.ucoin.app.technical.crypto.TestFixtures;

/**
 * Access to configuration options
 * @author Benoit Lavenier <benoit.lavenier@e-is.pro>
 * @since 1.0
 */
public class Configuration  {
    /** Logger. */
    private static final String TAG = "Configuration";

    private static Configuration instance;

    public static Configuration instance() {
        return instance;
    }

    public static void setInstance(Configuration instance) {
        Configuration.instance = instance;
    }

    protected File configFile;

    public Configuration() {
        super();
    }

    public String getVersion() {
        return ConfigurationOption.VERSION.getDefaultValue();
    }

//    public String getNodeCurrency() {
//        //return applicationConfig.getOption(ConfigurationOption.NODE_CURRENCY.getKey());
//    }
//
//    public String getNodeProtocol() {
//        //return applicationConfig.getOption(ConfigurationOption.NODE_PROTOCOL.getKey());
//    }

    public String getNodeHost() {
        //return applicationConfig.getOption(ConfigurationOption.NODE_HOST.getKey());
        return ConfigurationOption.NODE_HOST.getDefaultValue();
    }

    public int getNodePort() {
        //return applicationConfig.getOptionAsInt(ConfigurationOption.NODE_PORT.getKey());
        return Integer.parseInt(ConfigurationOption.NODE_PORT.getDefaultValue());
    }

    public URL getNodeUrl() {
        //return applicationConfig.getOptionAsURL(ConfigurationOption.NODE_URL.getKey());
        try {
            return new URL("http://" + getNodeHost() + ":" + getNodePort());
        } catch (MalformedURLException e) {
            throw new UCoinTechnicalException("Could not compute the node URL", e);
        }
    }

    public int getNodeTimeout() {
        return Integer.parseInt(ConfigurationOption.NODE_TIMEOUT.getDefaultValue());
    }

    private Wallet currentWallet;

    public Wallet getCurrentWallet() {
        if (currentWallet != null) {
            return currentWallet;
        }

        // TODO : replace from a database ?
        TestFixtures fixtures = new TestFixtures();

        Identity identity = new Identity();
        identity.setUid(fixtures.getUid());
        identity.setPubkey(fixtures.getUserPublicKey());
        identity.setTimestamp(fixtures.getSelfTimestamp());
        identity.setSignature(fixtures.getSelfSignature());
        currentWallet = new Wallet(
                fixtures.getCurrency(),
                CryptoUtils.decodeBase58(fixtures.getUserPrivateKey()),
                identity);

        return currentWallet;
    }

    public void setCurrentWallet(Wallet currentWallet) {
        this.currentWallet = currentWallet;
    }
    
}
