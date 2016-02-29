package io.ucoin.app.model;

import android.database.Cursor;

public interface UcoinContacts extends SqlTable, Iterable<UcoinContact> {
    UcoinContact add(String name, String uid, String publicKey);

    UcoinContact getById(Long id);

    UcoinContact getByName(String name);

    UcoinContact getByPublicKey(String publicKey);

    Cursor getbyCurrency();
}