package io.ucoin.app.model;

import android.database.Cursor;

import java.io.Serializable;

import io.ucoin.app.database.Contract;
import io.ucoin.app.technical.crypto.CryptoUtils;

/**
 * A wallet is a user account
 * Created by eis on 13/01/15.
 */
public class Community implements Serializable {

    private Peer mPeers[];

    private Integer mId;
    private String mCurrencyName;
    private Integer mMembersCount;
    private String mFirstBlockSignature;


    public Community(BlockchainParameter parameter, BlockchainBlock firstBlock,
                      BlockchainBlock lastBlock, Peer[] peers) {
        mCurrencyName = parameter.getCurrency();
        mFirstBlockSignature = firstBlock.getSignature();
        mMembersCount = lastBlock.getMembersCount();
        mPeers = peers;
    }

    public Community(Cursor cursor) {
        int idIndex = cursor.getColumnIndex(Contract.Community._ID);
        mId = cursor.getInt(idIndex);

        int currencyNameIndex = cursor.getColumnIndex(Contract.Community.CURRENCY_NAME);
        mCurrencyName = cursor.getString(currencyNameIndex);

        int membersCountIndex = cursor.getColumnIndex(Contract.Community.MEMBERS_COUNT);
        mMembersCount = cursor.getInt(membersCountIndex);

        int firstBlockSignatureIndex = cursor
                .getColumnIndex(Contract.Community.FIRST_BLOCK_SIGNATURE);
        mFirstBlockSignature = cursor.getString(firstBlockSignatureIndex);
    }

    public Integer getId() {
        return mId;
    }
    public String getCurrencyName()
    {
        return mCurrencyName;
    }

    public Integer getMembersCount() {
        return mMembersCount;
    }

    public String getFirstBlockSignature() {
        return mFirstBlockSignature;
    }

    public Peer[] getPeers() {
        return mPeers;
    }
}
