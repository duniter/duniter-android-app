package io.duniter.app.technical.callback;

import java.util.List;
import java.util.Map;

import io.duniter.app.model.Entity.Identity;
import io.duniter.app.model.Entity.Requirement;
import io.duniter.app.model.Entity.Source;
import io.duniter.app.model.Entity.Tx;
import io.duniter.app.model.Entity.Wallet;

/**
 * Created by naivalf27 on 21/04/16.
 */
public interface CallbackUpdateWallet {
    void methode(
            Wallet wallet,
            List<Source> sources,
            List<Tx> listTx,
            List<String[]> listSourcePending,
            Requirement requirement,
            Identity identity);
}
