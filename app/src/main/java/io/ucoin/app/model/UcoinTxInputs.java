package io.ucoin.app.model;


import io.ucoin.app.model.http_api.TxHistory;

public interface UcoinTxInputs extends SqlTable, Iterable<UcoinTxInput> {
    UcoinTxInput add(TxHistory.Tx.Input input);

    UcoinTxInput add(UcoinSource source, Integer index);

    UcoinTxInput getById(Long id);
}