package io.ucoin.app.service;

import io.ucoin.app.model.BlockchainParameter;
import io.ucoin.app.model.Wallet;

/**
 * Created by eis on 14/01/15.
 */
public class DataContext extends BaseService {

    /** Logger. */
    private static final String TAG = "DataContext";

    private Wallet wallet = null;

    private BlockchainParameter blockchainParameter = null;

    public DataContext() {
        super();
    }

    public Wallet getWallet() {
        return wallet;
    }

    public BlockchainParameter getBlockchainParameter() {
        return blockchainParameter;
    }

    public void setBlockchainParameter(BlockchainParameter blockchainParameter) {
        this.blockchainParameter = blockchainParameter;
    }

    public void setWallet(Wallet currentWallet) {
        this.wallet = currentWallet;
    }

}
