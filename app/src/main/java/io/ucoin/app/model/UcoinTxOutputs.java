package io.ucoin.app.model;


import io.ucoin.app.model.http_api.TxHistory;

public interface UcoinTxOutputs extends SqlTable, Iterable<UcoinTxOutput> {
    UcoinTxOutput add(TxHistory.Tx.Output output);

    UcoinTxOutput add(String publicKey, Long amount);

    UcoinTxOutput getById(Long id);

    UcoinTxOutputs getByOutput(String publicKey);
}