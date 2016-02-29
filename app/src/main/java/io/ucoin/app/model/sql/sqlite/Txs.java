package io.ucoin.app.model.sql.sqlite;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;

import io.ucoin.app.Application;
import io.ucoin.app.UcoinUris;
import io.ucoin.app.enumeration.SourceState;
import io.ucoin.app.enumeration.TxDirection;
import io.ucoin.app.enumeration.TxState;
import io.ucoin.app.model.UcoinSource;
import io.ucoin.app.model.UcoinTx;
import io.ucoin.app.model.UcoinTxs;
import io.ucoin.app.model.UcoinWallet;
import io.ucoin.app.model.http_api.TxHistory;
import io.ucoin.app.sqlite.SQLiteTable;
import io.ucoin.app.sqlite.SQLiteView;

final public class Txs extends Table
        implements UcoinTxs {

    private Long mWalletId;

    public Txs(Context context, Long walletId) {
        this(context, walletId, SQLiteTable.Tx.WALLET_ID + "=?", new String[]{walletId.toString()});
    }
    public Txs(Context context) {
        this(context, null, null, null);
    }

    private Txs(Context context, Long walletId, String selection, String[] selectionArgs) {
        this(context, walletId, selection, selectionArgs, null);
    }

    private Txs(Context context, String query, String[] selectionArgs) {
        super(context, UcoinUris.REQUETE_URI, query, selectionArgs, null);
    }

    private Txs(Context context, Long walletId, String selection, String[] selectionArgs, String sortOrder) {
        super(context, UcoinUris.TX_URI, selection, selectionArgs, sortOrder);
        mWalletId = walletId;
    }

    @Override
    public UcoinTx add(TxHistory.Tx tx, TxDirection direction) {
        ContentValues values = new ContentValues();
        values.put(SQLiteTable.Tx.WALLET_ID, mWalletId);
        values.put(SQLiteTable.Tx.VERSION, tx.version);
        values.put(SQLiteTable.Tx.COMMENT, tx.comment);
        values.put(SQLiteTable.Tx.DIRECTION, direction.name());

        //calculate defaultAmount once
        //Long qtAmount = (long) 0;
        BigInteger qtAmount = new BigInteger("0");
        switch (direction) {
            case IN:
                for (TxHistory.Tx.Output output : tx.outputs) {
                    if (output.publicKey.matches(wallet().publicKey())) {
                        //qtAmount += output.amount;
                        qtAmount = qtAmount.add(new BigInteger(output.amount));
                    }
                }
                break;
            case OUT:
                for (TxHistory.Tx.Input input : tx.inputs) {
                    if (tx.issuers[input.index].matches(wallet().publicKey()))
                        //qtAmount += input.amount;
                        qtAmount = qtAmount.add(new BigInteger(input.amount));
                }

                for (TxHistory.Tx.Output output : tx.outputs) {
                    if (output.publicKey.matches(wallet().publicKey())) {
                        //qtAmount -= output.amount;
                        qtAmount = qtAmount.subtract(new BigInteger(output.amount));
                    }
                }

                break;
        }
        values.put(SQLiteTable.Tx.QUANTITATIVE_AMOUNT, qtAmount.toString());
        values.put(SQLiteTable.Tx.HASH, tx.hash);

        if (tx instanceof TxHistory.ConfirmedTx) {
            values.put(SQLiteTable.Tx.STATE, TxState.CONFIRMED.name());
            values.put(SQLiteTable.Tx.TIME, ((TxHistory.ConfirmedTx) tx).time);
            values.put(SQLiteTable.Tx.BLOCK, ((TxHistory.ConfirmedTx) tx).block_number);
        } else if (tx instanceof TxHistory.PendingTx) {
            values.put(SQLiteTable.Tx.STATE, TxState.PENDING.name());
            values.put(SQLiteTable.Tx.TIME, Application.getCurrentTime());
            values.put(SQLiteTable.Tx.BLOCK, wallet().currency().blocks().currentBlock().number());
        }


        //insertion in TX table
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        operations.add(ContentProviderOperation.newInsert(UcoinUris.TX_URI)
                .withValues(values)
                .build());

        //insertions in TX_ISSUER table
        int issuerOrder = 0;
        for (String issuer : tx.issuers) {
            values = new ContentValues();
            values.put(SQLiteTable.TxIssuer.PUBLIC_KEY, issuer);
            values.put(SQLiteTable.TxIssuer.ISSUER_ORDER, issuerOrder++);
            operations.add(ContentProviderOperation.newInsert(UcoinUris.TX_ISSUER_URI)
                    .withValues(values)
                    .withValueBackReference(SQLiteTable.TxIssuer.TX_ID, 0)
                    .build());
        }

        //insertions in TX_INPUT table
        for (TxHistory.Tx.Input input : tx.inputs) {
            values = new ContentValues();
            values.put(SQLiteTable.TxInput.ISSUER_INDEX, input.index);
            values.put(SQLiteTable.TxInput.TYPE, input.type.name());
            values.put(SQLiteTable.TxInput.NUMBER, input.number);
            values.put(SQLiteTable.TxInput.FINGERPRINT, input.fingerprint);
            values.put(SQLiteTable.TxInput.AMOUNT, input.amount);
            operations.add(ContentProviderOperation.newInsert(UcoinUris.TX_INPUT_URI)
                    .withValues(values)
                    .withValueBackReference(SQLiteTable.TxInput.TX_ID, 0)
                    .build());
        }

        //insertions in TX_OUPUT table
        for (TxHistory.Tx.Output output : tx.outputs) {
            values = new ContentValues();
            values.put(SQLiteTable.TxOutput.PUBLIC_KEY, output.publicKey);
            values.put(SQLiteTable.TxOutput.AMOUNT, output.amount);

            operations.add(ContentProviderOperation.newInsert(UcoinUris.TX_OUTPUT_URI)
                    .withValues(values)
                    .withValueBackReference(SQLiteTable.TxOutput.TX_ID, 0)
                    .build());
        }

        //insertions in TX_SIGNATURE table
        issuerOrder = 0;
        for (String signature : tx.signatures) {
            values = new ContentValues();
            values.put(SQLiteTable.TxSignature.VALUE, signature);
            values.put(SQLiteTable.TxSignature.ISSUER_ORDER, issuerOrder++);

            operations.add(ContentProviderOperation.newInsert(UcoinUris.TX_SIGNATURE_URI)
                    .withValues(values)
                    .withValueBackReference(SQLiteTable.TxSignature.TX_ID, 0)
                    .build());
        }

        ContentProviderResult[] result;
        try {
            result = applyBatch(operations);
        } catch (Exception e) {
            return null;
        }

        return new Tx(mContext, Long.parseLong(result[0].uri.getLastPathSegment()));
    }

    @Override
    public UcoinTxs add(TxHistory history) {
        for (TxHistory.ConfirmedTx tx : history.history.sent) {
            UcoinTx localTx = getByHash(tx.hash);
            if (localTx == null) {
                boolean isIssuer = false;
                for (String issuer : tx.issuers) {
                    if (issuer.equals(wallet().publicKey())) {
                        isIssuer = true;
                        break;
                    }
                }
                if (isIssuer) {
                    if (tx.time != null) add(tx, TxDirection.OUT);
                }
            } else if (localTx.state() == TxState.PENDING) {
                localTx.setState(TxState.CONFIRMED);
                localTx.setTime(tx.time);
                localTx.setBlock(tx.block_number);
            } else if (localTx.direction() == TxDirection.IN){
                localTx.setDirection(TxDirection.OUT);
            }
        }

        for (TxHistory.ConfirmedTx tx : history.history.received) {
            UcoinTx localTx = getByHash(tx.hash);
            if (localTx == null) {
                boolean isIssuer = false;
                for (String issuer : tx.issuers) {
                    if (issuer.equals(wallet().publicKey())) {
                        isIssuer = true;
                        break;
                    }
                }
                if (!isIssuer) {
                    if (tx.time != null) add(tx, TxDirection.IN);
                }
            } else if (localTx.state() == TxState.PENDING) {
                localTx.setState(TxState.CONFIRMED);
                localTx.setTime(tx.time);
                localTx.setBlock(tx.block_number);
            } else if (localTx.direction() == TxDirection.OUT){
                localTx.setDirection(TxDirection.IN);
            }
        }

        for (TxHistory.PendingTx tx : history.history.pending) {
            UcoinTx localTx = getByHash(tx.hash);
            if (localTx == null) {
                boolean isIssuer = false;
                for (String issuer : tx.issuers) {
                    if (issuer.equals(wallet().publicKey())) {
                        isIssuer = true;
                        break;
                    }
                }

                if (!isIssuer) {
                    add(tx, TxDirection.IN);
                } else {
                    add(tx, TxDirection.OUT);
                    for (TxHistory.Tx.Input input : tx.inputs) {
                        UcoinSource source = wallet().sources().getByFingerprint(input.fingerprint);
                        if (source != null) {
                            source.setState(SourceState.CONSUMED);
                        }
                    }
                }
            }
        }

        return this;
    }

    @Override
    public UcoinTx getById(Long id) {
        return new Tx(mContext, id);
    }

    @Override
    public UcoinTx getLastTx() {
        String selection = SQLiteView.Tx.WALLET_ID + "=?";
        String[] selectionArgs = new String[]{mWalletId.toString()};
        String sortOrder = SQLiteView.Tx.TIME + " DESC LIMIT 1";
        UcoinTxs txs = new Txs(mContext, mWalletId, selection, selectionArgs, sortOrder);
        if (txs.iterator().hasNext()) {
            return txs.iterator().next();
        } else {
            return null;
        }
    }

    @Override
    public UcoinTx getLastConfirmedTx() {
        String selection = SQLiteView.Tx.WALLET_ID + "=? AND " + SQLiteView.Tx.STATE + "=?";
        String[] selectionArgs = new String[]{mWalletId.toString(), TxState.CONFIRMED.name()};
        String sortOrder = SQLiteView.Tx.TIME + " DESC LIMIT 1";
        UcoinTxs txs = new Txs(mContext, mWalletId, selection, selectionArgs, sortOrder);
        if (txs.iterator().hasNext()) {
            return txs.iterator().next();
        } else {
            return null;
        }
    }

    @Override
    public UcoinTxs getByDirection(TxDirection direction) {
        String selection = SQLiteTable.Tx.WALLET_ID + "=? AND " +
                SQLiteTable.Tx.DIRECTION + "=?";
        String[] selectionArgs = new String[]{mWalletId.toString(), direction.name()};
        return new Txs(mContext, mWalletId, selection, selectionArgs);
    }

    @Override
    public UcoinTx getByHash(String hash) {
        String selection = SQLiteView.Tx.WALLET_ID + "=? AND " + SQLiteView.Tx.HASH + "=?";
        String[] selectionArgs = new String[]{
                mWalletId.toString(),
                hash
        };
        String sortOrder = SQLiteView.Tx.TIME + " DESC LIMIT 1";
        UcoinTxs txs = new Txs(mContext, mWalletId, selection, selectionArgs, sortOrder);
        if (txs.iterator().hasNext()) {
            return txs.iterator().next();
        } else {
            return null;
        }
    }

    @Override
    public UcoinTxs getByWalletId(long walletId){
        String query = "SELECT * " +
                "FROM " + SQLiteView.Tx.TABLE_NAME + " WHERE " +
                SQLiteView.Tx.WALLET_ID + "=? " +
                "ORDER BY " + SQLiteView.Tx.TIME + " DESC";

        String[] selectionArgs = new String[]{String.valueOf(walletId)};
        return new Txs(mContext, query, selectionArgs);
    }

    @Override
    public UcoinTxs getByPublicKey(String publicKey, long walletId){
        String query = "SELECT * " +
                "FROM " + SQLiteView.Tx.TABLE_NAME + " trx" +
                " LEFT OUTER JOIN " + SQLiteTable.TxOutput.TABLE_NAME + " output " +
                    "ON trx." + SQLiteView.Tx._ID + "=output." + SQLiteTable.TxOutput.TX_ID +
                " LEFT OUTER JOIN " + SQLiteTable.TxIssuer.TABLE_NAME + " issuer " +
                    "ON trx." + SQLiteView.Tx._ID + "=issuer." + SQLiteTable.TxIssuer.TX_ID +
                " WHERE ";
        if(walletId != (long)-1){
            query += "trx." + SQLiteView.Tx.WALLET_ID + "=? AND ";
        }
        query += "( output." + SQLiteTable.TxOutput.PUBLIC_KEY + "=? OR " +
                "issuer." + SQLiteTable.TxIssuer.PUBLIC_KEY + "=? ) " +
                "ORDER BY trx." + SQLiteView.Tx.TIME + " DESC";

        String[] selectionArgs;
        if(walletId != (long)-1){
            selectionArgs = new String[]{String.valueOf(walletId),publicKey,publicKey};
        }else {
            selectionArgs = new String[]{publicKey, publicKey};
        }

        return new Txs(mContext, query, selectionArgs);
    }

    @Override
    public UcoinWallet wallet() {
        return new Wallet(mContext, mWalletId);
    }

    @Override
    public UcoinTxs getByState(TxState state) {
        String selection = SQLiteTable.Tx.WALLET_ID + "=? AND " +
                SQLiteTable.Tx.STATE + "=?";
        String[] selectionArgs = new String[]{mWalletId.toString(), state.name()};
        return new Txs(mContext, mWalletId, selection, selectionArgs);
    }

    @Override
    public Cursor cursor() {
        return fetch();
    }

    @Override
    public Iterator<UcoinTx> iterator() {
        Cursor cursor = fetch();
        if (cursor != null) {
            ArrayList<UcoinTx> data = new ArrayList<>();
            while (cursor.moveToNext()) {
                Long id = cursor.getLong(0);
                data.add(new Tx(mContext, id));
            }
            cursor.close();

            return data.iterator();
        }
        return null;
    }
}