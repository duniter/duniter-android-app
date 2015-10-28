package io.ucoin.app.service;

import java.util.List;

import io.ucoin.app.model.local.Wallet;
import io.ucoin.app.model.remote.BlockchainParameters;

/**
 * Created by eis on 14/01/15.
 */
public class DataContext extends BaseService {

    /** Logger. */
    private static final String TAG = "DataContext";

    private List<Wallet> wallets = null;

    private BlockchainParameters blockchainParameters = null;

    public DataContext() {
        super();
    }

    public List<Wallet> getWallets() {
        return wallets;
    }

    public BlockchainParameters getBlockchainParameters() {
        return blockchainParameters;
    }

    public void setBlockchainParameters(BlockchainParameters blockchainParameters) {
        this.blockchainParameters = blockchainParameters;
    }

    public void setWallets(List<Wallet> wallets) {
        this.wallets = wallets;
    }

}
