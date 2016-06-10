package org.duniter.app.technical.callback;

import org.duniter.app.model.Entity.Tx;

import java.util.List;

/**
 * Created by naivalf27 on 21/04/16.
 */
public interface CallbackUdReceived {
    public void methode(List<Tx> txList);
}
