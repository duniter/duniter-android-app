package io.ucoin.app.service;

import java.util.List;

import io.ucoin.app.model.BlockchainParameter;
import io.ucoin.app.model.Wallet;

/**
 * Created by eis on 14/01/15.
 */
public class DataContext extends BaseService {

    /** Logger. */
    private static final String TAG = "DataContext";

    private List<Wallet> wallets = null;

    private BlockchainParameter blockchainParameter = null;

    public DataContext() {
        super();
    }

    public List<Wallet> getWallets() {
        return wallets;
    }

    public BlockchainParameter getBlockchainParameter() {
        return blockchainParameter;
    }

    public void setBlockchainParameter(BlockchainParameter blockchainParameter) {
        this.blockchainParameter = blockchainParameter;
    }

    public void setWallets(List<Wallet> wallets) {
        this.wallets = wallets;
    }

}
