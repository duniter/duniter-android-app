package io.ucoin.app.model.sql.sqlite;

import android.content.Context;

import io.ucoin.app.UcoinUris;
import io.ucoin.app.model.UcoinCurrency;
import io.ucoin.app.model.UcoinIdentity;
import io.ucoin.app.model.UcoinRequirement;
import io.ucoin.app.sqlite.SQLiteTable;

public class Requirement extends Row
        implements UcoinRequirement {


    public Requirement(Context context, Long identityId) {
        super(context, UcoinUris.REQUIREMENT_URI, identityId);
    }

    @Override
    public Long currencyId() {
        return getLong(SQLiteTable.Requirement.CURRENCY_ID);
    }

    @Override
    public Long identityId() {
        return getLong(SQLiteTable.Requirement.IDENTITY_ID);
    }

    @Override
    public String publicKey() {
        return getString(SQLiteTable.Requirement.PUBLIC_KEY);
    }

    @Override
    public Long expiresIn() {
        return getLong(SQLiteTable.Requirement.EXPIRES_IN);
    }

    @Override
    public UcoinCurrency currency() {
        return new Currency(mContext, currencyId());
    }

    @Override
    public UcoinIdentity identity() { return new Identity(mContext, identityId()); }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Requirement{");
        sb.append(publicKey());
        sb.append(" ");
        sb.append(expiresIn());
        sb.append('}');
        return sb.toString();
    }
}