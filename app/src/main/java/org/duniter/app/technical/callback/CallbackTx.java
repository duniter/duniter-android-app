package org.duniter.app.technical.callback;

import java.util.List;

import org.duniter.app.model.Entity.Tx;

/**
 * Created by naivalf27 on 21/04/16.
 */
public interface CallbackTx {
    public void methode(List<Tx> txList,List<String[]> listPendingSource);
}
