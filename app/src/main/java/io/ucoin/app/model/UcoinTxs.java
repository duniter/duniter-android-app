package io.ucoin.app.model;


import android.database.Cursor;

import io.ucoin.app.enumeration.TxDirection;
import io.ucoin.app.enumeration.TxState;
import io.ucoin.app.model.http_api.TxHistory;

public interface UcoinTxs extends SqlTable, Iterable<UcoinTx> {
    UcoinTx add(TxHistory.Tx tx, TxDirection direction);

    UcoinTxs add(TxHistory history);

    UcoinTx getById(Long id);

    UcoinTx getLastTx();

    UcoinTx getLastConfirmedTx();

    UcoinTxs getByState(TxState state);

    UcoinTxs getByDirection(TxDirection direction);

    UcoinTx getByHash(String hash);

    UcoinTxs getByPublicKey(String publicKey,long walletId);

    UcoinTxs getByWalletId(long walletId);

    UcoinWallet wallet();

    Cursor cursor();
}