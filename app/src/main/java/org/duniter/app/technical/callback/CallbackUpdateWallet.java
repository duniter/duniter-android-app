package org.duniter.app.technical.callback;

import java.util.List;

import org.duniter.app.model.Entity.Identity;
import org.duniter.app.model.Entity.Requirement;
import org.duniter.app.model.Entity.Source;
import org.duniter.app.model.Entity.Tx;
import org.duniter.app.model.Entity.Wallet;

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
