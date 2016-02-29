package io.ucoin.app.model.sql.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.Iterator;

import io.ucoin.app.UcoinUris;
import io.ucoin.app.enumeration.MembershipState;
import io.ucoin.app.enumeration.MembershipType;
import io.ucoin.app.model.UcoinMembership;
import io.ucoin.app.model.UcoinMemberships;
import io.ucoin.app.model.http_api.BlockchainMemberships;
import io.ucoin.app.sqlite.SQLiteTable;

final public class Memberships extends Table
        implements UcoinMemberships {

    private Long mIdentityId;

    public Memberships(Context context, Long identityId) {
        this(context, identityId, SQLiteTable.Membership.IDENTITY_ID + "=?", new String[]{identityId.toString()});
    }

    private Memberships(Context context, Long identityId, String selection, String[] selectionArgs) {
        this(context, identityId, selection, selectionArgs, null);
    }

    private Memberships(Context context, Long identityId, String selection, String[] selectionArgs, String sortOrder) {
        super(context, UcoinUris.MEMBERSHIP_URI, selection, selectionArgs, sortOrder);
        mIdentityId = identityId;
    }

    @Override
    public UcoinMembership add(BlockchainMemberships.Membership membership) {
        ContentValues values = new ContentValues();
        values.put(SQLiteTable.Membership.IDENTITY_ID, mIdentityId);
        values.put(SQLiteTable.Membership.VERSION, membership.version);
        values.put(SQLiteTable.Membership.TYPE, membership.membership.name());
        values.put(SQLiteTable.Membership.BLOCK_NUMBER, membership.blockNumber);
        values.put(SQLiteTable.Membership.BLOCK_HASH, membership.blockHash);
        values.put(SQLiteTable.Membership.STATE, MembershipState.WRITTEN.name());

        Uri uri = insert(values);
        if (Long.parseLong(uri.getLastPathSegment()) > 0) {
            return new Membership(mContext, Long.parseLong(uri.getLastPathSegment()));
        } else {
            return null;
        }
    }

    @Override
    public UcoinMembership add(MembershipType type, Long blockNumber, String blockHash) {
        ContentValues values = new ContentValues();
        values.put(SQLiteTable.Membership.IDENTITY_ID, mIdentityId);
        values.put(SQLiteTable.Membership.VERSION, 1);
        values.put(SQLiteTable.Membership.TYPE, type.name());
        values.put(SQLiteTable.Membership.BLOCK_NUMBER, blockNumber);
        values.put(SQLiteTable.Membership.BLOCK_HASH, blockHash);
        values.put(SQLiteTable.Membership.STATE, MembershipState.SEND.name());

        Uri uri = insert(values);
        if (Long.parseLong(uri.getLastPathSegment()) > 0) {
            return new Membership(mContext, Long.parseLong(uri.getLastPathSegment()));
        } else {
            return null;
        }
    }

    @Override
    public UcoinMembership getById(Long id) {
        return new Membership(mContext, id);
    }

    @Override
    public UcoinMembership lastMembership() {
        String selection = SQLiteTable.Membership.IDENTITY_ID + "=?";
        String[] selectionArgs = new String[]{mIdentityId.toString()};
        String sortOrder = SQLiteTable.Membership.BLOCK_NUMBER + " DESC LIMIT 1";
        UcoinMemberships memberships = new Memberships(mContext, mIdentityId, selection, selectionArgs, sortOrder);
        if (memberships.iterator().hasNext()) {
            return memberships.iterator().next();
        } else {
            return null;
        }
    }

    @Override
    public UcoinMemberships getByState(MembershipState state) {
        String selection = SQLiteTable.SelfCertification.IDENTITY_ID + "=? AND " +
                SQLiteTable.SelfCertification.STATE + "=?";
        String[] selectionArgs = new String[]{mIdentityId.toString(), state.name()};
        return new Memberships(mContext, mIdentityId, selection, selectionArgs);

    }

    @Override
    public Iterator<UcoinMembership> iterator() {
        Cursor cursor = fetch();
        if (cursor != null) {
            ArrayList<UcoinMembership> data = new ArrayList<>();
            while (cursor.moveToNext()) {
                Long id = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
                data.add(new Membership(mContext, id));
            }
            cursor.close();

            return data.iterator();
        }
        return null;
    }
}