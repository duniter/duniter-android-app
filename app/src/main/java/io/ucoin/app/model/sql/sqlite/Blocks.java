package io.ucoin.app.model.sql.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;
import java.util.Iterator;

import io.ucoin.app.UcoinUris;
import io.ucoin.app.model.UcoinBlock;
import io.ucoin.app.model.UcoinBlocks;
import io.ucoin.app.model.http_api.BlockchainBlock;
import io.ucoin.app.sqlite.SQLiteTable;

final public class Blocks extends Table
        implements UcoinBlocks {

    private Long mCurrencyId;

    public Blocks(Context context, Long currencyId) {
        this(context, currencyId, SQLiteTable.Block.CURRENCY_ID + "=?", new String[]{currencyId.toString()});
    }

    public Blocks(Context context, Long currencyId, String selection, String[] selectionArgs) {
        this(context, currencyId, selection, selectionArgs, null);
    }

    public Blocks(Context context, Long currencyId, String selection, String[] selectionArgs, String sortOrder) {
        super(context, UcoinUris.BLOCK_URI, selection, selectionArgs, sortOrder);
        mCurrencyId = currencyId;
    }

    @Override
    public UcoinBlock add(BlockchainBlock blockchainBlock) {
        if (blockchainBlock == null) {
            return null;
        }
        ContentValues values = new ContentValues();
        values.put(SQLiteTable.Block.CURRENCY_ID, mCurrencyId);
        values.put(SQLiteTable.Block.VERSION, blockchainBlock.version);
        values.put(SQLiteTable.Block.NONCE, blockchainBlock.nonce);
        values.put(SQLiteTable.Block.NUMBER, blockchainBlock.number);
        values.put(SQLiteTable.Block.POWMIN, blockchainBlock.powMin);
        values.put(SQLiteTable.Block.TIME, blockchainBlock.time);
        values.put(SQLiteTable.Block.MEDIAN_TIME, blockchainBlock.medianTime);
        values.put(SQLiteTable.Block.DIVIDEND, blockchainBlock.dividend);
        values.put(SQLiteTable.Block.MONETARY_MASS, blockchainBlock.monetaryMass);
        values.put(SQLiteTable.Block.ISSUER, blockchainBlock.issuer);
        values.put(SQLiteTable.Block.PREVIOUS_HASH, blockchainBlock.previousHash);
        values.put(SQLiteTable.Block.PREVIOUS_ISSUER, blockchainBlock.previousIssuer);
        values.put(SQLiteTable.Block.MEMBERS_COUNT, blockchainBlock.membersCount);
        values.put(SQLiteTable.Block.HASH, blockchainBlock.hash);
        values.put(SQLiteTable.Block.SIGNATURE, blockchainBlock.signature);
        values.put(SQLiteTable.Block.IS_MEMBERSHIP, false);

        Uri uri = insert(values);
        Long id = Long.parseLong(uri.getLastPathSegment());
        if (id > 0) {
            return new Block(mContext, id);
        }

        return null;
    }

    @Override
    public UcoinBlock getById(Long id) {
        return new Block(mContext, id);
    }

    @Override
    public UcoinBlock getByNumber(Long number) {
        String selection = SQLiteTable.Block.CURRENCY_ID + "=? AND " +
                SQLiteTable.Block.NUMBER + "=?";
        String[] selectionArgs = new String[]{mCurrencyId.toString(), number.toString()};
        UcoinBlocks blocks = new Blocks(mContext, mCurrencyId, selection, selectionArgs);
        if (blocks.iterator().hasNext()) {
            return blocks.iterator().next();
        } else {
            return null;
        }
    }

    @Override
    public UcoinBlock lastUdBlock() {
        String selection = SQLiteTable.Block.CURRENCY_ID + "=? AND " + SQLiteTable.Block.DIVIDEND + " IS NOT NULL";
        String[] selectionArgs = new String[]{mCurrencyId.toString()};
        String sortOrder = SQLiteTable.Block.NUMBER + " DESC LIMIT 1";
        UcoinBlocks blocks = new Blocks(mContext, mCurrencyId, selection, selectionArgs, sortOrder);
        if (blocks.iterator().hasNext()) {
            return blocks.iterator().next();
        } else {
            return null;
        }
    }

    @Override
    public UcoinBlock currentBlock() {
        String selection = SQLiteTable.Block.CURRENCY_ID + "=?";
        String[] selectionArgs = new String[]{mCurrencyId.toString()};
        String sortOrder = SQLiteTable.Block.NUMBER + " DESC LIMIT 1";
        UcoinBlocks blocks = new Blocks(mContext, mCurrencyId, selection, selectionArgs, sortOrder);
        if (blocks.iterator().hasNext()) {
            return blocks.iterator().next();
        } else {
            return null;
        }
    }

    @Override
    public Iterator<UcoinBlock> iterator() {
        ArrayList<UcoinBlock> data = new ArrayList<>();
        final Cursor cursor = query(null, mSelection, mSelectionArgs, mSortOrder);

        while (cursor.moveToNext()) {
            Long id = cursor.getLong(cursor.getColumnIndex(SQLiteTable.Block._ID));
            data.add(new Block(mContext, id));
        }
        cursor.close();

        return data.iterator();
    }
}