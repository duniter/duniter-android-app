package io.ucoin.app.model.sql.sqlite;

import android.content.Context;

import io.ucoin.app.UcoinUris;
import io.ucoin.app.model.UcoinContact;
import io.ucoin.app.sqlite.SQLiteTable;

public class Contact extends Row
        implements UcoinContact {

    public Contact(Context context, Long contactId) {
        super(context, UcoinUris.CONTACT_URI, contactId);
    }

    @Override
    public Long currencyId() {
        return getLong(SQLiteTable.Contact.CURRENCY_ID);
    }

    @Override
    public String name() {
        return getString(SQLiteTable.Contact.NAME);
    }

    @Override
    public String uid() {
        return getString(SQLiteTable.Contact.UID);
    }

    @Override
    public String publicKey() {
        return getString(SQLiteTable.Contact.PUBLIC_KEY);
    }

    @Override
    public String toString() {
        String s = "CONTACT id=" + id() + "\n";
        s += "\ncurrencyId=" + currencyId();
        s += "\nname=" + name();
        s += "\npublicKey=" + publicKey();

        return s;
    }
}